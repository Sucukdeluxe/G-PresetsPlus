package extension.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import gearth.ui.themes.Theme;
import gearth.ui.themes.ThemeFactory;
import gearth.ui.titlebar.TitleBarConfig;
import gearth.ui.titlebar.TitleBarController;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.Messages;

import java.util.function.Supplier;

public class StyledDialog {

    private final Stage stage = new Stage();
    private final Button okButton;
    private boolean confirmed = false;
    private Supplier<String> validator = null;
    private final Label errorLabel = new Label();

    public StyledDialog(Stage owner, String title, Node content, double width, double height) {
        okButton = new Button(Messages.get("ui.dialog.save"));
        Button cancelButton = new Button(Messages.get("ui.dialog.cancel"));
        okButton.setPrefWidth(110);
        cancelButton.setPrefWidth(110);
        okButton.setMinHeight(28);
        cancelButton.setMinHeight(28);
        okButton.setDefaultButton(true);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(event -> {
            if (validator != null) {
                String problem = validator.get();
                if (problem != null) {
                    errorLabel.setText(problem);
                    errorLabel.setVisible(true);
                    return;
                }
            }
            confirmed = true;
            stage.close();
        });
        cancelButton.setOnAction(event -> stage.close());

        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(width - 260);
        errorLabel.setMinHeight(Region.USE_PREF_SIZE);
        errorLabel.getStyleClass().add("red");
        errorLabel.setStyle("-fx-text-fill: #ff8d82;");
        HBox.setHgrow(errorLabel, Priority.ALWAYS);

        HBox footer = new HBox(10, errorLabel, okButton, cancelButton);
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, content, footer);
        root.setPadding(new Insets(14));
        if (content instanceof Region) {
            VBox.setVgrow(content, Priority.ALWAYS);
        }

        root.getStyleClass().add("dialog-root");

        Scene scene = height > 0 ? new Scene(root, width, height) : new Scene(root, width, -1);
        if (owner != null) {
            stage.initOwner(owner);
            stage.getIcons().addAll(owner.getIcons());
            if (owner.getScene() != null) {
                scene.getStylesheets().addAll(owner.getScene().getStylesheets());
                Parent ownerRoot = owner.getScene().getRoot();
                for (String sheet : ownerRoot.getStylesheets()) {
                    if (!scene.getStylesheets().contains(sheet)) {
                        scene.getStylesheets().add(sheet);
                    }
                }
                for (String styleClass : ownerRoot.getStyleClass()) {
                    if (styleClass.startsWith("g-")) {
                        root.getStyleClass().add(styleClass);
                    }
                }
            }
        }

        boolean dark = root.getStyleClass().contains("g-dark");
        root.setStyle("-fx-border-width: 0;"
                + " -fx-border-color: transparent;"
                + " -fx-background-insets: 0;"
                + " -fx-background-radius: 0;"
                + " -fx-background-color: " + (dark ? "#2b2b2b" : "#f2f2f2") + ";");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMinWidth(width);
        if (height > 0) {
            stage.setMinHeight(height);
        }

        try {
            TitleBarController.create(stage, new DialogTitleBar(stage));
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (height <= 0) {
            stage.sizeToScene();
        }
    }

    private static class DialogTitleBar implements TitleBarConfig {
        private final Stage stage;
        private Theme theme = ThemeFactory.getDefaultTheme();

        DialogTitleBar(Stage stage) {
            this.stage = stage;
        }

        @Override
        public boolean displayThemePicker() {
            return false;
        }

        @Override
        public boolean displayMinimizeButton() {
            return false;
        }

        @Override
        public void onCloseClicked() {
            stage.close();
        }

        @Override
        public void onMinimizeClicked() {
            stage.setIconified(true);
        }

        @Override
        public void setTheme(Theme theme) {
            this.theme = theme;
        }

        @Override
        public Theme getCurrentTheme() {
            return theme;
        }
    }

    public Stage getStage() {
        return stage;
    }

    public StyledDialog setValidator(Supplier<String> validator) {
        this.validator = validator;
        return this;
    }

    public StyledDialog setSaveText(String text) {
        okButton.setText(text);
        return this;
    }

    public boolean showAndWaitConfirmed() {
        stage.showAndWait();
        return confirmed;
    }
}
