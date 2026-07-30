package extension.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import roomcopy.FlatCategories;
import utils.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PresetSettingsDialog {

    private static final double DIALOG_WIDTH = 640;
    private static final double DIALOG_HEIGHT = 560;
    private static final double LABEL_COLUMN = 200;
    private static final double INPUT_WIDTH = 260;
    private static final double NOTE_WIDTH = DIALOG_WIDTH - 90;
    private static final double ROW_HEIGHT = 26;

    private static class Choice {
        final int value;
        final String label;

        Choice(int value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final JSONObject settings;

    private final TextField nameField = new TextField();
    private final TextArea descriptionField = new TextArea();
    private final ComboBox<FlatCategories.Category> categoryBox = new ComboBox<>();
    private final TextField categoryFallback = new TextField();
    private final ComboBox<Choice> maxVisitorsBox = new ComboBox<>();
    private final ComboBox<Choice> tradeBox = new ComboBox<>();
    private final TextField tag1 = new TextField();
    private final TextField tag2 = new TextField();
    private final CheckBox walkThroughBox = new CheckBox();

    private final ToggleGroup doorGroup = new ToggleGroup();
    private final CheckBox petsBox = new CheckBox();
    private final CheckBox petsEatBox = new CheckBox();
    private final CheckBox mutePetsBox = new CheckBox();
    private final TextField doorPasswordField = new TextField();

    private final CheckBox hideWallsBox = new CheckBox();
    private final ComboBox<Choice> wallThicknessBox = new ComboBox<>();
    private final ComboBox<Choice> floorThicknessBox = new ComboBox<>();
    private final CheckBox leaveOnDoorBox = new CheckBox();
    private final CheckBox sleepBox = new CheckBox();
    private final TextField sleepSeconds = new TextField();
    private final CheckBox kickBox = new CheckBox();
    private final TextField kickSeconds = new TextField();
    private final ComboBox<Choice> floodBox = new ComboBox<>();

    private final ComboBox<Choice> muteRightsBox = new ComboBox<>();
    private final ComboBox<Choice> kickRightsBox = new ComboBox<>();
    private final ComboBox<Choice> banRightsBox = new ComboBox<>();

    public PresetSettingsDialog(JSONObject settings) {
        this.settings = settings;
    }

    public boolean show(Stage owner, String presetName) {
        VBox root = buildContent(presetName);
        StyledDialog dialog = new StyledDialog(owner,
                Messages.get("preset.editor.title"), root, DIALOG_WIDTH, DIALOG_HEIGHT);
        dialog.setValidator(this::validate);
        if (!dialog.showAndWaitConfirmed()) {
            return false;
        }
        store();
        return true;
    }

    public VBox buildContent(String presetName) {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("panel-frame");
        tabs.getTabs().addAll(
                tab("preset.editor.tab.basic", basicPane()),
                tab("preset.editor.tab.access", accessPane()),
                tab("preset.editor.tab.hc", hcPane()),
                tab("preset.editor.tab.mod", modPane()));

        Label header = new Label(Messages.get("preset.editor.header", presetName));
        header.setMinHeight(ROW_HEIGHT);
        VBox root = new VBox(10, header, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        load();
        return root;
    }

    public static double dialogWidth() {
        return DIALOG_WIDTH;
    }

    public static double dialogHeight() {
        return DIALOG_HEIGHT;
    }

    private static Tab tab(String key, Node content) {
        Tab tab = new Tab(Messages.get(key));
        tab.setContent(content);
        return tab;
    }

    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        ColumnConstraints labels = new ColumnConstraints();
        labels.setMinWidth(LABEL_COLUMN);
        labels.setPrefWidth(LABEL_COLUMN);
        ColumnConstraints inputs = new ColumnConstraints();
        inputs.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, inputs);
        return grid;
    }

    private static Label caption(String key) {
        Label label = new Label(Messages.get(key));
        label.setMinHeight(ROW_HEIGHT);
        label.setWrapText(true);
        label.setMaxWidth(LABEL_COLUMN);
        return label;
    }

    private static Label note(String key) {
        Label label = new Label(Messages.get(key));
        label.setWrapText(true);
        label.setMaxWidth(NOTE_WIDTH);
        label.setPrefWidth(NOTE_WIDTH);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    private static void row(GridPane grid, int index, String key, Node input) {
        grid.add(caption(key), 0, index);
        grid.add(input, 1, index);
    }

    private static void sizeInput(Region input) {
        input.setPrefWidth(INPUT_WIDTH);
        input.setMaxWidth(INPUT_WIDTH);
        input.setMinHeight(ROW_HEIGHT);
    }

    private GridPane basicPane() {
        GridPane grid = grid();
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        sizeInput(nameField);
        descriptionField.setPrefWidth(INPUT_WIDTH);
        descriptionField.setMaxWidth(INPUT_WIDTH);
        sizeInput(tag1);
        sizeInput(tag2);

        row(grid, 0, "preset.editor.name", nameField);
        row(grid, 1, "preset.editor.description", descriptionField);
        if (FlatCategories.isEmpty()) {
            sizeInput(categoryFallback);
            row(grid, 2, "preset.editor.category_id", categoryFallback);
        } else {
            categoryBox.getItems().addAll(FlatCategories.selectable());
            sizeInput(categoryBox);
            row(grid, 2, "preset.editor.category", categoryBox);
        }
        fill(maxVisitorsBox, new int[] { 10, 25, 50, 75, 100 }, "preset.editor.visitors_n");
        sizeInput(maxVisitorsBox);
        row(grid, 3, "preset.editor.visitors", maxVisitorsBox);

        tradeBox.getItems().addAll(
                new Choice(0, Messages.get("preset.editor.trade.0")),
                new Choice(1, Messages.get("preset.editor.trade.1")),
                new Choice(2, Messages.get("preset.editor.trade.2")));
        sizeInput(tradeBox);
        row(grid, 4, "preset.editor.trade", tradeBox);
        row(grid, 5, "preset.editor.tag1", tag1);
        row(grid, 6, "preset.editor.tag2", tag2);

        walkThroughBox.setText(Messages.get("preset.editor.walkthrough"));
        walkThroughBox.setMinHeight(ROW_HEIGHT);
        grid.add(walkThroughBox, 1, 7);
        grid.add(note("preset.editor.unavailable"), 0, 8, 2, 1);
        return grid;
    }

    private GridPane accessPane() {
        GridPane grid = grid();
        VBox doors = new VBox(6);
        for (int mode = 0; mode <= 3; mode++) {
            RadioButton radio = new RadioButton(Messages.get("preset.editor.door." + mode));
            radio.setToggleGroup(doorGroup);
            radio.setUserData(mode);
            radio.setMinHeight(ROW_HEIGHT);
            doors.getChildren().add(radio);
        }
        grid.add(caption("preset.editor.access"), 0, 0);
        grid.add(doors, 1, 0);
        sizeInput(doorPasswordField);
        row(grid, 1, "preset.editor.password", doorPasswordField);
        grid.add(note("preset.editor.password_note"), 0, 2, 2, 1);

        petsBox.setText(Messages.get("preset.editor.pets"));
        petsEatBox.setText(Messages.get("preset.editor.pets_eat"));
        mutePetsBox.setText(Messages.get("preset.editor.pets_mute"));
        for (CheckBox check : Arrays.asList(petsBox, petsEatBox, mutePetsBox)) {
            check.setMinHeight(ROW_HEIGHT);
        }
        VBox pets = new VBox(6, petsBox, petsEatBox, mutePetsBox);
        grid.add(caption("preset.editor.pet_settings"), 0, 3);
        grid.add(pets, 1, 3);
        return grid;
    }

    private GridPane hcPane() {
        GridPane grid = grid();
        hideWallsBox.setText(Messages.get("preset.editor.hidewalls"));
        hideWallsBox.setMinHeight(ROW_HEIGHT);
        grid.add(hideWallsBox, 1, 0);

        fill(wallThicknessBox, new int[] { -2, -1, 0, 1 }, "preset.editor.thickness_n");
        fill(floorThicknessBox, new int[] { -2, -1, 0, 1 }, "preset.editor.thickness_n");
        sizeInput(wallThicknessBox);
        sizeInput(floorThicknessBox);
        row(grid, 1, "preset.editor.wallthickness", wallThicknessBox);
        row(grid, 2, "preset.editor.floorthickness", floorThicknessBox);

        leaveOnDoorBox.setText(Messages.get("preset.editor.leaveondoor"));
        leaveOnDoorBox.setMinHeight(ROW_HEIGHT);
        leaveOnDoorBox.setWrapText(true);
        grid.add(leaveOnDoorBox, 1, 3);

        sleepBox.setText(Messages.get("preset.editor.sleep"));
        sleepBox.setMinHeight(ROW_HEIGHT);
        sizeInput(sleepSeconds);
        grid.add(sleepBox, 1, 4);
        row(grid, 5, "preset.editor.sleep_seconds", sleepSeconds);

        kickBox.setText(Messages.get("preset.editor.autokick"));
        kickBox.setMinHeight(ROW_HEIGHT);
        sizeInput(kickSeconds);
        grid.add(kickBox, 1, 6);
        row(grid, 7, "preset.editor.autokick_seconds", kickSeconds);

        floodBox.getItems().addAll(
                new Choice(0, Messages.get("preset.editor.flood.0")),
                new Choice(1, Messages.get("preset.editor.flood.1")),
                new Choice(2, Messages.get("preset.editor.flood.2")));
        sizeInput(floodBox);
        row(grid, 8, "preset.editor.flood", floodBox);
        return grid;
    }

    private GridPane modPane() {
        GridPane grid = grid();
        for (ComboBox<Choice> box : Arrays.asList(muteRightsBox, kickRightsBox, banRightsBox)) {
            box.getItems().addAll(
                    new Choice(0, Messages.get("preset.editor.rights.0")),
                    new Choice(1, Messages.get("preset.editor.rights.1")),
                    new Choice(2, Messages.get("preset.editor.rights.2")));
            sizeInput(box);
        }
        row(grid, 0, "preset.editor.who_mute", muteRightsBox);
        row(grid, 1, "preset.editor.who_kick", kickRightsBox);
        row(grid, 2, "preset.editor.who_ban", banRightsBox);
        grid.add(note("preset.editor.mod_unavailable"), 0, 3, 2, 1);
        return grid;
    }

    private static void fill(ComboBox<Choice> box, int[] values, String labelKey) {
        for (int value : values) {
            box.getItems().add(new Choice(value, Messages.get(labelKey + "." + value)));
        }
    }

    private static void select(ComboBox<Choice> box, int value) {
        for (Choice choice : box.getItems()) {
            if (choice.value == value) {
                box.getSelectionModel().select(choice);
                return;
            }
        }
        Choice raw = new Choice(value, String.valueOf(value));
        box.getItems().add(raw);
        box.getSelectionModel().select(raw);
    }

    private static int valueOf(ComboBox<Choice> box, int fallback) {
        Choice choice = box.getSelectionModel().getSelectedItem();
        return choice == null ? fallback : choice.value;
    }

    private void load() {
        nameField.setText(settings.optString("name", ""));
        descriptionField.setText(settings.optString("description", ""));

        int categoryId = settings.optInt("categoryId", 0);
        if (FlatCategories.isEmpty()) {
            categoryFallback.setText(String.valueOf(categoryId));
        } else {
            FlatCategories.Category current = FlatCategories.get(categoryId);
            if (current != null) {
                if (!categoryBox.getItems().contains(current)) {
                    categoryBox.getItems().add(0, current);
                }
                categoryBox.getSelectionModel().select(current);
            }
        }

        select(maxVisitorsBox, settings.optInt("maximumVisitors", 25));
        select(tradeBox, settings.optInt("tradeMode", 0));

        JSONArray tags = settings.optJSONArray("tags");
        if (tags != null) {
            if (tags.length() > 0) tag1.setText(tags.optString(0, ""));
            if (tags.length() > 1) tag2.setText(tags.optString(1, ""));
        }
        walkThroughBox.setSelected(settings.optBoolean("allowWalkThrough", true));

        int doorMode = settings.optInt("doorMode", 0);
        for (Toggle toggle : doorGroup.getToggles()) {
            if (Integer.valueOf(doorMode).equals(toggle.getUserData())) {
                toggle.setSelected(true);
            }
        }
        petsBox.setSelected(settings.optBoolean("allowPets", false));
        petsEatBox.setSelected(settings.optBoolean("allowFoodConsume", false));
        mutePetsBox.setSelected(settings.optBoolean("muteAllPets", false));
        doorPasswordField.setText(settings.optString("doorPassword", ""));

        hideWallsBox.setSelected(settings.optBoolean("hideWalls", false));
        select(wallThicknessBox, settings.optInt("wallThickness", 0));
        select(floorThicknessBox, settings.optInt("floorThickness", 0));
        leaveOnDoorBox.setSelected(settings.optBoolean("leaveOnDoorTile", true));
        sleepBox.setSelected(settings.optBoolean("idleSleepEnabled", true));
        sleepSeconds.setText(String.valueOf(settings.optInt("idleSleepTimeoutSeconds", 300)));
        kickBox.setSelected(settings.optBoolean("idleAutokickEnabled", true));
        kickSeconds.setText(String.valueOf(settings.optInt("idleAutokickTimeoutSeconds", 900)));
        select(floodBox, settings.optInt("chatFloodSensitivity", 1));

        select(muteRightsBox, settings.optInt("whoCanMute", 0));
        select(kickRightsBox, settings.optInt("whoCanKick", 0));
        select(banRightsBox, settings.optInt("whoCanBan", 0));
    }

    private String validate() {
        if (nameField.getText().trim().isEmpty()) {
            return Messages.get("preset.editor.error.name");
        }
        if (parsePositive(sleepSeconds.getText()) < 0 || parsePositive(kickSeconds.getText()) < 0) {
            return Messages.get("preset.editor.error.seconds");
        }
        if (FlatCategories.isEmpty() && parsePositive(categoryFallback.getText()) < 0) {
            return Messages.get("preset.editor.error.category");
        }
        Toggle door = doorGroup.getSelectedToggle();
        if (door != null && Integer.valueOf(2).equals(door.getUserData())
                && doorPasswordField.getText().trim().isEmpty()) {
            return Messages.get("preset.editor.error.password");
        }
        return null;
    }

    private static int parsePositive(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value < 0 ? -1 : value;
        } catch (Throwable t) {
            return -1;
        }
    }

    private void store() {
        settings.put("name", nameField.getText().trim());
        settings.put("description", descriptionField.getText().trim());

        if (FlatCategories.isEmpty()) {
            settings.put("categoryId", parsePositive(categoryFallback.getText()));
        } else {
            FlatCategories.Category category = categoryBox.getSelectionModel().getSelectedItem();
            if (category != null) {
                settings.put("categoryId", category.id);
            }
        }

        settings.put("maximumVisitors", valueOf(maxVisitorsBox, 25));
        settings.put("tradeMode", valueOf(tradeBox, 0));

        List<String> tags = new ArrayList<>();
        if (!tag1.getText().trim().isEmpty()) tags.add(tag1.getText().trim());
        if (!tag2.getText().trim().isEmpty()) tags.add(tag2.getText().trim());
        settings.put("tags", new JSONArray(tags));

        settings.put("allowWalkThrough", walkThroughBox.isSelected());

        Toggle door = doorGroup.getSelectedToggle();
        settings.put("doorMode", door == null ? 0 : (Integer) door.getUserData());
        settings.put("allowPets", petsBox.isSelected());
        settings.put("allowFoodConsume", petsEatBox.isSelected());
        settings.put("muteAllPets", mutePetsBox.isSelected());
        settings.put("doorPassword", doorPasswordField.getText());

        settings.put("hideWalls", hideWallsBox.isSelected());
        settings.put("wallThickness", valueOf(wallThicknessBox, 0));
        settings.put("floorThickness", valueOf(floorThicknessBox, 0));
        settings.put("leaveOnDoorTile", leaveOnDoorBox.isSelected());
        settings.put("idleSleepEnabled", sleepBox.isSelected());
        settings.put("idleSleepTimeoutSeconds", parsePositive(sleepSeconds.getText()));
        settings.put("idleAutokickEnabled", kickBox.isSelected());
        settings.put("idleAutokickTimeoutSeconds", parsePositive(kickSeconds.getText()));
        settings.put("chatFloodSensitivity", valueOf(floodBox, 1));

        settings.put("whoCanMute", valueOf(muteRightsBox, 0));
        settings.put("whoCanKick", valueOf(kickRightsBox, 0));
        settings.put("whoCanBan", valueOf(banRightsBox, 0));
    }
}
