package extension;

import utils.Messages;
import extension.logger.Logger;
import extension.tools.*;
import extension.tools.importutils.AvailabilityChecker;
import extension.tools.importutils.FurniDropInfo;
import extension.tools.postconfig.FurniPostConfig;
import extension.tools.postconfig.ItemSource;
import extension.tools.postconfig.PostConfig;
import extension.tools.presetconfig.PresetConfig;
import extension.tools.presetconfig.PresetConfigUtils;
import extension.tools.presetconfig.furni.PresetFurni;
import furnidata.FurniDataTools;
import game.FloorState;
import game.Inventory;
import game.RoomPermissions;
import gearth.extensions.ExtensionBase;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.HFloorItem;
import gearth.extensions.parsers.HPoint;
import utils.SettingsCache;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import extension.ui.PresetSettingsDialog;
import extension.ui.StyledDialog;
import roomcopy.CloneOrchestrator;
import roomcopy.FlatCategories;
import roomcopy.Executor;
import roomcopy.ProtocolProbe;
import utils.InterceptGuard;
import utils.Utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionInfo(
        Title =  "G-PresetsPlus",
        Description =  "Clone a whole room with settings, floor plan, furni and wired, or build a preset into a fresh room",
        Version =  "1.1.1",
        Author =  "Sucukdeluxe"
)
public class GRoomCloner extends ExtensionForm {

    public BorderPane logsBorderPane;

    public Label cndConnectedLbl;
    public Label cndRoomLbl;
    public Label cndInventoryLbl;
    public Label cndStackTileLbl;
    public Label cndFurnidataLbl;
    public Label cndPermissionsLbl;

    public ToggleGroup stacktile_tgl;

    public RadioButton onlyInvCbx;
    public RadioButton preferInvCbx;
    public RadioButton preferBcCbx;
    public RadioButton onlyBcCbx;
    public ToggleGroup item_src_tgl;
    public ListView<String> presetListView;

    public Label donateLabel;
    public RadioButton donateMissing;
    public RadioButton donateAll;
    public ToggleGroup donate_tgl;

    public Button availabilityBtn;
    public Button selfDonateBtn;
    public Button currentPresetBtn;
    public Button renamePresetBtn;
    public Button editPresetBtn;
    public Button deletePresetBtn;
    public CheckBox onTopCloneCbx;
    public CheckBox allowIncompleteBuildsCbx;

    public TextField furniNamePC_txt;
    public TextField replacementIdPC_txt;
    public TableView<FurniPostConfig> postconfigTable;
    public GridPane pcgrid;
    public Label postconfigErrorLbl;
    public CheckBox noExportWiredCbx;
    public Slider ratelimiter;

    public Button cloneBtn;
    public Button cloneBuildBtn;
    public Button cancelCloneBtn;
    public Button buildPresetBtn;
    public Button presetToNewRoomBtn;
    public TextField roomModel_txt;
    public TextField nameSuffix_txt;
    public CheckBox copyWallItemsCbx;
    public CheckBox workAnnexCbx;
    public Label cloneStatusLbl;

    public TabPane mainTabs;
    public Tab cloneTab;
    public Tab presetsTab;
    public Tab settingsTab;
    public Tab presetConfigTab;

    public Button loadInventoryBtn;
    public Button reloadRoomBtn;
    public Button reloadPresetsBtn;
    public Button openPresetsFolderBtn;
    public Button clearWiredBtn;
    public Button updatePostconfigBtn;

    public Label roomModelLbl;
    public Label workAnnexHintLbl;
    public Label nameSuffixLbl;
    public Label savedPresetsLbl;
    public Label buildPresetHintLbl;
    public Label presetToNewRoomHintLbl;
    public Label mainStackTileLbl;
    public Label itemSourceLbl;
    public Label ratelimitLbl;
    public Label languageLbl;
    public Label furniNameLbl;
    public Label existingFurniLbl;

    public RadioButton langEn;
    public RadioButton langDe;
    public ToggleGroup lang_tgl;

    private List<FurniPostConfig> furniPostConfigs = new ArrayList<>();

    private Logger logger = new Logger();

    private FurniDataTools furniDataTools = null;
    private Inventory inventory = null;
    private FloorState floorState = null;
    private RoomPermissions permissions = null;
    private volatile boolean isConnected = false;

    private GPresetExporter exporter = null;
    private GPresetImporter importer = null;

    private AutoDonator autoDonator = null;

    private Executor executor = null;
    private CloneOrchestrator cloneOrchestrator = null;

    private volatile long latestPingTimestamp = -1;
    private volatile int ping = 45;
    private volatile double pingVariation = 10;

    private StackTileSetting stackTileSetting = StackTileSetting.Large;


