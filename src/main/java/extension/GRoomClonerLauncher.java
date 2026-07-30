package extension;

import utils.Messages;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.ThemedExtensionFormCreator;
import javafx.stage.Stage;

import java.net.URL;

public class GRoomClonerLauncher extends ThemedExtensionFormCreator {

    @Override
    protected String getTitle() {
        return "G-PresetsPlus - Building & Wired Presets - "
                + GRoomCloner.class.getAnnotation(ExtensionInfo.class).Version();
    }

    @Override
    protected URL getFormResource() {
        return getClass().getResource("ui/groomcloner.fxml");
    }

    @Override
    protected void initialize(Stage primaryStage) {
        primaryStage.getScene().getStylesheets().add(getClass().getResource("ui/styles.css").toExternalForm());
    }

    public static void main(String[] args) {
        runExtensionForm(args, GRoomClonerLauncher.class);
    }

}
