package roomcopy;

import utils.Messages;
import extension.GRoomCloner;
import extension.logger.Logger;
import extension.tools.GPresetImporter;
import extension.tools.PresetUtils;
import extension.tools.StackTileSetting;
import extension.tools.presetconfig.PresetConfig;
import extension.tools.presetconfig.PresetConfigUtils;
import extension.tools.presetconfig.furni.PresetFurni;
import game.FloorState;
import game.Inventory;
import furnidata.FurniDataTools;
import furnidata.details.FloorItemDetails;
import gearth.extensions.parsers.HFloorItem;
import gearth.extensions.parsers.HPoint;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CloneOrchestrator {

    public static final int ROOM_SCAN_SIZE = 101;

    private final GRoomCloner extension;
    private final Logger logger;
    private final Executor executor;
    private final RoomCapture roomCapture;
    private final RoomCreator roomCreator;
    private final StackTileBootstrap stackTileBootstrap;

    private volatile boolean running = false;
    private volatile boolean cancelRequested = false;
    private volatile Boolean exportResult = null;

    private String roomModel = "model_a";
    private String nameSuffix = Messages.get("settings.namesuffix.default");
    private boolean copyWallItems = true;
    private boolean useWorkAnnex = true;

    public void setUseWorkAnnex(boolean useWorkAnnex) {
        this.useWorkAnnex = useWorkAnnex;
    }

    public CloneOrchestrator(GRoomCloner extension, Executor executor) {
        this.extension = extension;
        this.logger = extension.getLogger();
        this.executor = executor;
        this.roomCapture = new RoomCapture(executor, logger);
        this.roomCreator = new RoomCreator(executor, logger);
        this.stackTileBootstrap = new StackTileBootstrap(executor, logger);
    }

    public boolean isRunning() {
        return running;
    }

    public void cancel() {
        if (running) {
            cancelRequested = true;
            logger.log(Messages.get("clone.cancel.requested"), "orange");
        }
    }

    public void setRoomModel(String roomModel) {
        this.roomModel = roomModel == null || roomModel.trim().isEmpty() ? "model_a" : roomModel.trim();
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix == null ? "" : nameSuffix;
    }

    public void setCopyWallItems(boolean copyWallItems) {
        this.copyWallItems = copyWallItems;
    }

    public boolean start(Consumer<Boolean> onFinished) {
        return start(true, onFinished);
    }

    public boolean start(boolean buildAfterExport, Consumer<Boolean> onFinished) {
        if (running) {
            logger.log(Messages.get("clone.already_running"), "red");
            return false;
        }
        running = true;
        cancelRequested = false;

        Thread thread = new Thread(() -> {
            boolean success = false;
            try {
                success = run(buildAfterExport);
            } catch (Throwable t) {
                t.printStackTrace();
                logger.log(Messages.get("clone.aborted.exception", t), "red");
            } finally {
                running = false;
                if (onFinished != null) {
                    onFinished.accept(success);
                }
            }
        }, "clone-orchestrator");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    public boolean startPresetToNewRoom(String presetName, Consumer<Boolean> onFinished) {
        if (running) {
            logger.log(Messages.get("clone.already_running"), "red");
            return false;
        }
        running = true;
        cancelRequested = false;

        Thread thread = new Thread(() -> {
            boolean success = false;
            try {
                success = runPresetToNewRoom(presetName);
            } catch (Throwable t) {
                t.printStackTrace();
                logger.log(Messages.get("clone.aborted.exception", t), "red");
            } finally {
                running = false;
                if (onFinished != null) {
                    onFinished.accept(success);
                }
            }
        }, "preset-to-new-room");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    private boolean runPresetToNewRoom(String presetName) {
        logger.log(Messages.get("preset.newroom.title", presetName), "purple");

        if (!extension.furniDataReady()) {
            logger.log(Messages.get("clone.furnidata_not_ready"), "red");
            return false;
        }

        PresetConfig preset = PresetConfigUtils.loadPreset(presetName);
        if (preset == null || preset.getFurniture().isEmpty()) {
            logger.log(Messages.get("preset.newroom.load_failed", presetName), "red");
            return false;
        }

        int maxX = 0;
        int maxY = 0;
        for (Long tile : presetFootprints(preset, false)) {
            maxX = Math.max(maxX, (int) (tile >> 32));
            maxY = Math.max(maxY, (int) (tile & 0xffffffffL));
        }
        int planWidth = maxX + 1;
        int planHeight = maxY + 1;

        if (planWidth > 60 || planHeight > 60) {
            logger.log(Messages.get("preset.newroom.too_large", planWidth, planHeight), "red");
            return false;
        }
        logger.log(Messages.get("preset.newroom.summary", presetName, preset.getFurniture().size(), planWidth, planHeight), "green");

        RoomSnapshot saved = loadRoomSnapshot(presetName);
        FloorPlanSnapshot basePlan;
        RoomSnapshot snapshot;
        HPoint presetRoot;
        if (saved != null && saved.floorPlan != null && saved.settings != null) {
            basePlan = saved.floorPlan;
            snapshot = saved;
            presetRoot = new HPoint(0, 0);
            logger.log(Messages.get("preset.newroom.saved_plan",
                    basePlan.width(), basePlan.height(), basePlan.usableTiles(),
                    basePlan.doorX, basePlan.doorY), "green");
            if (planWidth > basePlan.width() || planHeight > basePlan.height()) {
                logger.log(Messages.get("preset.newroom.plan_too_small",
                        planWidth, planHeight, basePlan.width(), basePlan.height()), "orange");
            }
        } else {
            basePlan = FloorPlanSnapshot.forPreset(planWidth, planHeight);
            snapshot = new RoomSnapshot(RoomSettingsSnapshot.defaults(presetName), basePlan, null);
            presetRoot = new HPoint(FloorPlanSnapshot.PRESET_ORIGIN, FloorPlanSnapshot.PRESET_ORIGIN);
            logger.log(Messages.get("preset.newroom.generated_plan",
                    basePlan.width(), basePlan.height()), "blue");
        }

        if (cancelled()) return false;

        RoomSettingsFull storedSettings = snapshot.fullSettings == null
                ? null : new RoomSettingsFull(snapshot.fullSettings);
        String baseName = storedSettings != null && !storedSettings.name.trim().isEmpty()
                ? storedSettings.name
                : presetName;
        String newName = baseName + nameSuffix;
        logger.log(Messages.get("preset.newroom.roomname", newName), "blue");

        roomCreator.logRoomQuota();
        RoomCreator.CreatedRoom created = roomCreator.createRoom(
                newName, "", roomModel, 0, 25, 0);
        if (created == null) {
            return false;
        }
        if (cancelled()) return false;

        if (!roomCreator.enterRoom(created.roomId, "")) {
            logger.log(Messages.get("room.enter.failed", created.roomId), "orange");
            return false;
        }
        Utils.sleep(600);

        WorkAnnex annex = useWorkAnnex
                ? WorkAnnex.build(basePlan, extension.getStackTileSetting().getDimension())
                : null;

        if (!applyFloorPlan(snapshot, annex)) {
            return false;
        }
        if (cancelled()) return false;

        if (!waitForRoomRights()) return false;

        if (storedSettings != null) {
            applyRoomSettings(snapshot, storedSettings, created.roomId, newName, baseName);
            if (cancelled()) return false;
        }
        if (!ensureInventory()) return false;
        if (cancelled()) return false;

        HPoint stackTileLocation;
        HPoint reservedSpace;
        if (annex != null) {
            stackTileLocation = annex.getStackTileSpot();
            reservedSpace = annex.getReservedSpot();
        } else {
            stackTileLocation = pickStackTileLocation(snapshot, preset);
            reservedSpace = stackTileLocation == null
                    ? null : pickReservedSpace(snapshot, preset, stackTileLocation);
            if (stackTileLocation == null || reservedSpace == null) {
                logger.log(Messages.get("stacktile.no_space_found"), "red");
                return false;
            }
        }

        int stackTileId = stackTileBootstrap.ensureStackTile(extension.getStackTileSetting(),
                extension.getItemSource(), extension.getFloorState(), extension.getInventory(),
                extension.getFurniDataTools(), stackTileLocation);
        if (stackTileId == StackTileBootstrap.FAILED) {
            return false;
        }
        if (cancelled()) return false;

        List<Integer> helperStackTiles = placeSmallerStackTiles(annex);
        if (cancelled()) return false;

        if (!buildPreset(preset, reservedSpace, 0, stackTileLocation, presetRoot)) {
            return false;
        }

        for (Integer helper : helperStackTiles) {
            pickUpStackTile(helper);
        }

        boolean cleanedUp = false;
        if (annex != null) {
            cleanedUp = removeAnnex(snapshot, annex, stackTileId);
        } else if (stackTileId > 0) {
            cleanedUp = pickUpStackTile(stackTileId);
        }

        logger.log(Messages.get("clone.done", presetName, created.roomId), "purple");
        if (!cleanedUp) {
            logger.log(Messages.get("clone.cleanup_incomplete"), "orange");
        }
        return true;
    }

    private List<Integer> placeSmallerStackTiles(WorkAnnex annex) {
        List<Integer> placed = new ArrayList<>();
        if (annex == null) {
            return placed;
        }

        int mainDimension = extension.getStackTileSetting().getDimension();
        if (mainDimension <= 1) {
            return placed;
        }

        HPoint mediumSpot = annex.getMediumStackSpot();
        addHelperStackTile(placed, StackTileSetting.Large, mainDimension, mediumSpot);

        HPoint smallSpot = mainDimension > 2 ? mediumSpot : annex.getStackTileSpot();
        addHelperStackTile(placed, StackTileSetting.Small, mainDimension, smallSpot);

        if (!placed.isEmpty()) {
            logger.log(Messages.get("stacktile.helpers_placed", placed.size()), "green");
        }
        return placed;
    }

    private void addHelperStackTile(List<Integer> placed, StackTileSetting setting,
                                    int mainDimension, HPoint spot) {
        if (setting.getDimension() >= mainDimension || spot == null) {
            return;
        }
        int id = stackTileBootstrap.ensureStackTile(setting,
                extension.getItemSource(), extension.getFloorState(), extension.getInventory(),
                extension.getFurniDataTools(), spot);
        if (id > 0) {
            placed.add(id);
        } else if (id == StackTileBootstrap.FAILED) {
            logger.log(Messages.get("stacktile.helper_unavailable", setting.toString()), "orange");
        }
    }

    private boolean cancelled() {
        return cancelRequested;
    }

    public void reloadCurrentRoom() {
        int roomId = extension.getFloorState().getRoomId();
        if (roomId == 0) {
            logger.log(Messages.get("room.reload.unknown"), "red");
            return;
        }
        Thread thread = new Thread(() -> {
            logger.log(Messages.get("room.reload.start", roomId), "blue");
            if (roomCreator.enterRoom(roomId, "")) {
                logger.log(Messages.get("room.reload.done"), "green");
            }
        }, "room-reload");
        thread.setDaemon(true);
        thread.start();
    }

    private boolean ensureRoomState() {
        FloorState floorState = extension.getFloorState();
        if (floorState.inRoom()) {
            return true;
        }
        int roomId = floorState.getRoomId();
        if (roomId == 0) {
            logger.log(Messages.get("clone.no_room"), "red");
            return false;
        }
        logger.log(Messages.get("room.reload.start", roomId), "blue");
        if (!roomCreator.enterRoom(roomId, "")) {
            return false;
        }
        Utils.sleep(800);
        if (!floorState.inRoom()) {
            logger.log(Messages.get("clone.no_room"), "red");
            return false;
        }
        logger.log(Messages.get("room.reload.done"), "green");
        return true;
    }

    private void announce(String key, Object... args) {
        String text = Messages.get(key, args);
        logger.log(text, "blue");
        extension.sendVisualChatInfo(text);
    }

    private boolean run(boolean buildAfterExport) {
        logger.log(Messages.get("clone.start.header"), "purple");

        if (!extension.furniDataReady()) {
            logger.log(Messages.get("clone.furnidata_not_ready"), "red");
            return false;
        }
        if (!ensureRoomState()) {
            return false;
        }
        FloorState floorState = extension.getFloorState();
        if (!extension.getPermissions().canMoveFurni()) {
            logger.log(Messages.get("clone.no_furni_rights"), "red");
            return false;
        }

        RoomSnapshot snapshot = roomCapture.capture(extension.getFurniDataTools());
        if (snapshot == null || snapshot.settings == null) {
            return false;
        }
        if (snapshot.floorPlan == null) {
            logger.log(Messages.get("clone.no_floorplan"), "red");
            return false;
        }
        if (cancelled()) return false;

        RoomSettingsFull sourceSettings = RoomSettingsFull.request(executor, logger, snapshot.settings.id);
        if (sourceSettings == null && snapshot.settings.missingFields > 0) {
            logger.log(Messages.get("capture.settings_fields_missing", snapshot.settings.missingFields), "orange");
        }

        String presetName = presetNameFor(snapshot.settings);
        if (!exportPreset(presetName)) {
            return false;
        }
        if (cancelled()) return false;

        PresetConfig preset = PresetConfigUtils.loadPreset(presetName);
        if (preset == null) {
            logger.log(Messages.get("preset.load_failed.after_export"), "red");
            return false;
        }
        logger.log(Messages.get("preset.exported.summary", presetName, preset.getFurniture().size()), "green");

        writeRoomSnapshot(presetName, snapshot, sourceSettings);

        if (!buildAfterExport) {
            logger.log(Messages.get("clone.export_only.done", presetName), "green");
            announce("clone.export_only.announce");
            return true;
        }

        int sourceHeightOffset = PresetUtils.lowestFloorPoint(floorState,
                new HPoint(0, 0), new HPoint(ROOM_SCAN_SIZE, ROOM_SCAN_SIZE));
        logger.log(Messages.get("clone.source_height_offset", sourceHeightOffset), "blue");

        if (cancelled()) return false;

        roomCreator.logRoomQuota();

        String newName = snapshot.settings.name + nameSuffix;
        String creationName = safeCreationName(snapshot.settings.id);
        logger.log(Messages.get("room.create.safe_name", creationName, newName), "blue");

        RoomCreator.CreatedRoom created = roomCreator.createRoom(
                creationName, "", roomModel, 0, 25, 0, snapshot.settings.id);
        if (created == null) {
            return false;
        }
        if (cancelled()) return false;

        if (!roomCreator.enterRoom(created.roomId, "")) {
            logger.log(Messages.get("room.enter.failed", created.roomId), "orange");
            return false;
        }

        Utils.sleep(600);

        WorkAnnex annex = useWorkAnnex
                ? WorkAnnex.build(snapshot.floorPlan, extension.getStackTileSetting().getDimension())
                : null;
        if (useWorkAnnex && annex == null) {
            logger.log(Messages.get("annex.not_fitting"), "orange");
        }

        if (!applyFloorPlan(snapshot, annex)) {
            return false;
        }
        if (cancelled()) return false;

        if (!waitForRoomRights()) {
            return false;
        }
        if (cancelled()) return false;

        if (!applyRoomSettings(snapshot, sourceSettings, created.roomId, newName, snapshot.settings.name)) {
            return false;
        }
        if (cancelled()) return false;

        if (!ensureInventory()) {
            return false;
        }
        if (cancelled()) return false;

        HPoint stackTileLocation;
        HPoint reservedSpace;
        if (annex != null) {
            stackTileLocation = annex.getStackTileSpot();
            reservedSpace = annex.getReservedSpot();
        } else {
            stackTileLocation = pickStackTileLocation(snapshot, preset);
            if (stackTileLocation == null) {
                logger.log(Messages.get("stacktile.no_spot"), "red");
                return false;
            }
            reservedSpace = pickReservedSpace(snapshot, preset, stackTileLocation);
            if (reservedSpace == null) {
                logger.log(Messages.get("clone.no_free_space"), "red");
                return false;
            }
        }

        int stackTileId = stackTileBootstrap.ensureStackTile(extension.getStackTileSetting(), extension.getItemSource(),
                extension.getFloorState(), extension.getInventory(), extension.getFurniDataTools(),
                stackTileLocation);
        if (stackTileId == StackTileBootstrap.FAILED) {
            return false;
        }
        if (cancelled()) return false;

        List<Integer> helperStackTiles = placeSmallerStackTiles(annex);
        if (cancelled()) return false;

        int targetHeightOffset = PresetUtils.lowestFloorPoint(extension.getFloorState(),
                new HPoint(0, 0), new HPoint(ROOM_SCAN_SIZE, ROOM_SCAN_SIZE));
        if (targetHeightOffset != sourceHeightOffset) {
            logger.log(Messages.get("clone.height_offset_mismatch", targetHeightOffset, sourceHeightOffset), "orange");
        }

        if (!buildPreset(preset, reservedSpace, sourceHeightOffset, stackTileLocation, new HPoint(0, 0))) {
            return false;
        }

        for (Integer helper : helperStackTiles) {
            pickUpStackTile(helper);
        }

        boolean cleanedUp = false;
        if (annex != null) {
            cleanedUp = removeAnnex(snapshot, annex, stackTileId);
        } else if (stackTileId > 0) {
            cleanedUp = pickUpStackTile(stackTileId);
        }

        if (copyWallItems && snapshot.wallItems != null && snapshot.wallItems.size() > 0) {
            if (cancelled()) {
                logger.log(Messages.get("wallitems.skipped_cancelled"), "orange");
            } else {
                logger.log(Messages.get("wallitems.placing", snapshot.wallItems.size()), "blue");
                snapshot.wallItems.applyTo(executor, extension.getInventory(),
                        extension.getFurniDataTools(), logger, () -> !cancelled());
            }
        }

        logger.log(Messages.get("clone.done", newName, created.roomId), "purple");
        if (!cleanedUp) {
            logger.log(Messages.get("clone.cleanup_incomplete"), "orange");
        }
        return true;
    }

    private String safeCreationName(int sourceRoomId) {
        return "Clone " + sourceRoomId;
    }

    private String presetNameFor(RoomSettingsSnapshot settings) {
        String base = settings.name
                .replace('/', '⁄')
                .replace('\\', '⁄')
                .replace('|', '¦')
                .replace('"', '\'')
                .replaceAll("[<>:?*]", "_")
                .trim();
        if (base.isEmpty()) {
            base = Messages.get("preset.name.fallback");
        }
        return uniquePresetName(base);
    }

    private String uniquePresetName(String base) {
        String chosen = numberedName(base, CloneOrchestrator::presetNameTaken);
        if (!chosen.equals(base)) {
            logger.log(Messages.get("preset.name.numbered", base, chosen), "blue");
        }
        return chosen;
    }

    static String numberedName(String base, java.util.function.Predicate<String> taken) {
        if (!taken.test(base)) {
            return base;
        }
        for (int copy = 1; copy <= 999; copy++) {
            String candidate = base + " (" + copy + ")";
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
        return base;
    }

    private static boolean presetNameTaken(String name) {
        File dir = new File(PresetConfigUtils.presetPath());
        return new File(dir, name + PresetConfigUtils.PRESET_EXT).isFile()
                || new File(dir, name + PresetConfigUtils.ROOM_EXT).isFile();
    }

    private boolean exportPreset(String presetName) {
        announce("preset.export.start", presetName);
        exportResult = null;
        if (!extension.getExporter().startExportAll(presetName, success -> exportResult = success)) {
            return false;
        }

        long deadline = System.currentTimeMillis() + 15 * 60 * 1000L;
        while (exportResult == null) {
            if (cancelled()) {
                extension.getExporter().reset();
                logger.log(Messages.get("preset.export.cancelled"), "orange");
                return false;
            }
            if (System.currentTimeMillis() > deadline) {
                extension.getExporter().reset();
                logger.log(Messages.get("preset.export.timeout"), "red");
                return false;
            }
            Utils.sleep(200);
        }

        if (!Boolean.TRUE.equals(exportResult)) {
            logger.log(Messages.get("preset.export.failed"), "red");
            return false;
        }
        extension.updateInstalledPresets();
        return true;
    }

    private RoomSnapshot loadRoomSnapshot(String presetName) {
        File file = new File(PresetConfigUtils.presetPath(), presetName + PresetConfigUtils.ROOM_EXT);
        if (!file.isFile()) {
            return null;
        }
        try {
            String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return new RoomSnapshot(new JSONObject(json));
        } catch (Throwable t) {
            logger.log(Messages.get("preset.newroom.snapshot_unreadable", file.getName(), t), "orange");
            return null;
        }
    }

    private void writeRoomSnapshot(String presetName, RoomSnapshot snapshot, RoomSettingsFull fullSettings) {
        if (fullSettings != null) {
            snapshot = new RoomSnapshot(snapshot.settings, snapshot.floorPlan, snapshot.wallItems,
                    fullSettings.toJson());
        }
        File dir = new File(PresetConfigUtils.presetPath());
        dir.mkdirs();
        File target = new File(dir, presetName + PresetConfigUtils.ROOM_EXT);
        try (Writer writer = new OutputStreamWriter(
                Files.newOutputStream(target.toPath()), StandardCharsets.UTF_8)) {
            writer.write(snapshot.toJson().toString(4));
            writer.flush();
            logger.log(Messages.get("capture.roomdata.saved", target.getName()), "blue");
        } catch (IOException e) {
            logger.log(Messages.get("capture.roomdata.save_failed", e), "orange");
        }
    }

    private boolean applyRoomSettings(RoomSnapshot snapshot, RoomSettingsFull sourceSettings, int newRoomId) {
        return applyRoomSettings(snapshot, sourceSettings, newRoomId, null);
    }

    private boolean applyRoomSettings(RoomSnapshot snapshot, RoomSettingsFull sourceSettings, int newRoomId,
                                      String nameOverride) {
        return applyRoomSettings(snapshot, sourceSettings, newRoomId, nameOverride, null);
    }

    private boolean applyRoomSettings(RoomSnapshot snapshot, RoomSettingsFull sourceSettings, int newRoomId,
                                      String nameOverride, String fallbackName) {
        if (nameOverride != null) {
            logger.log(Messages.get("settings.rename", nameOverride), "blue");
        }
        int lockType = sourceSettings != null ? sourceSettings.doorMode : snapshot.settings.lockType;
        boolean havePassword = sourceSettings != null && !sourceSettings.doorPassword.isEmpty();
        if (lockType == 2 && !havePassword) {
            logger.log(Messages.get("settings.door_password_warning"), "orange");
            lockType = 0;
        }

        Utils.sleep(1500);

        Executor.AwaitingPacket saved =
                new Executor.AwaitingPacket("RoomSettingsSaved", HMessage.Direction.TOCLIENT, 10000);
        Executor.AwaitingPacket error =
                new Executor.AwaitingPacket("RoomSettingsSaveError", HMessage.Direction.TOCLIENT, 10000);
        executor.register(saved, error);

        boolean sent;
        if (sourceSettings != null) {
            announce("settings.transfer.full");
            sent = sourceSettings.applyTo(executor, newRoomId, null, lockType, nameOverride);
        } else {
            announce("settings.transfer.basic");
            sent = snapshot.settings.applyTo(executor, newRoomId, null, lockType, nameOverride);
        }

        if (!sent) {
            logger.log(Messages.get("settings.save.send_failed"), "red");
            return false;
        }

        executor.awaitPacket(saved, error);
        if (error.getPacket() != null) {
            logger.log(Messages.get("settings.rejected", error.getPacket().toExpression()), "orange");
            return finishSettings(snapshot, sourceSettings, newRoomId, nameOverride, fallbackName, lockType);
        }
        if (saved.getPacket() != null) {
            logger.log(Messages.get("settings.applied"), "green");
            return finishSettings(snapshot, sourceSettings, newRoomId, nameOverride, fallbackName, lockType);
        }

        logger.log(Messages.get("settings.debug.sent", executor.lastSentDescription()), "gray");
        logger.log(Messages.get("settings.debug.await",
                executor.describeHeader(HMessage.Direction.TOCLIENT, "RoomSettingsSaved"),
                executor.describeHeader(HMessage.Direction.TOCLIENT, "RoomSettingsSaveError")), "gray");
        logger.log(Messages.get("settings.retry_raw"), "orange");
        Utils.sleep(3000);

        Executor.AwaitingPacket saved2 =
                new Executor.AwaitingPacket("RoomSettingsSaved", HMessage.Direction.TOCLIENT, 10000);
        Executor.AwaitingPacket error2 =
                new Executor.AwaitingPacket("RoomSettingsSaveError", HMessage.Direction.TOCLIENT, 10000);
        executor.register(saved2, error2);

        if (sourceSettings != null) {
            sourceSettings.applyTo(executor, newRoomId, null, lockType, nameOverride, true);
        } else {
            snapshot.settings.applyTo(executor, newRoomId, null, lockType, nameOverride, true);
        }
        logger.log(Messages.get("settings.debug.sent", executor.lastSentDescription()), "gray");

        executor.awaitPacket(saved2, error2);
        if (saved2.getPacket() != null) {
            logger.log(Messages.get("settings.applied_raw"), "green");
        } else if (error2.getPacket() != null) {
            logger.log(Messages.get("settings.rejected", error2.getPacket().toExpression()), "orange");
        } else {
            logger.log(Messages.get("settings.no_confirmation"), "orange");
        }
        return finishSettings(snapshot, sourceSettings, newRoomId, nameOverride, fallbackName, lockType);
    }

    private boolean finishSettings(RoomSnapshot snapshot, RoomSettingsFull sourceSettings, int newRoomId,
                                   String nameOverride, String fallbackName, int lockType) {
        String expectedName = nameOverride == null ? snapshot.settings.name : nameOverride;
        if (verifyRoomSettings(newRoomId, expectedName, sourceSettings)) {
            return true;
        }
        if (fallbackName == null || fallbackName.trim().isEmpty() || fallbackName.equals(expectedName)) {
            return true;
        }

        logger.log(Messages.get("settings.name_fallback", expectedName, fallbackName), "orange");
        Utils.sleep(1500);

        Executor.AwaitingPacket saved =
                new Executor.AwaitingPacket("RoomSettingsSaved", HMessage.Direction.TOCLIENT, 10000);
        Executor.AwaitingPacket error =
                new Executor.AwaitingPacket("RoomSettingsSaveError", HMessage.Direction.TOCLIENT, 10000);
        executor.register(saved, error);

        if (sourceSettings != null) {
            sourceSettings.applyTo(executor, newRoomId, null, lockType, fallbackName);
        } else {
            snapshot.settings.applyTo(executor, newRoomId, null, lockType, fallbackName);
        }
        executor.awaitPacket(saved, error);

        if (verifyRoomSettings(newRoomId, fallbackName, sourceSettings)) {
            logger.log(Messages.get("settings.name_fallback_ok", fallbackName), "green");
        } else {
            logger.log(Messages.get("settings.name_fallback_failed"), "red");
        }
        return true;
    }

    private boolean verifyRoomSettings(int roomId, String expectedName, RoomSettingsFull expected) {
        RoomSettingsFull check = RoomSettingsFull.request(executor, logger, roomId, true);
        if (check == null) {
            logger.log(Messages.get("settings.verify.unreadable", roomId), "orange");
            return true;
        }

        List<String> diffs = new ArrayList<>();
        if (expectedName != null && !expectedName.equals(check.name)) {
            diffs.add(describeDiff("name", expectedName, check.name));
        }
        if (expected != null) {
            addDiff(diffs, "doorMode", expected.doorMode, check.doorMode);
            addDiff(diffs, "category", expected.categoryId, check.categoryId);
            addDiff(diffs, "maxVisitors", expected.maximumVisitors, check.maximumVisitors);
            addDiff(diffs, "tradeMode", expected.tradeMode, check.tradeMode);
            addDiff(diffs, "allowPets", expected.allowPets, check.allowPets);
            addDiff(diffs, "allowFoodConsume", expected.allowFoodConsume, check.allowFoodConsume);
            addDiff(diffs, "allowWalkThrough", expected.allowWalkThrough, check.allowWalkThrough);
            addDiff(diffs, "hideWalls", expected.hideWalls, check.hideWalls);
            addDiff(diffs, "wallThickness", expected.wallThickness, check.wallThickness);
            addDiff(diffs, "floorThickness", expected.floorThickness, check.floorThickness);
            addDiff(diffs, "floodSensitivity", expected.chatFloodSensitivity, check.chatFloodSensitivity);
            addDiff(diffs, "leaveOnDoorTile", expected.leaveOnDoorTile, check.leaveOnDoorTile);
            addDiff(diffs, "idleSleep", expected.idleSleepEnabled, check.idleSleepEnabled);
            addDiff(diffs, "idleAutokick", expected.idleAutokickEnabled, check.idleAutokickEnabled);
            addDiff(diffs, "muteAllPets", expected.muteAllPets, check.muteAllPets);
            addDiff(diffs, "whoCanMute", expected.whoCanMute, check.whoCanMute);
            addDiff(diffs, "whoCanKick", expected.whoCanKick, check.whoCanKick);
            addDiff(diffs, "whoCanBan", expected.whoCanBan, check.whoCanBan);
            addDiff(diffs, "tags", expected.tags.size(), check.tags.size());
        }

        boolean nameMatches = expectedName == null || expectedName.equals(check.name);
        if (diffs.isEmpty()) {
            logger.log(Messages.get("settings.verify.all_ok", check.name), "green");
            return true;
        }
        logger.log(Messages.get("settings.verify.diff_count", diffs.size()), "red");
        for (String diff : diffs) {
            logger.log("   " + diff, "orange");
        }
        return nameMatches;
    }

    private static void addDiff(List<String> diffs, String field, Object expected, Object actual) {
        if (!String.valueOf(expected).equals(String.valueOf(actual))) {
            diffs.add(describeDiff(field, expected, actual));
        }
    }

    private static String describeDiff(String field, Object expected, Object actual) {
        return Messages.get("settings.verify.field", field, expected, actual);
    }

    private boolean applyFloorPlan(RoomSnapshot snapshot, WorkAnnex annex) {
        announce("floorplan.transfer",
                snapshot.floorPlan.width(), snapshot.floorPlan.height(), snapshot.floorPlan.usableTiles(),
                snapshot.floorPlan.doorX, snapshot.floorPlan.doorY, snapshot.floorPlan.doorDir,
                snapshot.floorPlan.writableWallHeight(),
                snapshot.settings.wallThickness, snapshot.settings.floorThickness);

        if (snapshot.floorPlan.wallHeight < 0) {
            logger.log(Messages.get("floorplan.wallheight_default"), "blue");
        }
        if (annex != null) {
            logger.log(annex.describe(extension.getStackTileSetting().getDimension()), "blue");
            logger.log(Messages.get("annex.purpose"), "blue");
        }

        String expectedPlan = annex != null ? annex.getPlan() : snapshot.floorPlan.floorPlan;
        int expectedWidth = planWidth(expectedPlan);
        int expectedTiles = walkableTiles(expectedPlan);

        for (int attempt = 1; attempt <= FLOORPLAN_ATTEMPTS; attempt++) {
            Executor.AwaitingPacket floorHeightMap =
                    new Executor.AwaitingPacket("FloorHeightMap", HMessage.Direction.TOCLIENT, 20000);
            Executor.AwaitingPacket objects =
                    new Executor.AwaitingPacket("Objects", HMessage.Direction.TOCLIENT, 20000);
            executor.register(floorHeightMap, objects);

            boolean sent = annex != null
                    ? annex.applyTo(executor, snapshot.settings.wallThickness, snapshot.settings.floorThickness,
                            snapshot.floorPlan.wallHeight)
                    : snapshot.floorPlan.applyTo(executor,
                            snapshot.settings.wallThickness, snapshot.settings.floorThickness);

            if (!sent) {
                logger.log(Messages.get("floorplan.send_failed"), "red");
                return false;
            }

            executor.awaitPacketList(floorHeightMap, objects);
            if (floorHeightMap.getPacket() == null) {
                logger.log(Messages.get("floorplan.not_confirmed", roomModel), "red");
                return false;
            }

            String actualPlan = readHeightMapPlan(floorHeightMap.getPacket());
            int actualWidth = planWidth(actualPlan);
            int actualTiles = walkableTiles(actualPlan);
            if (actualWidth == expectedWidth && actualTiles == expectedTiles) {
                Utils.sleep(800);
                if (!extension.getFloorState().inRoom()) {
                    logger.log(Messages.get("floorplan.state_incomplete"), "orange");
                    Utils.sleep(1500);
                }
                logger.log(Messages.get("floorplan.verified", actualWidth, planHeight(actualPlan), actualTiles), "green");
                return true;
            }

            logger.log(Messages.get("floorplan.mismatch",
                    expectedWidth, planHeight(expectedPlan), expectedTiles,
                    actualWidth, planHeight(actualPlan), actualTiles,
                    attempt, FLOORPLAN_ATTEMPTS), "orange");
            if (attempt < FLOORPLAN_ATTEMPTS) {
                Utils.sleep(FLOORPLAN_RETRY_WAIT_MS);
            }
        }

        logger.log(Messages.get("floorplan.never_applied"), "red");
        return false;
    }

    private static final int FLOORPLAN_ATTEMPTS = 3;
    private static final int FLOORPLAN_RETRY_WAIT_MS = 2500;

    private static String readHeightMapPlan(HPacket packet) {
        try {
            packet.resetReadIndex();
            packet.readBoolean();
            packet.readInteger();
            return packet.readString(StandardCharsets.UTF_8);
        } catch (Throwable t) {
            return "";
        }
    }

    private static String[] planRows(String plan) {
        if (plan == null) return new String[0];
        return plan.replace((char) 10, (char) 13).split(String.valueOf((char) 13));
    }

    private static int planWidth(String plan) {
        int width = 0;
        for (String row : planRows(plan)) {
            width = Math.max(width, row.length());
        }
        return width;
    }

    private static int planHeight(String plan) {
        int height = 0;
        for (String row : planRows(plan)) {
            if (!row.isEmpty()) height++;
        }
        return height;
    }

    private static int walkableTiles(String plan) {
        int count = 0;
        for (String row : planRows(plan)) {
            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);
                if (c != 'x' && c != 'X') count++;
            }
        }
        return count;
    }



    private boolean stillInRoom(int furniId) {
        try {
            return extension.getFloorState().furniFromId(furniId) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean pickUpStackTile(int stackTileId) {
        announce("stacktile.pickup.start", stackTileId);

        if (!stillInRoom(stackTileId)) {
            logger.log(Messages.get("stacktile.pickup.already_gone"), "green");
            return true;
        }

        if (!executor.sendToServer("PickupObject", 2, stackTileId, false)) {
            logger.log(Messages.get("stacktile.pickup.send_failed"), "orange");
            return false;
        }

        long deadline = System.currentTimeMillis() + 2500;
        while (System.currentTimeMillis() < deadline) {
            if (!stillInRoom(stackTileId)) {
                logger.log(Messages.get("stacktile.pickup.done"), "green");
                Utils.sleep(300);
                return true;
            }
            Utils.sleep(150);
        }

        logger.log(Messages.get("stacktile.pickup.still_present"), "orange");
        Utils.sleep(400);
        return false;
    }

    private List<String> annexBlockers(WorkAnnex annex) {
        List<String> blockers = new ArrayList<>();
        for (Long tile : annex.getAnnexTiles()) {
            int x = (int) (tile >> 32);
            int y = (int) (tile & 0xffffffffL);
            for (HFloorItem item : extension.getFloorState().getFurniOnTile(x, y)) {
                String className = extension.getFurniDataTools().getFloorItemName(item.getTypeId());
                blockers.add((className == null ? Messages.get("preset.furni.unknown_typeid", item.getTypeId()) : className)
                        + " @" + x + "," + y);
            }
        }
        return blockers;
    }

    private boolean annexStillWalkable(HPacket floorHeightMapPacket, WorkAnnex annex) {
        String plan;
        try {
            plan = floorHeightMapPacket.readString(11);
        } catch (Throwable t) {
            return true;
        }
        if (plan == null) {
            return true;
        }

        String[] rows = plan.split("\r");
        for (Long tile : annex.getAnnexTiles()) {
            int x = (int) (tile >> 32);
            int y = (int) (tile & 0xffffffffL);
            if (y < rows.length && x < rows[y].length()) {
                char c = rows[y].charAt(x);
                if (c != 'x' && c != 'X') {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean removeAnnex(RoomSnapshot snapshot, WorkAnnex annex, int stackTileId) {
        if (stackTileId > 0) {
            pickUpStackTile(stackTileId);
        }

        announce("annex.remove.start");

        Executor.AwaitingPacket floorHeightMap =
                new Executor.AwaitingPacket("FloorHeightMap", HMessage.Direction.TOCLIENT, 20000);
        executor.register(floorHeightMap);

        if (!snapshot.floorPlan.applyTo(executor,
                snapshot.settings.wallThickness, snapshot.settings.floorThickness)) {
            logger.log(Messages.get("annex.remove.send_failed"), "orange");
            return false;
        }

        HPacket response = executor.awaitPacket(floorHeightMap);
        if (response == null) {
            logger.log(Messages.get("annex.remove.no_response"), "orange");
        } else if (!annexStillWalkable(response, annex)) {
            Utils.sleep(600);
            logger.log(Messages.get("annex.remove.done",
                    snapshot.floorPlan.doorX, snapshot.floorPlan.doorY, snapshot.floorPlan.doorDir), "green");
            return true;
        } else {
            logger.log(Messages.get("annex.remove.rejected"), "orange");
        }

        List<String> blockers = annexBlockers(annex);
        if (blockers.isEmpty()) {
            logger.log(Messages.get("annex.blockers.state_empty"), "orange");
        } else {
            logger.log(Messages.get("annex.blockers.count", blockers.size()), "orange");
            for (String blocker : blockers.subList(0, Math.min(blockers.size(), 8))) {
                logger.log("   " + blocker, "orange");
            }
        }

        restoreDoorOnly(snapshot, annex);
        logger.log(Messages.get("annex.remove.manual_hint"), "orange");
        return false;
    }

    private void restoreDoorOnly(RoomSnapshot snapshot, WorkAnnex annex) {
        logger.log(String.format(Messages.get("annex.door.restore_only"),
                snapshot.floorPlan.doorX, snapshot.floorPlan.doorY, snapshot.floorPlan.doorDir), "blue");

        Executor.AwaitingPacket floorHeightMap =
                new Executor.AwaitingPacket("FloorHeightMap", HMessage.Direction.TOCLIENT, 15000);
        executor.register(floorHeightMap);

        boolean sent = executor.sendToServer("UpdateFloorProperties",
                annex.getPlan(), snapshot.floorPlan.doorX, snapshot.floorPlan.doorY, snapshot.floorPlan.doorDir,
                snapshot.settings.wallThickness, snapshot.settings.floorThickness,
                snapshot.floorPlan.writableWallHeight());

        if (!sent || executor.awaitPacket(floorHeightMap) == null) {
            logger.log(Messages.get("annex.door.restore_failed"), "orange");
            return;
        }
        logger.log(Messages.get("annex.door.restored"), "green");
    }

    private boolean waitForRoomRights() {
        boolean wiredNeeded = extension.shouldExportWired();
        long deadline = System.currentTimeMillis() + 20000;
        boolean logged = false;

        while (true) {
            boolean inRoom = extension.getFloorState().inRoom();
            boolean furni = extension.getPermissions().canMoveFurni();
            boolean wired = !wiredNeeded || extension.getPermissions().canModifyWired();

            if (inRoom && furni && wired) {
                return true;
            }
            if (cancelled()) return false;
            if (System.currentTimeMillis() > deadline) {
                logger.log(Messages.get("room.rights.missing",
                        inRoom ? "" : Messages.get("room.rights.missing.room_state"),
                        furni ? "" : Messages.get("room.rights.missing.furni"),
                        wired ? "" : Messages.get("room.rights.missing.wired")), "red");
                return false;
            }
            if (!logged) {
                logger.log(Messages.get("room.rights.waiting"), "blue");
                logged = true;
            }
            Utils.sleep(250);
        }
    }

    private boolean ensureInventory() {
        Inventory inventory = extension.getInventory();

        if (inventory.getState() == Inventory.InventoryState.LOADED) {
            logger.log(Messages.get("inventory.already_loaded"), "blue");
            return true;
        }

        logger.log(Messages.get("inventory.loading.long_wait"), "blue");
        if (inventory.getState() != Inventory.InventoryState.LOADING) {
            inventory.requestInventory();
        }

        long start = System.currentTimeMillis();
        long deadline = start + 45000L;
        long nextNote = start + 5000;

        while (inventory.getState() != Inventory.InventoryState.LOADED) {
            if (cancelled()) return false;
            if (System.currentTimeMillis() > deadline) {
                logger.log(Messages.get("inventory.timeout"), "red");
                return false;
            }
            if (System.currentTimeMillis() > nextNote) {
                nextNote += 15000;
                logger.log(String.format(Messages.get("inventory.waiting"),
                        (System.currentTimeMillis() - start) / 1000, inventory.getState()), "blue");
            }
            Utils.sleep(250);
        }

        logger.log(Messages.get("inventory.loaded.duration", (System.currentTimeMillis() - start) / 1000), "green");
        return true;
    }

    private Set<Long> presetFootprints(PresetConfig preset, boolean onlyUnmovable) {
        Set<Long> tiles = new HashSet<>();
        FurniDataTools furniData = extension.getFurniDataTools();

        for (PresetFurni furni : preset.getFurniture()) {
            String className = furni.getClassName();
            if (onlyUnmovable && furniData.isStackable(className)) {
                continue;
            }

            int xDim = 1;
            int yDim = 1;
            FloorItemDetails details = furniData.getFloorItemDetails(className);
            if (details != null) {
                xDim = Math.max(1, details.xDim);
                yDim = Math.max(1, details.yDim);
            }
            int rotation = furni.getRotation();
            if (rotation == 2 || rotation == 6) {
                int swap = xDim;
                xDim = yDim;
                yDim = swap;
            }

            for (int dx = 0; dx < xDim; dx++) {
                for (int dy = 0; dy < yDim; dy++) {
                    tiles.add(key(furni.getLocation().getX() + dx, furni.getLocation().getY() + dy));
                }
            }
        }
        return tiles;
    }

    private HPoint pickStackTileLocation(RoomSnapshot snapshot, PresetConfig preset) {
        int dimension = Math.max(1, extension.getStackTileSetting().getDimension());

        int[] spot = snapshot.floorPlan.findFlatSquare(dimension, presetFootprints(preset, false));
        if (spot != null) {
            return new HPoint(spot[0], spot[1]);
        }

        spot = snapshot.floorPlan.findFlatSquare(dimension, presetFootprints(preset, true));
        if (spot != null) {
            logger.log(Messages.get("stacktile.spot.will_be_built_on"), "orange");
            return new HPoint(spot[0], spot[1]);
        }

        spot = snapshot.floorPlan.findFlatSquare(dimension, null);
        if (spot != null) {
            logger.log(Messages.get("stacktile.spot.multi_tile_conflict"), "orange");
            return new HPoint(spot[0], spot[1]);
        }

        if (dimension > 1) {
            logger.log(Messages.get("stacktile.spot.smaller_area", dimension, dimension), "orange");
            spot = snapshot.floorPlan.findFlatSquare(1, null);
        }
        return spot == null ? null : new HPoint(spot[0], spot[1]);
    }

    private HPoint pickReservedSpace(RoomSnapshot snapshot, PresetConfig preset, HPoint stackTileLocation) {
        Set<Long> occupied = presetFootprints(preset, false);

        int dimension = Math.max(1, extension.getStackTileSetting().getDimension());
        for (int dx = 0; dx < dimension; dx++) {
            for (int dy = 0; dy < dimension; dy++) {
                occupied.add(key(stackTileLocation.getX() + dx, stackTileLocation.getY() + dy));
            }
        }

        String[] rows = snapshot.floorPlan.rows();
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                if (snapshot.floorPlan.isWalkable(x, y) && !occupied.contains(key(x, y))) {
                    return new HPoint(x, y);
                }
            }
        }

        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                if (snapshot.floorPlan.isWalkable(x, y) && !key(stackTileLocation.getX(), stackTileLocation.getY())
                        .equals(key(x, y))) {
                    logger.log(Messages.get("clone.reserved_space.fallback", x, y), "orange");
                    return new HPoint(x, y);
                }
            }
        }
        return null;
    }

    private static Long key(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    private boolean buildPreset(PresetConfig preset, HPoint reservedSpace, int heightOffset,
                                HPoint preferredStackTile, HPoint root) {
        GPresetImporter importer = extension.getImporter();
        importer.setPresetConfig(preset);
        extension.refreshPostConfig();

        announce("clone.build.start");
        if (!importer.startImport(reservedSpace, root, heightOffset, preferredStackTile)) {
            return false;
        }

        long lastProgress = System.currentTimeMillis();
        GPresetImporter.BuildingImportState lastState = importer.getState();
        while (importer.getState() != GPresetImporter.BuildingImportState.NONE) {
            if (cancelled()) {
                importer.reset();
                logger.log(Messages.get("clone.build.cancelled"), "orange");
                return false;
            }
            GPresetImporter.BuildingImportState current = importer.getState();
            if (current != lastState) {
                lastState = current;
                lastProgress = System.currentTimeMillis();
                logger.log(Messages.get("clone.build.phase", current), "blue");
            }
            if (System.currentTimeMillis() - lastProgress > 10 * 60 * 1000L) {
                importer.reset();
                logger.log(Messages.get("clone.build.stuck", current), "red");
                return false;
            }
            Utils.sleep(250);
        }

        if (!importer.lastImportSucceeded()) {
            logger.log(Messages.get("clone.build.failed"), "red");
            return false;
        }
        logger.log(Messages.get("clone.build.done"), "green");
        return true;
    }
}