    public void initialize() {
        setupCache();

        logsBorderPane.setPadding(new Insets(5, 5, 5, 5));
        logger.initialize(logsBorderPane);

        logger.log(Messages.get("ui.log.title") + " " 
                + GRoomCloner.class.getAnnotation(ExtensionInfo.class).Version(), "purple");
        logger.logKey("clone.hint.start", "purple");
        logger.logKey("ui.log.chatcommands", "purple");

        stacktile_tgl.selectedToggleProperty().addListener(observable -> {
            String option = ((RadioButton)(stacktile_tgl.getSelectedToggle())).getText();
            SettingsCache.put("stacktile", option);
            stackTileSetting = StackTileSetting.fromString(option);
            updateUI();
        });

        donate_tgl.selectedToggleProperty().addListener(observable -> {
            Object option = ((RadioButton)(donate_tgl.getSelectedToggle())).getUserData();
            SettingsCache.put("donationMode", option);
        });

        noExportWiredCbx.selectedProperty().addListener(observable -> {
            SettingsCache.put("noExportWired", noExportWiredCbx.isSelected());
            updateUI();
        });

        allowIncompleteBuildsCbx.selectedProperty().addListener(observable ->
                SettingsCache.put("allowIncompleteBuilds", allowIncompleteBuildsCbx.isSelected())
        );

        workAnnexCbx.selectedProperty().addListener(observable ->
                SettingsCache.put("workAnnex", workAnnexCbx.isSelected())
        );



        postconfigTable = new TableView<>();
//        postconfigTable.setTableMenuButtonVisible(true);
        postconfigTable.setStyle("-fx-focus-color: white;");

//        postconfigTable.focusedProperty().addListener(observable -> {
//            if (postconfigTable.isFocused()) {
//                pcgrid.requestFocus();
//            }
//        });

        TableColumn<FurniPostConfig, String> furniNameColumn = new TableColumn<>(Messages.get("ui.postconfig.column.furniname"));
        furniNameColumn.setCellValueFactory(new PropertyValueFactory<>("furniIdentifier"));
        furniNameColumn.setPrefWidth(110);

        TableColumn<FurniPostConfig, Integer> existingFurniIdColumn = new TableColumn<>(Messages.get("ui.postconfig.column.existingfurniid"));
        existingFurniIdColumn.setCellValueFactory(new PropertyValueFactory<>("existingFurniId"));
        existingFurniIdColumn.setPrefWidth(120);

        postconfigTable.getColumns().addAll(Arrays.asList(furniNameColumn, existingFurniIdColumn));

        // https://stackoverflow.com/questions/20802208/delete-a-row-from-a-javafx-table-using-context-menu
        postconfigTable.setRowFactory(tableView -> {
            final TableRow<FurniPostConfig> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem(Messages.get("ui.contextmenu.remove"));
            removeMenuItem.setOnAction(event -> {
                FurniPostConfig config = row.getItem();
                if (config != null) {
                    furniPostConfigs.remove(config);
                    updatePostConfig();
                    updateFurniPostConfigsView();
                }
            });
            contextMenu.getItems().add(removeMenuItem);
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );
            return row ;
        });

        pcgrid.add(postconfigTable, 0, 0);



        ratelimiter.valueProperty().addListener((observable, oldValue, newValue) -> {
            int val = newValue.intValue();
            Utils.setExtraSleepTime(val);
            SettingsCache.put("ratelimit", val);
        });

