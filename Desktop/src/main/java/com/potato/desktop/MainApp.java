package com.potato.desktop;

import com.potato.desktop.controller.*;
import com.potato.kernel.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {
    private HashMap<Controller, Stage> openedWindows = new HashMap<>();

    @Override
    public void start(Stage stage) throws Exception {
        openWindow("main-frame.fxml", "app.title.main", MainFrameController.class);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void openActivateWinFrame() {
        openWindow("activate-win-frame.fxml", "app.title.activateWindows", ActivateWinFrameController.class);
    }

    public void openBitLockerFrame() {
        openWindow("bitlocker-frame.fxml", "app.title.bitlocker", BitLockerFrameController.class);
    }

    public void openStressTestFrame() {
        openWindow("stress-test-frame.fxml", "app.title.stressTest", StressTestFrameController.class);
    }

    public <Con extends Controller> Pair<Con, Stage> openWindow(String fxml, String title, Class<Con> controllerClass) {
        // todo error handling
        try {
            Locale locale = new Locale("en", "US");
            ResourceBundle languageBundle = ResourceBundle.getBundle("messages", locale);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml), languageBundle);

            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setTitle(Config.APP_NAME + " " + languageBundle.getString(title));
            stage.setScene(scene);

            Con controller = controllerClass.cast(loader.getController());
            controller.setMainApp(this);

            stage.setOnCloseRequest((e) -> {
                controller.onClose();
            });
            stage.show();

            openedWindows.put(controller, stage);

            return new Pair<>(controller, stage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
