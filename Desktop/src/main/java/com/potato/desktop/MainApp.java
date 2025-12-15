package com.potato.desktop;

import com.potato.desktop.controller.MainFrameController;
import com.potato.kernel.Config;
import com.sun.source.tree.ParenthesizedTree;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Locale locale = new Locale("en", "US");
        ResourceBundle languageBundle = ResourceBundle.getBundle("messages", locale);

        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("main-frame.fxml"), languageBundle);
        Scene scene = new Scene(mainLoader.load());
        MainFrameController mainController = mainLoader.getController();
        stage.setTitle(Config.APP_NAME);
        stage.setScene(scene);

        stage.setOnCloseRequest((event) -> {
            mainController.closeFrame();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