        setupLanguage();
        applyTexts();
    }

    @Override
    public void intercept(HMessage.Direction direction, ExtensionBase.MessageListener messageListener) {
        super.intercept(direction, InterceptGuard.guard(messageListener));
    }

    @Override
    public void intercept(HMessage.Direction direction, String headerName, ExtensionBase.MessageListener messageListener) {
        super.intercept(direction, headerName, InterceptGuard.guard(messageListener));
    }

    @Override
    public void intercept(HMessage.Direction direction, int headerId, ExtensionBase.MessageListener messageListener) {
        super.intercept(direction, headerId, InterceptGuard.guard(messageListener));
    }

    @Override
    public void initExtension() {
        this.executor = new Executor(this);

        this.floorState = new FloorState(this, logger, this::updateUI, () -> {
            this.updateUI();
            this.exporter.reset();
            logger.logKey("room.leaving", "blue");
        });
        this.inventory = new Inventory(this, logger, this::updateUI);
        this.permissions = new RoomPermissions(this, logger, this::updateUI);

        this.exporter = new GPresetExporter(this);
        this.importer = new GPresetImporter(this);

        this.autoDonator = new AutoDonator(this);
        this.cloneOrchestrator = new CloneOrchestrator(this, executor);
        ProtocolProbe.attachIfEnabled(this, logger);

        onConnect((host, i, s1, s2, hClient) -> {
            showSelfDonateBtn("game-s2.habbo.com".equals(host));
            furniDataTools = new FurniDataTools(host, this::updateUI);
        });

        intercept(HMessage.Direction.TOSERVER, "LatencyPingRequest", hMessage -> {
            latestPingTimestamp = System.currentTimeMillis();
        });
        intercept(HMessage.Direction.TOCLIENT, "LatencyPingResponse", hMessage -> {
            if (latestPingTimestamp != -1) {
                int newPing = (int) (System.currentTimeMillis() - latestPingTimestamp) / 2;
                pingVariation = pingVariation * 0.66 + (Math.abs(ping - newPing)) * 0.34;
                if (pingVariation > 10) {
                    pingVariation = 10;
                }
                ping = newPing;
            }
        });

        if (isConnected) {
            this.floorState.requestRoom(this);
        }
        updateUI();
        updateInstalledPresets();

        item_src_tgl.selectedToggleProperty().addListener(o -> updatePostConfig());
        presetListView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                String presetName = presetListView.getSelectionModel().getSelectedItem();
                PresetConfig preset = PresetConfigUtils.loadPreset(presetName);
                if (preset != null) {
                    selectPreset(preset, presetName);
                }
            }
            updateUI();
        });

        updatePostConfig();
    }

    private void setupCache() {
        File extDir = null;
        try {
            extDir = (new File(GRoomCloner.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getParentFile();
            if (extDir.getName().equals("Extensions")) {
                extDir = extDir.getParentFile();
            }
        } catch (URISyntaxException ignored) {}

        SettingsCache.setCacheDir(extDir + File.separator + "Cache");
        loadCache();
    }

    private void loadCache() {
        JSONObject cache = SettingsCache.getCacheContents();

        Messages.setLanguage(Messages.Language.fromName(
                cache.optString("language", "EN"), Messages.Language.EN));

        String stackTileKey = cache.optString("stacktile", "2x2");
        stackTileSetting = StackTileSetting.fromString(stackTileKey);
        stacktile_tgl.getToggles()
                .stream()
                .filter(s -> ((RadioButton) s).getText().equals(stackTileKey))
                .findFirst()
                .orElseGet(() -> stacktile_tgl.getToggles().get(2))
                .setSelected(true);

        String itemSourceKey = cache.optString("itemSource", "ONLY_INVENTORY");
        item_src_tgl.getToggles()
                .stream()
                .filter(s -> itemSourceKey.equals(s.getUserData()))
                .findFirst()
                .orElseGet(() -> item_src_tgl.getToggles().get(0))
                .setSelected(true);

        String donationModeKey = cache.optString("donationMode", "MISSING_ITEMS");
        donate_tgl.getToggles()
                .stream()
                .filter(s -> donationModeKey.equals(s.getUserData()))
                .findFirst()
                .orElseGet(() -> donate_tgl.getToggles().get(0))
                .setSelected(true);

        ratelimiter.setValue(cache.optInt("ratelimit", 22));
        Utils.setExtraSleepTime(cache.optInt("ratelimit", 22));

        noExportWiredCbx.setSelected(cache.optBoolean("noExportWired"));
        allowIncompleteBuildsCbx.setSelected(cache.optBoolean("allowIncompleteBuilds"));

        roomModel_txt.setText(cache.optString("roomModel", "model_a"));
        nameSuffix_txt.setText(cache.optString("nameSuffix",
                Messages.get("settings.namesuffix.default")));
        copyWallItemsCbx.setSelected(cache.optBoolean("copyWallItems", true));
        workAnnexCbx.setSelected(cache.optBoolean("workAnnex", true));

        boolean onTop = cache.optBoolean("alwaysOnTop", false);
        onTopCloneCbx.setSelected(onTop);
        applyAlwaysOnTop(onTop);
    }

    private void applyAlwaysOnTop(boolean onTop) {
        SettingsCache.put("alwaysOnTop", onTop);
        syncAlwaysOnTop();
    }

    private final java.util.concurrent.atomic.AtomicBoolean categoriesRequested =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    private void maybeLoadCategories() {
        if (!isConnected || floorState == null || !floorState.inRoom() || !FlatCategories.isEmpty()) {
            return;
        }
        if (!categoriesRequested.compareAndSet(false, true)) {
            return;
        }

        Thread categories = new Thread(() -> {
            if (FlatCategories.request(executor)) {
                logger.logKey("categories.loaded", "blue", FlatCategories.selectable().size());
            } else {
                categoriesRequested.set(false);
            }
        }, "flat-categories");
        categories.setDaemon(true);
        categories.start();
    }

    private void syncAlwaysOnTop() {
        if (primaryStage == null) {
            return;
        }
        boolean wanted = onTopCloneCbx.isSelected();
        if (primaryStage.isAlwaysOnTop() != wanted) {
            primaryStage.setAlwaysOnTop(wanted);
        }
    }

    private void ensureSelectedPresetLoaded() {
        String selected = presetListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        PresetConfig loaded = importer.getPresetConfig();
        if (loaded != null && selected.equals(loadedPresetName)) {
            return;
        }
        PresetConfig preset = PresetConfigUtils.loadPreset(selected);
        if (preset != null) {
            selectPreset(preset, selected);
        }
    }

    private String loadedPresetName = null;

    private void selectPreset(PresetConfig preset, String name) {
        loadedPresetName = name;
        logger.log(String.format("Selected \"%s\" preset", name), "green");
        importer.setPresetConfig(preset);
        HPoint dim = PresetUtils.presetDimensions(preset);
        logger.logKey("preset.dimensions", "green", dim.getX(), dim.getY());
        updateUI();
    }

    @Override
    public void onStartConnection() {
        latestPingTimestamp = System.currentTimeMillis();
//        sendToServer(new HPacket("LatencyPingRequest", HMessage.Direction.TOSERVER, -1));

        isConnected = true;
        updateUI();
    }

    @Override
    public void onEndConnection() {
        isConnected = false;
        FlatCategories.clear();
        categoriesRequested.set(false);
        if (cloneOrchestrator != null) {
            cloneOrchestrator.cancel();
        }
        furniDataTools = null;
        floorState.reset();
        inventory.clear();
        exporter.reset();
        importer.reset();
        updateUI();
    }

    public HFloorItem stackTile() {
        if (!floorState.inRoom() || !furniDataTools.isReady()) return null;
        List<HFloorItem> items = floorState.getItemsFromType(furniDataTools, stackTileSetting.getClassName());
        if (items.size() == 0) return null;
        return items.get(0);
    }

    public boolean furniDataReady() {
        return furniDataTools != null && furniDataTools.isReady();
    }

//
//    public boolean isReady() {
//        return isConnected && floorState.inRoom() && furniDataReady() && inventory.getState() == Inventory.InventoryState.LOADED
//                && stackTile() != null;
//    }
//
//    private boolean BCCatalogAvailable() {
//        return catalog.getState() == BCCatalog.CatalogState.COLLECTED;
//    }

    private void updateLabel(Label lbl, boolean isFullfilled, boolean isBusy, boolean isOptional) {
        lbl.getStyleClass().removeAll(lbl.getStyleClass().stream().filter(p -> p.startsWith("lbl")).collect(Collectors.toList()));
        lbl.getStyleClass().add(
                isFullfilled ?
                        "lblgreen" : (isBusy ? "lblorange" : (isOptional ? "lblgrey" : "lblred")));
    }

    private void updateLabel(Label lbl, boolean isFullfilled, boolean isBusy) {
        updateLabel(lbl, isFullfilled, isBusy, false);
    }

    private void updateLabel(Label lbl, boolean isFullfilled) {
        updateLabel(lbl, isFullfilled, false);
    }

    private void updateUI() {
        maybeLoadCategories();
        Platform.runLater(() -> {
            syncAlwaysOnTop();
            updateLabel(cndConnectedLbl, isConnected);
            updateLabel(cndRoomLbl, floorState.inRoom());
            updateLabel(cndFurnidataLbl, furniDataReady());
            updateLabel(cndInventoryLbl, inventory.getState() == Inventory.InventoryState.LOADED,
                    inventory.getState() == Inventory.InventoryState.LOADING);
            updateLabel(cndStackTileLbl, stackTile() != null);
            updateLabel(cndPermissionsLbl, permissions.canMoveFurni() && (noExportWiredCbx.isSelected() || permissions.canModifyWired()));

            availabilityBtn.setDisable(presetListView.getSelectionModel().getSelectedItem() == null
                    && importer.getPresetConfig() == null);
            selfDonateBtn.setDisable(importer.getPresetConfig() == null);
            currentPresetBtn.setDisable(presetListView.getSelectionModel().getSelectedItem() == null);
            boolean noPresetSelected = presetListView.getSelectionModel().getSelectedItem() == null;
            boolean busy = cloneOrchestrator != null && cloneOrchestrator.isRunning();
            renamePresetBtn.setDisable(noPresetSelected || busy);
            editPresetBtn.setDisable(noPresetSelected || busy);
            deletePresetBtn.setDisable(noPresetSelected || busy);
            buildPresetBtn.setDisable(presetListView.getSelectionModel().getSelectedItem() == null);
            presetToNewRoomBtn.setDisable(presetListView.getSelectionModel().getSelectedItem() == null
                    || cloneOrchestrator == null || cloneOrchestrator.isRunning() || !isConnected);

            boolean cloning = cloneOrchestrator != null && cloneOrchestrator.isRunning();
            boolean cloneBlocked = cloning || !isConnected || !floorState.inRoom() || !furniDataReady();
            cloneBtn.setDisable(cloneBlocked);
            cloneBuildBtn.setDisable(cloneBlocked);
            cancelCloneBtn.setDisable(!cloning);
            loadInventoryBtn.setDisable(inventory.getState() == Inventory.InventoryState.LOADING);
        });
    }

    public void updateInstalledPresets() {
        List<String> installed = PresetConfigUtils.listPresets();
        Platform.runLater(() -> {
            presetListView.getItems().clear();
            presetListView.getItems().addAll(installed);
        });
    }

    public void sendVisualChatInfo(String text) {
        sendToClient(new HPacket("Whisper", HMessage.Direction.TOCLIENT, -1, text, 0, 30, 0, -1));
    }

    public void renamePresetClick(ActionEvent actionEvent) {
        String selected = presetListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TextField input = new TextField(selected);
        input.setPrefColumnCount(40);
        input.setMinHeight(28);
        Label current = new Label(Messages.get("preset.rename.header", selected));
        current.setWrapText(true);
        current.setMaxWidth(460);
        Label caption = new Label(Messages.get("preset.rename.label"));
        caption.setMinHeight(26);
        VBox content = new VBox(8, current, caption, input);

        StyledDialog dialog = new StyledDialog(primaryStage,
                Messages.get("preset.rename.title"), content, 520, 0);
        dialog.setSaveText(Messages.get("ui.presets.rename"));
        dialog.setValidator(() -> {
            String target = input.getText().trim();
            if (target.isEmpty() || target.equals(selected)) {
                return Messages.get("preset.rename.unchanged");
            }
            if (!PresetConfigUtils.isValidPresetName(target)) {
                return Messages.get("preset.rename.invalid", target);
            }
            if (PresetConfigUtils.presetExists(target)) {
                return Messages.get("preset.rename.exists", target);
            }
            return null;
        });

        if (!dialog.showAndWaitConfirmed()) {
            return;
        }
        String target = input.getText().trim();
        if (PresetConfigUtils.renamePreset(selected, target)) {
            logger.logKey("preset.rename.done", "green", selected, target);
            updateInstalledPresets();
        } else {
            logger.logKey("preset.rename.failed", "red", selected);
        }
    }

    public void deletePresetClick(ActionEvent actionEvent) {
        String selected = presetListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Label question = new Label(Messages.get("preset.delete.header", selected));
        question.setWrapText(true);
        question.setMaxWidth(420);
        Label body = new Label(Messages.get("preset.delete.body"));
        body.setWrapText(true);
        body.setMaxWidth(420);
        VBox content = new VBox(10, question, body);

        StyledDialog dialog = new StyledDialog(primaryStage,
                Messages.get("preset.delete.title"), content, 480, 0);
        dialog.setSaveText(Messages.get("ui.presets.delete"));
        if (!dialog.showAndWaitConfirmed()) {
            return;
        }
        if (PresetConfigUtils.deletePreset(selected)) {
            logger.logKey("preset.delete.done", "green", selected);
            updateInstalledPresets();
        } else {
            logger.logKey("preset.delete.failed", "red", selected);
        }
    }

    public void editPresetClick(ActionEvent actionEvent) {
        String selected = presetListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        File file = new File(PresetConfigUtils.presetPath(), selected + PresetConfigUtils.ROOM_EXT);
        JSONObject root = new JSONObject();
        if (file.isFile()) {
            try {
                root = new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            } catch (Throwable t) {
                logger.logKey("preset.editor.unreadable", "red", file.getName(), t);
                return;
            }
        }

        JSONObject settings = root.optJSONObject("roomSettings");
        if (settings == null) {
            settings = seedRoomSettings(root.optJSONObject("roomData"), selected);
        }

        if (!new PresetSettingsDialog(settings).show(primaryStage, selected)) {
            return;
        }

        root.put("roomSettings", settings);
        try (Writer writer = new OutputStreamWriter(
                Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            writer.write(root.toString(4));
            writer.flush();
            logger.logKey("preset.editor.saved", "green", selected);
        } catch (IOException e) {
            logger.logKey("preset.editor.save_failed", "red", e);
        }
    }

    private static JSONObject seedRoomSettings(JSONObject roomData, String presetName) {
        JSONObject settings = new JSONObject();
        if (roomData == null) {
            settings.put("name", presetName);
            return settings;
        }
        settings.put("name", roomData.optString("name", presetName));
        settings.put("description", roomData.optString("description", ""));
        settings.put("categoryId", roomData.optInt("category", 0));
        settings.put("maximumVisitors", roomData.optInt("maxUsers", 25));
        settings.put("tradeMode", roomData.optInt("tradingMode", 0));
        settings.put("doorMode", roomData.optInt("lockType", 0));
        settings.put("allowPets", roomData.optBoolean("allowPets", false));
        settings.put("hideWalls", roomData.optBoolean("wallsHidden", false));
        settings.put("wallThickness", roomData.optInt("wallThickness", 0));
        settings.put("floorThickness", roomData.optInt("floorThickness", 0));
        settings.put("chatFloodSensitivity", roomData.optInt("floodMode", 1));
        settings.put("whoCanMute", roomData.optInt("muteRights", 0));
        settings.put("whoCanKick", roomData.optInt("kickRights", 0));
        settings.put("whoCanBan", roomData.optInt("banRights", 0));
        return settings;
    }

    public void reloadRoomClick(ActionEvent actionEvent) {
        if (cloneOrchestrator == null || cloneOrchestrator.isRunning()) {
            logger.logKey("clone.already_running", "red");
            return;
        }
        cloneOrchestrator.reloadCurrentRoom();
    }

    public void loadInventoryClick(ActionEvent actionEvent) {
        inventory.requestInventory();
    }

    public FurniDataTools getFurniDataTools() {
        return furniDataTools;
    }

    public Logger getLogger() {
        return logger;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public FloorState getFloorState() {
        return floorState;
    }

    public RoomPermissions getPermissions() {
        return permissions;
    }

    public StackTileSetting getStackTileSetting() {
        return stackTileSetting;
    }

    public int getSafeFeedbackTimeout() {
        return (int)((ping + pingVariation + 10) * 3);
    }

    private void updatePostConfig() {
        importer.setPostConfig(createPostConfig());
        updateUI();
    }

    public void refreshPostConfig() {
        importer.setPostConfig(createPostConfig());
    }

    public ItemSource getItemSource() {
        Object userData = item_src_tgl.getSelectedToggle().getUserData();
        return ItemSource.valueOf((String) userData);
    }

    public void copyRoomOnlyClick(ActionEvent actionEvent) {
        startClone(false);
    }

    public void cloneRoomClick(ActionEvent actionEvent) {
        startClone(true);
    }

    private void startClone(boolean buildAfterExport) {
        if (cloneOrchestrator.isRunning()) {
            logger.logKey("clone.already_running", "red");
            return;
        }
        if (exporter.getState() != GPresetExporter.PresetExportState.NONE
                || importer.getState() != GPresetImporter.BuildingImportState.NONE) {
            logger.logKey("clone.finish_pending_first", "red");
            return;
        }

        String model = roomModel_txt.getText();
        String suffix = nameSuffix_txt.getText();
        cloneOrchestrator.setRoomModel(model);
        cloneOrchestrator.setNameSuffix(suffix);
        cloneOrchestrator.setCopyWallItems(copyWallItemsCbx.isSelected());
        cloneOrchestrator.setUseWorkAnnex(workAnnexCbx.isSelected());

        SettingsCache.put("roomModel", model);
        SettingsCache.put("nameSuffix", suffix);
        SettingsCache.put("copyWallItems", copyWallItemsCbx.isSelected());
        SettingsCache.put("workAnnex", workAnnexCbx.isSelected());

        setCloneStatus(Messages.get("clone.status.running"), "status-busy");
        cloneOrchestrator.start(buildAfterExport, success -> Platform.runLater(() -> {
            setCloneStatus(success ? Messages.get("clone.status.done") : Messages.get("clone.status.aborted"),
                    success ? "status-ok" : "status-failed");
            updateUI();
        }));
        updateUI();
    }

    public void cancelCloneClick(ActionEvent actionEvent) {
        cloneOrchestrator.cancel();
        setCloneStatus(Messages.get("clone.status.cancel_requested"), "status-busy");
    }

    public void buildSelectedPresetClick(ActionEvent actionEvent) {
        String presetName = presetListView.getSelectionModel().getSelectedItem();
        if (presetName == null) {
            logger.logKey("preset.none_selected", "red");
            return;
        }
        PresetConfig preset = PresetConfigUtils.loadPreset(presetName);
        if (preset == null) {
            logger.logKey("preset.load_failed.named", "red", presetName);
            return;
        }
        selectPreset(preset, presetName);
        logger.logKey("preset.hint.build_command", "purple");
    }

    public void presetToNewRoomClick(ActionEvent actionEvent) {
        String presetName = presetListView.getSelectionModel().getSelectedItem();
        if (presetName == null) {
            logger.logKey("preset.none_selected", "red");
            return;
        }
        if (cloneOrchestrator.isRunning()) {
            logger.logKey("clone.already_running", "red");
            return;
        }
        if (exporter.getState() != GPresetExporter.PresetExportState.NONE
                || importer.getState() != GPresetImporter.BuildingImportState.NONE) {
            logger.logKey("clone.finish_pending_first", "red");
            return;
        }

        cloneOrchestrator.setRoomModel(roomModel_txt.getText());
        cloneOrchestrator.setNameSuffix(nameSuffix_txt.getText());
        cloneOrchestrator.setUseWorkAnnex(workAnnexCbx.isSelected());

        setCloneStatus(Messages.get("clone.status.running"), "status-busy");
        cloneOrchestrator.startPresetToNewRoom(presetName, success -> Platform.runLater(() -> {
            setCloneStatus(success ? Messages.get("clone.status.done") : Messages.get("clone.status.aborted"),
                    success ? "status-ok" : "status-failed");
            updateUI();
        }));
        updateUI();
    }

    private void applyTexts() {
        cloneTab.setText(Messages.get("ui.tab.clone"));
        presetsTab.setText(Messages.get("ui.tab.presets"));
        settingsTab.setText(Messages.get("ui.tab.settings"));
        presetConfigTab.setText(Messages.get("ui.tab.presetconfig"));

        cndConnectedLbl.setText(Messages.get("ui.status.connected"));
        cndRoomLbl.setText(Messages.get("ui.status.room"));
        cndFurnidataLbl.setText(Messages.get("ui.status.furnidata"));
        cndInventoryLbl.setText(Messages.get("ui.status.inventory"));
        cndStackTileLbl.setText(Messages.get("ui.status.stacktile"));
        cndPermissionsLbl.setText(Messages.get("ui.status.permissions"));

        loadInventoryBtn.setText(Messages.get("ui.button.loadinventory"));
        reloadRoomBtn.setText(Messages.get("ui.button.reloadroom"));
        cloneBtn.setText(Messages.get("ui.button.copyroom"));
        cloneBuildBtn.setText(Messages.get("ui.button.cloneroom"));
        cancelCloneBtn.setText(Messages.get("ui.button.cancel"));
        roomModelLbl.setText(Messages.get("ui.label.roommodel"));
        nameSuffixLbl.setText(Messages.get("ui.label.namesuffix"));
        copyWallItemsCbx.setText(Messages.get("ui.checkbox.copywallitems"));
        workAnnexCbx.setText(Messages.get("ui.checkbox.workannex"));
        workAnnexHintLbl.setText(Messages.get("ui.hint.workannex"));

        savedPresetsLbl.setText(Messages.get("ui.label.savedpresets"));
        availabilityBtn.setText(Messages.get("ui.button.checkavailability"));
        selfDonateBtn.setText(Messages.get("ui.button.selfdonate"));
        reloadPresetsBtn.setText(Messages.get("ui.button.reloadpresets"));
        openPresetsFolderBtn.setText(Messages.get("ui.button.openpresetsfolder"));
        currentPresetBtn.setText(Messages.get("ui.button.openpreset"));
        clearWiredBtn.setText(Messages.get("ui.button.clearwiredcache"));
        renamePresetBtn.setText(Messages.get("ui.presets.rename"));
        editPresetBtn.setText(Messages.get("ui.presets.edit"));
        deletePresetBtn.setText(Messages.get("ui.presets.delete"));
        onTopCloneCbx.setText(Messages.get("ui.checkbox.alwaysontop"));
        buildPresetBtn.setText(Messages.get("ui.button.buildpreset"));
        buildPresetHintLbl.setText(Messages.get("ui.label.buildpresethint"));
        presetToNewRoomBtn.setText(Messages.get("ui.button.presettonewroom"));
        presetToNewRoomHintLbl.setText(Messages.get("ui.label.presettonewroomhint"));

        mainStackTileLbl.setText(Messages.get("ui.label.mainstacktile"));
        itemSourceLbl.setText(Messages.get("ui.label.itemsource"));
        onlyInvCbx.setText(Messages.get("ui.radio.onlyinventory"));
        preferInvCbx.setText(Messages.get("ui.radio.preferinventory"));
        preferBcCbx.setText(Messages.get("ui.radio.preferbc"));
        onlyBcCbx.setText(Messages.get("ui.radio.onlybc"));
        ratelimitLbl.setText(Messages.get("ui.label.ratelimit"));
        languageLbl.setText(Messages.get("ui.label.language"));
        langEn.setText(Messages.get("ui.language.en"));
        langDe.setText(Messages.get("ui.language.de"));
        donateLabel.setText(Messages.get("ui.label.selfdonate"));
        donateMissing.setText(Messages.get("ui.radio.donatemissing"));
        donateAll.setText(Messages.get("ui.radio.donateall"));
        noExportWiredCbx.setText(Messages.get("ui.checkbox.noexportwired"));
        allowIncompleteBuildsCbx.setText(Messages.get("ui.checkbox.allowincomplete"));

        updatePostconfigBtn.setText(Messages.get("ui.button.update"));
        furniNameLbl.setText(Messages.get("ui.label.furniname"));
        existingFurniLbl.setText(Messages.get("ui.label.existingfurni"));
        if (postconfigTable != null && postconfigTable.getColumns().size() >= 2) {
            postconfigTable.getColumns().get(0).setText(Messages.get("ui.postconfig.column.furniname"));
            postconfigTable.getColumns().get(1).setText(Messages.get("ui.postconfig.column.existingfurniid"));
        }

        if (!cloneOrchestratorRunning()) {
            cloneStatusLbl.setText(Messages.get("clone.status.ready"));
        }

        if (primaryStage != null) {
            primaryStage.setTitle(Messages.get("ui.window.title",
                    GRoomCloner.class.getAnnotation(ExtensionInfo.class).Version()));
        }
    }

    private boolean cloneOrchestratorRunning() {
        return cloneOrchestrator != null && cloneOrchestrator.isRunning();
    }

    private void setupLanguage() {
        Messages.Language cached = Messages.Language.fromName(
                SettingsCache.getCacheContents().optString("language", "EN"), Messages.Language.EN);
        Messages.setLanguage(cached);
        (cached == Messages.Language.DE ? langDe : langEn).setSelected(true);

        lang_tgl.selectedToggleProperty().addListener(observable -> {
            Object data = lang_tgl.getSelectedToggle() == null
                    ? null : lang_tgl.getSelectedToggle().getUserData();
            Messages.Language chosen = Messages.Language.fromName((String) data, Messages.Language.EN);
            String previousDefault = Messages.get("settings.namesuffix.default");
            SettingsCache.put("language", chosen.name());
            Messages.setLanguage(chosen);

            if (previousDefault.equals(nameSuffix_txt.getText())) {
                String followed = Messages.get("settings.namesuffix.default");
                nameSuffix_txt.setText(followed);
                SettingsCache.put("nameSuffix", followed);
            }
        });

        Messages.onLanguageChange(() -> Platform.runLater(() -> {
            applyTexts();
            logger.retranslate();
        }));
    }

    private void setCloneStatus(String text) {
        setCloneStatus(text, "status-ok");
    }

    private void setCloneStatus(String text, String state) {
        Platform.runLater(() -> {
            cloneStatusLbl.setText(text);
            cloneStatusLbl.getStyleClass().removeAll("status-ok", "status-busy", "status-failed");
            cloneStatusLbl.getStyleClass().add(state);
        });
    }

    private PostConfig createPostConfig() {
        PostConfig postConfig = new PostConfig();

        ItemSource itemSource = ItemSource.valueOf((String) item_src_tgl.getSelectedToggle().getUserData());
        SettingsCache.put("itemSource", itemSource);

        postConfig.setItemSource(itemSource);

        furniPostConfigs.forEach(postConfig::addFurniPostConfig);
        return postConfig;
    }

    public void availabilityBtnClick(ActionEvent actionEvent) {
        ensureSelectedPresetLoaded();
        PresetConfig presetConfig = importer.getPresetConfig();
        PostConfig postConfig = createPostConfig();

        if (presetConfig != null && furniDataReady()) {
            PresetConfig combined = new PresetConfig(presetConfig.toJsonObject());
            combined.applyPostConfig(postConfig);

            List<FurniDropInfo> fakeDropInfo = new ArrayList<>();
            for (PresetFurni f : combined.getFurniture()) {
                fakeDropInfo.add(new FurniDropInfo(-1, -1, furniDataTools.getFloorTypeId(f.getClassName()), postConfig.getItemSource(), -1));
            }

            AvailabilityChecker.printAvailability(logger, fakeDropInfo, inventory, furniDataTools);
        }
        else {
            logger.logKey("preset.availability.not_ready", "red");
        }

    }

    public void selfDonateBtnClick(ActionEvent actionEvent) {
        if (exporter.getState() != GPresetExporter.PresetExportState.NONE) {
            logger.logKey("preset.donate.busy.export", "red");
            return;
        }
        if (importer.getState() != GPresetImporter.BuildingImportState.NONE) {
            logger.logKey("preset.donate.busy.import", "red");
            return;
        }

        autoDonator.donateAll(donateMissing.isSelected());
    }

    public void showSelfDonateBtn(boolean show) {
        selfDonateBtn.setVisible(show);
        donateLabel.setVisible(show);
        donateAll.setVisible(show);
        donateMissing.setVisible(show);
        presetListView.setPrefHeight(show ? 170.0 : 204.0);
    }

    public void openPresetsFolderClick(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(new File(PresetConfigUtils.presetPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openCurrentPresetClick(ActionEvent actionEvent) {
        String selectedPreset = presetListView.getSelectionModel().getSelectedItem();
        if (selectedPreset == null) {
            logger.logKey("preset.none_selected", "red");
            return;
        }

        File file = new File(PresetConfigUtils.presetPath(), selectedPreset + PresetConfigUtils.PRESET_EXT);
        if (!file.isFile()) {
            logger.logKey("preset.file_missing", "red", file.getName());
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            logger.logKey("preset.open.unsupported", "orange", file.getParent());
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.EDIT)) {
            try {
                desktop.edit(file);
                return;
            } catch (Throwable ignored) {
            }
        }
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            try {
                desktop.open(file);
                return;
            } catch (Throwable ignored) {
            }
        }
        try {
            desktop.open(file.getParentFile());
            logger.logKey("preset.open.folder_instead", "orange", file.getName());
        } catch (Throwable t) {
            logger.logKey("preset.open.failed", "red", file.getAbsolutePath());
        }
    }

    public void reloadPresetsClick(ActionEvent actionEvent) {
        updateInstalledPresets();
        updateUI();
    }

    public boolean allowIncompleteBuilds() {
        return allowIncompleteBuildsCbx.isSelected();
    }

    private void updateFurniPostConfigsView() {
        Platform.runLater(() -> {
            postconfigTable.getItems().clear();
            postconfigTable.getItems().addAll(furniPostConfigs);
        });
        updateUI();
    }

    public void updatePostconfigClick(ActionEvent actionEvent) {
        try {
            if (furniNamePC_txt.getText().isEmpty() || replacementIdPC_txt.getText().isEmpty()) throw new Exception();

            FurniPostConfig furniPostConfig = new FurniPostConfig(
                    furniNamePC_txt.getText(),
                    true, Integer.parseInt(replacementIdPC_txt.getText()),
                    null, null, null, null, new HashMap<>(), new HashMap<>()
            );
            furniPostConfigs = furniPostConfigs.stream().filter(c -> !c.getFurniIdentifier().equals(furniPostConfig.getFurniIdentifier())).collect(Collectors.toList());
            furniPostConfigs.add(furniPostConfig);
            updatePostConfig();
            updateFurniPostConfigsView();
            Platform.runLater(() -> postconfigErrorLbl.setText(""));
        }
        catch (Exception e) {
//            e.printStackTrace();
            Platform.runLater(() -> postconfigErrorLbl.setText(Messages.get("ui.postconfig.invalidinput")));
        }
    }

    public boolean shouldExportWired() {
        return !noExportWiredCbx.isSelected();
    }

    public GPresetExporter getExporter() {
        return exporter;
    }

    public GPresetImporter getImporter() {
        return importer;
    }

    public void alwaysOnTopCloneClick(ActionEvent actionEvent) {
        applyAlwaysOnTop(onTopCloneCbx.isSelected());
    }

    public void clearWiredClick(ActionEvent actionEvent) {
        int cleared = exporter.clearCache();
        if (cleared < 0) {
            logger.logKey("wired.cache.busy", "orange");
        } else {
            logger.logKey("wired.cache.cleared", "green", cleared);
        }
    }
}
