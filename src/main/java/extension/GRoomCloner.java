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
import javafx.stage.FileChooser;
import org.json.JSONObject;
import extension.ui.PresetImportDialog;
import extension.ui.PresetSettingsDialog;
import extension.ui.StyledDialog;
import roomcopy.BuildEstimate;
import roomcopy.CloneOrchestrator;
import roomcopy.FlatCategories;
import roomcopy.StackTileBootstrap;
import roomcopy.FloorPlanText;
import roomcopy.RoomSettingsSnapshot;
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
        Version =  "1.1.2",
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
    public CheckBox workAnnexHereCbx;
    public CheckBox presetPlanHereCbx;
    public CheckBox autoStackTileCbx;
    public TextField buildHereXY_txt;
    public Label cloneStatusLbl;

    public TabPane mainTabs;
    public Tab cloneTab;
    public Tab presetsTab;
    public Tab settingsTab;
    public Tab presetConfigTab;

    public Button loadInventoryBtn;
    public Button reloadRoomBtn;
    public Button reloadPresetsBtn;
    public Button importPresetBtn;
    public Button openPresetsFolderBtn;
    public Button clearWiredBtn;
    public Button updatePostconfigBtn;

    public Label roomModelLbl;
    public Label workAnnexHintLbl;
    public Label nameSuffixLbl;
    public Label savedPresetsLbl;
    public Label buildPresetHintLbl;
    public Label presetToNewRoomHintLbl;
    public Label selectedPresetLbl;
    public Label buildHerePosLbl;
    public Label autoStackTileHintLbl;
    public Label workAnnexHereHintLbl;
    public Label presetPlanHereHintLbl;
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

        autoStackTileCbx.selectedProperty().addListener(observable ->
                SettingsCache.put("autoStackTile", autoStackTileCbx.isSelected())
        );

        workAnnexHereCbx.selectedProperty().addListener(observable ->
                SettingsCache.put("workAnnexHere", workAnnexHereCbx.isSelected())
        );

        presetPlanHereCbx.selectedProperty().addListener(observable ->
                SettingsCache.put("presetPlanHere", presetPlanHereCbx.isSelected())
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
        setupPresetCells();
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
        workAnnexHereCbx.setSelected(cache.optBoolean("workAnnexHere", true));
        presetPlanHereCbx.setSelected(cache.optBoolean("presetPlanHere", false));
        autoStackTileCbx.setSelected(cache.optBoolean("autoStackTile", true));

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

    private Object[] selectedPresetSummary = null;

    private void selectPreset(PresetConfig preset, String name) {
        loadedPresetName = name;
        logger.logKey("preset.selected.log", "green", name);
        importer.setPresetConfig(preset);
        HPoint dim = PresetUtils.presetDimensions(preset);
        logger.logKey("preset.dimensions", "green", dim.getX(), dim.getY());
        int furniCount = preset.getFurniture() == null ? 0 : preset.getFurniture().size();
        selectedPresetSummary = new Object[] { name, dim.getX(), dim.getY(), furniCount };
        Platform.runLater(() -> {
            refreshSelectedPresetLabel();
            presetListView.refresh();
        });
        updateUI();
    }

    private void refreshSelectedPresetLabel() {
        if (selectedPresetLbl == null) {
            return;
        }
        if (selectedPresetSummary == null) {
            selectedPresetLbl.setText(Messages.get("preset.selected.none"));
            selectedPresetLbl.getStyleClass().remove("hint-box-ok");
            if (!selectedPresetLbl.getStyleClass().contains("hint-box")) {
                selectedPresetLbl.getStyleClass().add("hint-box");
            }
            return;
        }
        selectedPresetLbl.setText(Messages.get("preset.selected.summary", selectedPresetSummary));
        selectedPresetLbl.getStyleClass().remove("hint-box");
        if (!selectedPresetLbl.getStyleClass().contains("hint-box-ok")) {
            selectedPresetLbl.getStyleClass().add("hint-box-ok");
        }
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

    private void setupPresetCells() {
        presetListView.setCellFactory(view -> new ListCell<String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    return;
                }
                PresetConfigUtils.Counts counts = PresetConfigUtils.counts(name);
                if (counts == null) {
                    setText(name);
                } else if (counts.wired > 0) {
                    setText(Messages.get("preset.list.entry_wired", name, counts.furni, counts.wired));
                } else {
                    setText(Messages.get("preset.list.entry", name, counts.furni));
                }

                boolean loaded = name.equals(loadedPresetName);
                getStyleClass().remove("preset-loaded");
                if (loaded) {
                    getStyleClass().add("preset-loaded");
                }
            }
        });
    }

    public void sendVisualChatInfo(String text) {
        String wireText = new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        sendToClient(new HPacket("Whisper", HMessage.Direction.TOCLIENT, -1, wireText, 0, 30, 0, -1));
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
        if (importer.getState() != GPresetImporter.BuildingImportState.NONE) {
            logger.logKey("preset.import.already_running.log", "red");
            return;
        }
        if (cloneOrchestrator.isRunning()) {
            logger.logKey("clone.already_running", "red");
            return;
        }
        if (!floorState.inRoom()) {
            logger.logKey("preset.import.not_ready.no_room", "red");
            return;
        }
        if (permissions.furniExplicitlyDenied()) {
            logger.logKey("preset.import.not_ready.no_furni_rights", "red");
            return;
        }

        String rawPosition = buildHereXY_txt == null ? "" : buildHereXY_txt.getText().trim();
        HPoint requestedRoot = parseBuildHereRoot(rawPosition);
        if (requestedRoot == null && !rawPosition.isEmpty()) {
            logger.logKey("buildhere.bad_position", "red");
            return;
        }

        selectPreset(preset, presetName);

        if (workAnnexHereCbx != null && workAnnexHereCbx.isSelected()) {
            boolean adoptPlan = presetPlanHereCbx != null && presetPlanHereCbx.isSelected();
            cloneOrchestrator.startBuildHere(presetName, requestedRoot, adoptPlan,
                    success -> Platform.runLater(this::updateUI));
            return;
        }

        boolean autoTile = autoStackTileCbx == null || autoStackTileCbx.isSelected();
        new Thread(() -> runBuildHere(preset, requestedRoot, autoTile)).start();
    }

    private HPoint parseBuildHereRoot(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            String[] parts = raw.split("[,; ]+");
            if (parts.length < 2) return null;
            return new HPoint(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private void runBuildHere(PresetConfig preset, HPoint requestedRoot, boolean autoTile) {
        List<Integer> temporaryTiles = new ArrayList<>();
        try {
            if (inventory.getState() != Inventory.InventoryState.LOADED) {
                logger.logKey("buildhere.loading_inventory", "blue");
                inventory.requestInventory();
                long deadline = System.currentTimeMillis() + 15000;
                while (inventory.getState() != Inventory.InventoryState.LOADED
                        && System.currentTimeMillis() < deadline) {
                    Utils.sleep(200);
                }
                if (inventory.getState() != Inventory.InventoryState.LOADED) {
                    logger.logKey("preset.import.not_ready.no_inventory", "red");
                    return;
                }
            }

            int dimension = Math.max(1, stackTileSetting.getDimension());
            HPoint presetSize = PresetUtils.presetDimensions(preset);
            int presetWidth = Math.max(1, presetSize.getX());
            int presetHeight = Math.max(1, presetSize.getY());

            java.util.Set<Long> occupied = new java.util.HashSet<>();
            HFloorItem presentTile = stackTile();
            if (presentTile != null) {
                markSquare(occupied, presentTile.getTile().getX(), presentTile.getTile().getY(), dimension);
            }

            HPoint root = requestedRoot;
            if (root == null) {
                root = floorState.findFreeArea(presetWidth, presetHeight, occupied);
                if (root == null) {
                    logger.logKey("buildhere.no_area", "red", presetWidth, presetHeight);
                    return;
                }
            }
            else if (floorState.floorHeight(root.getX(), root.getY()) == 'x') {
                logger.logKey("buildhere.position_outside", "red", root.getX(), root.getY());
                return;
            }

            java.util.Set<Long> footprint = new java.util.HashSet<>();
            for (int dx = 0; dx < presetWidth; dx++) {
                for (int dy = 0; dy < presetHeight; dy++) {
                    footprint.add(tileKey(root.getX() + dx, root.getY() + dy));
                }
            }

            if (presentTile == null) {
                if (!autoTile) {
                    logger.logKey("buildhere.no_stacktile_manual", "red");
                    return;
                }
                HPoint tileSpot = floorState.findFreeSquare(dimension, footprint);
                if (tileSpot == null) {
                    logger.logKey("buildhere.no_space_for_stacktile", "red");
                    return;
                }
                int placedTileId = new StackTileBootstrap(executor, logger).ensureStackTile(
                        stackTileSetting, getItemSource(), floorState, inventory,
                        furniDataTools, tileSpot);
                if (placedTileId == StackTileBootstrap.FAILED) {
                    logger.logKey("buildhere.stacktile_failed", "red");
                    return;
                }
                if (placedTileId > 0) {
                    temporaryTiles.add(placedTileId);
                }
                Utils.sleep(300);
            }

            HFloorItem tile = stackTile();
            if (tile == null) {
                logger.logKey("preset.import.not_ready.no_stacktile", "red");
                return;
            }

            int tileX = tile.getTile().getX();
            int tileY = tile.getTile().getY();

            java.util.Set<Long> avoid = new java.util.HashSet<>(footprint);
            markSquare(avoid, tileX, tileY, dimension);

            if (autoTile) {
                temporaryTiles.addAll(placeHelperStackTiles(dimension, avoid));
            }

            HPoint reserved = floorState.findFreeSquare(1, avoid);
            if (reserved == null) {
                logger.logKey("buildhere.no_reserved_space", "red");
                return;
            }

            logger.logKey("buildhere.starting", "blue", root.getX(), root.getY(),
                    reserved.getX(), reserved.getY());

            refreshPostConfig();
            if (!importer.startImport(reserved, root, null, new HPoint(tileX, tileY))) {
                return;
            }

            long lastChange = System.currentTimeMillis();
            GPresetImporter.BuildingImportState last = importer.getState();
            while (importer.getState() != GPresetImporter.BuildingImportState.NONE) {
                GPresetImporter.BuildingImportState now = importer.getState();
                if (now != last) {
                    last = now;
                    lastChange = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - lastChange > 120000) {
                    logger.logKey("buildhere.timeout", "orange");
                    break;
                }
                Utils.sleep(250);
            }

            boolean ok = importer.lastImportSucceeded();
            logger.logKey(ok ? "buildhere.done" : "buildhere.failed", ok ? "green" : "orange");
        }
        catch (Exception e) {
            logger.logKey("buildhere.error", "red", e.getClass().getSimpleName() + " " + e.getMessage());
        }
        finally {
            for (Integer id : temporaryTiles) {
                pickupTemporaryStackTile(id);
            }
            Platform.runLater(this::updateUI);
        }
    }

    private static Long tileKey(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    private static void markSquare(java.util.Set<Long> target, int x0, int y0, int dimension) {
        for (int dx = 0; dx < dimension; dx++) {
            for (int dy = 0; dy < dimension; dy++) {
                target.add(tileKey(x0 + dx, y0 + dy));
            }
        }
    }

    private List<Integer> placeHelperStackTiles(int mainDimension, java.util.Set<Long> blocked) {
        List<Integer> placed = new ArrayList<>();
        if (mainDimension <= 1) {
            return placed;
        }

        java.util.Set<Long> taken = new java.util.HashSet<>(blocked);
        if (mainDimension > 2) {
            addHelperStackTile(placed, taken, StackTileSetting.Large, 2);
        }
        addHelperStackTile(placed, taken, StackTileSetting.Small, 1);

        if (!placed.isEmpty()) {
            logger.logKey("stacktile.helpers_placed", "green", placed.size());
        }
        return placed;
    }

    private void addHelperStackTile(List<Integer> placed, java.util.Set<Long> taken,
                                    StackTileSetting setting, int dimension) {
        HPoint spot = floorState.findFreeSquare(dimension, taken);
        if (spot == null) {
            logger.logKey("stacktile.helper_no_space", "orange", setting.toString());
            return;
        }
        int id = new StackTileBootstrap(executor, logger).ensureStackTile(setting, getItemSource(),
                floorState, inventory, furniDataTools, spot);
        if (id == StackTileBootstrap.FAILED) {
            logger.logKey("stacktile.helper_unavailable", "orange", setting.toString());
            return;
        }
        markSquare(taken, spot.getX(), spot.getY(), dimension);
        if (id > 0) {
            placed.add(id);
        }
    }

    private void pickupTemporaryStackTile(int stackTileId) {
        try {
            Utils.sleep(400);
            if (!executor.sendToServer("PickupObject", 2, stackTileId, false)) {
                logger.logKey("stacktile.pickup.send_failed", "orange");
                return;
            }
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline) {
                boolean stillThere = false;
                for (HFloorItem item : floorState.getItemsFromType(furniDataTools, stackTileSetting.getClassName())) {
                    if (item.getId() == stackTileId) {
                        stillThere = true;
                        break;
                    }
                }
                if (!stillThere) {
                    logger.logKey("stacktile.pickup.done", "green");
                    return;
                }
                Utils.sleep(150);
            }
            logger.logKey("stacktile.pickup.still_present", "orange");
        }
        catch (Exception ignored) {
        }
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
        workAnnexHereCbx.setText(Messages.get("ui.checkbox.workannexhere"));
        presetPlanHereCbx.setText(Messages.get("ui.checkbox.presetplanhere"));
        autoStackTileCbx.setText(Messages.get("ui.checkbox.autostacktile"));
        autoStackTileHintLbl.setText(Messages.get("ui.hint.autostacktile"));
        workAnnexHereHintLbl.setText(Messages.get("ui.hint.workannexhere"));
        presetPlanHereHintLbl.setText(Messages.get("ui.hint.presetplanhere"));

        savedPresetsLbl.setText(Messages.get("ui.label.savedpresets"));
        availabilityBtn.setText(Messages.get("ui.button.checkavailability"));
        selfDonateBtn.setText(Messages.get("ui.button.selfdonate"));
        reloadPresetsBtn.setText(Messages.get("ui.button.reloadpresets"));
        importPresetBtn.setText(Messages.get("ui.button.importpreset"));
        openPresetsFolderBtn.setText(Messages.get("ui.button.openpresetsfolder"));
        currentPresetBtn.setText(Messages.get("ui.button.openpreset"));
        clearWiredBtn.setText(Messages.get("ui.button.clearwiredcache"));
        renamePresetBtn.setText(Messages.get("ui.presets.rename"));
        editPresetBtn.setText(Messages.get("ui.presets.edit"));
        deletePresetBtn.setText(Messages.get("ui.presets.delete"));
        onTopCloneCbx.setText(Messages.get("ui.checkbox.alwaysontop"));
        buildPresetBtn.setText(Messages.get("ui.button.buildpreset"));
        buildPresetHintLbl.setText(Messages.get("ui.label.buildpresethint"));
        buildHerePosLbl.setText(Messages.get("ui.label.buildhereposition"));
        refreshSelectedPresetLabel();
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
            logBuildEstimate(combined);
        }
        else {
            logger.logKey("preset.availability.not_ready", "red");
        }

    }

    private void logBuildEstimate(PresetConfig preset) {
        int furni = preset.getFurniture().size();
        int wired = 0;
        if (preset.getPresetWireds() != null) {
            wired = preset.getPresetWireds().getTriggers().size()
                    + preset.getPresetWireds().getConditions().size()
                    + preset.getPresetWireds().getEffects().size()
                    + preset.getPresetWireds().getSelectors().size()
                    + preset.getPresetWireds().getAddons().size()
                    + preset.getPresetWireds().getVariables().size();
        }

        int rateLimit = (int) ratelimiter.getValue();
        ItemSource itemSource = ItemSource.valueOf((String) item_src_tgl.getSelectedToggle().getUserData());
        boolean fromBc = itemSource == ItemSource.ONLY_BC || itemSource == ItemSource.PREFER_BC;

        BuildEstimate estimate = BuildEstimate.of(furni, wired, fromBc, rateLimit);

        logger.logKey("preset.estimate.contents", "blue", furni, wired);
        logger.logKey("preset.estimate.placing", "blue", BuildEstimate.format(estimate.dropMs));
        logger.logKey("preset.estimate.total", "green",
                BuildEstimate.format(estimate.totalMs), rateLimit,
                Messages.get(fromBc ? "preset.estimate.source.bc" : "preset.estimate.source.inventory"));
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

    public void importPresetClick(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(Messages.get("preset.import.choose"));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(Messages.get("preset.import.filter.preset"), "*.json"),
                new FileChooser.ExtensionFilter(Messages.get("preset.import.filter.all"), "*.*"));

        File file = chooser.showOpenDialog(primaryStage);
        if (file == null) {
            return;
        }

        String raw;
        PresetConfig config;
        try {
            raw = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            config = new PresetConfig(new JSONObject(raw));
        } catch (Throwable t) {
            logger.logKey("preset.import.error.unreadable", "red", file.getName(), t);
            return;
        }
        if (config.getFurniture().isEmpty()) {
            logger.logKey("preset.import.error.no_furni", "red", file.getName());
            return;
        }

        PresetImportDialog dialog = new PresetImportDialog(file, config.getFurniture().size());
        if (!dialog.show(primaryStage)) {
            return;
        }

        String name = PresetConfigUtils.uniqueName(dialog.getPresetName());
        if (!name.equals(dialog.getPresetName())) {
            logger.logKey("preset.name.numbered", "blue", dialog.getPresetName(), name);
        }

        File dir = new File(PresetConfigUtils.presetPath());
        dir.mkdirs();
        try (Writer writer = new OutputStreamWriter(
                Files.newOutputStream(new File(dir, name + PresetConfigUtils.PRESET_EXT).toPath()),
                StandardCharsets.UTF_8)) {
            writer.write(raw);
            writer.flush();
        } catch (IOException e) {
            logger.logKey("preset.import.error.write", "red", e);
            return;
        }

        boolean wantsPlan = dialog.getSnapshotFile() != null
                || !dialog.getFloorPlanText().trim().isEmpty();
        if (wantsPlan && !importFloorPlan(dir, name, dialog)) {
            logger.logKey("preset.import.floorplan.skipped", "orange");
        }

        logger.logKey("preset.import.done", "green", name, config.getFurniture().size());
        updateInstalledPresets();
        Platform.runLater(() -> presetListView.getSelectionModel().select(name));
    }

    private boolean importFloorPlan(File dir, String name, PresetImportDialog dialog) {
        JSONObject root = new JSONObject();
        File snapshot = dialog.getSnapshotFile();

        if (snapshot != null) {
            try {
                JSONObject existing = new JSONObject(
                        new String(Files.readAllBytes(snapshot.toPath()), StandardCharsets.UTF_8));
                if (existing.optJSONObject("floorPlan") == null) {
                    logger.logKey("preset.import.floorplan.no_plan", "orange", snapshot.getName());
                    return false;
                }
                root = existing;
                logger.logKey("preset.import.floorplan.snapshot", "blue", snapshot.getName());
            } catch (Throwable t) {
                logger.logKey("preset.import.floorplan.unreadable", "orange", snapshot.getName(), t);
                return false;
            }
        } else {
            FloorPlanText parsed = FloorPlanText.parse(dialog.getFloorPlanText());
            if (parsed == null) {
                logger.logKey("preset.import.floorplan.unparsable", "orange", name);
                return false;
            }
            root.put("roomData", RoomSettingsSnapshot.defaults(name).toJson());
            root.put("floorPlan", parsed.toFloorPlanJson());

            logger.logKey("preset.import.floorplan.parsed", "green",
                    parsed.width, parsed.height, parsed.walkableTiles,
                    parsed.doorX, parsed.doorY,
                    Messages.get("preset.import.door." + parsed.doorSource.name()));
            if (parsed.violatesDoorRule()) {
                logger.logKey("preset.import.floorplan.door_rule", "orange",
                        parsed.firstRowWalkable, parsed.firstColumnWalkable);
            }
        }

        try (Writer writer = new OutputStreamWriter(
                Files.newOutputStream(new File(dir, name + PresetConfigUtils.ROOM_EXT).toPath()),
                StandardCharsets.UTF_8)) {
            writer.write(root.toString(4));
            writer.flush();
            return true;
        } catch (IOException e) {
            logger.logKey("preset.import.error.write", "red", e);
            return false;
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
