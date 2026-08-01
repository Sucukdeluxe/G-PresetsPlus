package extension.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import roomcopy.FloorPlanText;
import utils.Messages;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PresetImportDialog {

    private static final double LABEL_COLUMN = 150;
    private static final double INPUT_WIDTH = 400;
    private static final double ROW_HEIGHT = 26;

    private final File presetFile;
    private final int furniCount;

    private final TextField nameField = new TextField();
    private final TextArea planArea = new TextArea();
    private final Label planStatus = new Label();
    private File snapshotFile = null;

    public PresetImportDialog(File presetFile, int furniCount) {
        this.presetFile = presetFile;
        this.furniCount = furniCount;
    }

    public String getPresetName() {
        return nameField.getText().trim();
    }

    public String getFloorPlanText() {
        return planArea.getText();
    }

    public File getSnapshotFile() {
        return snapshotFile;
    }

    public boolean show(Stage owner) {
        VBox root = buildContent(owner);
        StyledDialog dialog = new StyledDialog(owner, Messages.get("preset.import.title"), root, 640, 0);
        dialog.setSaveText(Messages.get("preset.import.save"));
        dialog.setValidator(() -> {
            String name = getPresetName();
            if (name.isEmpty()) {
                return Messages.get("preset.import.error.name_empty");
            }
            if (!extension.tools.presetconfig.PresetConfigUtils.isValidPresetName(name)) {
                return Messages.get("preset.rename.invalid", name);
            }
            if (snapshotFile == null && !planArea.getText().trim().isEmpty()
                    && FloorPlanText.parse(planArea.getText()) == null) {
                return Messages.get("preset.import.error.plan_invalid");
            }
            return null;
        });
        return dialog.showAndWaitConfirmed();
    }

    public VBox buildContent(Stage owner) {
        Label source = new Label(Messages.get("preset.import.source", presetFile.getName(), furniCount));
        source.setWrapText(true);
        source.setMaxWidth(INPUT_WIDTH + LABEL_COLUMN);
        source.setMinHeight(Region.USE_PREF_SIZE);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(4));
        ColumnConstraints labels = new ColumnConstraints();
        labels.setMinWidth(LABEL_COLUMN);
        labels.setPrefWidth(LABEL_COLUMN);
        ColumnConstraints inputs = new ColumnConstraints();
        inputs.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, inputs);

        nameField.setText(stripExtension(presetFile.getName()));
        nameField.setPrefWidth(INPUT_WIDTH);
        nameField.setMaxWidth(INPUT_WIDTH);
        nameField.setMinHeight(ROW_HEIGHT);
        grid.add(caption("preset.import.name"), 0, 0);
        grid.add(nameField, 1, 0);

        Button load = new Button(Messages.get("preset.import.floorplan.load"));
        load.setMinHeight(ROW_HEIGHT);
        Button clear = new Button(Messages.get("preset.import.floorplan.clear"));
        clear.setMinHeight(ROW_HEIGHT);
        HBox buttons = new HBox(8, load, clear);
        buttons.setMinHeight(ROW_HEIGHT);
        grid.add(caption("preset.import.floorplan"), 0, 1);
        grid.add(buttons, 1, 1);

        planArea.setPromptText(Messages.get("preset.import.floorplan.prompt"));
        planArea.setPrefRowCount(9);
        planArea.setPrefWidth(INPUT_WIDTH);
        planArea.setMaxWidth(INPUT_WIDTH);
        planArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 11px;");
        planArea.textProperty().addListener((observable, old, value) -> {
            if (snapshotFile == null) {
                updateStatus();
            }
        });
        grid.add(planArea, 1, 2);

        planStatus.setWrapText(true);
        planStatus.setMaxWidth(INPUT_WIDTH);
        planStatus.setMinHeight(Region.USE_PREF_SIZE);
        grid.add(planStatus, 1, 3);
        updateStatus();

        load.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(Messages.get("preset.import.floorplan.title"));
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(Messages.get("preset.import.filter.floorplan"),
                            "*.txt", "*.floor", "*.roomJson", "*.json"),
                    new FileChooser.ExtensionFilter(Messages.get("preset.import.filter.all"), "*.*"));
            if (presetFile.getParentFile() != null && presetFile.getParentFile().isDirectory()) {
                chooser.setInitialDirectory(presetFile.getParentFile());
            }
            File chosen = chooser.showOpenDialog(owner);
            if (chosen == null) {
                return;
            }
            String raw;
            try {
                raw = new String(Files.readAllBytes(chosen.toPath()), StandardCharsets.UTF_8);
            } catch (Throwable t) {
                snapshotFile = null;
                planArea.clear();
                setStatus("preset.import.status.unreadable", "orange", chosen.getName());
                return;
            }
            if (raw.trim().startsWith("{")) {
                snapshotFile = chosen;
                planArea.clear();
                planArea.setDisable(true);
                setStatus("preset.import.status.snapshot", "green", chosen.getName());
            } else {
                snapshotFile = null;
                planArea.setDisable(false);
                planArea.setText(raw);
                updateStatus();
            }
        });

        clear.setOnAction(event -> {
            snapshotFile = null;
            planArea.setDisable(false);
            planArea.clear();
            updateStatus();
        });

        Label note = new Label(Messages.get("preset.import.note"));
        note.setWrapText(true);
        note.setMaxWidth(INPUT_WIDTH + LABEL_COLUMN);
        note.setPrefWidth(INPUT_WIDTH + LABEL_COLUMN);
        note.setMinHeight(Region.USE_PREF_SIZE);
        note.getStyleClass().add("hint-box-solo");

        return new VBox(12, source, grid, note);
    }

    private void updateStatus() {
        String raw = planArea.getText();
        if (raw == null || raw.trim().isEmpty()) {
            setStatus("preset.import.status.empty", "grey");
            return;
        }
        FloorPlanText parsed = FloorPlanText.parse(raw);
        if (parsed == null) {
            setStatus("preset.import.status.invalid", "orange");
            return;
        }
        if (parsed.violatesDoorRule()) {
            setStatus("preset.import.status.door_rule", "orange",
                    parsed.width, parsed.height, parsed.walkableTiles,
                    parsed.firstRowWalkable, parsed.firstColumnWalkable);
            return;
        }
        setStatus("preset.import.status.ok", "green",
                parsed.width, parsed.height, parsed.walkableTiles,
                parsed.doorX, parsed.doorY,
                Messages.get("preset.import.door." + parsed.doorSource.name()));
    }

    private void setStatus(String key, String state, Object... args) {
        planStatus.setText(Messages.get(key, args));
        planStatus.getStyleClass().removeAll("status-ok", "status-busy", "status-failed");
        if ("green".equals(state)) {
            planStatus.getStyleClass().add("status-ok");
        } else if ("orange".equals(state)) {
            planStatus.getStyleClass().add("status-busy");
        }
    }

    private static Label caption(String key) {
        Label label = new Label(Messages.get(key));
        label.setMinHeight(ROW_HEIGHT);
        label.setWrapText(true);
        label.setMaxWidth(LABEL_COLUMN);
        return label;
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
