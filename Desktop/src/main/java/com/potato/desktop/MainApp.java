package com.potato.desktop;

import com.potato.desktop.Controller.*;
import com.potato.kernel.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.potato.desktop.Utils.DialogUtil.*;

public class MainApp extends Application {
    public final static String DESKTOP_VERSION = "0.0.1";

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    private HashMap<Controller, Stage> openedWindows = new HashMap<>();
    private int trickClickedCounter = 0;

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOG.log(Level.SEVERE, "Uncaught exception on thread " + t.getName(), e);
            e.printStackTrace();
        });

        LOG.info(() -> "user.dir=" + System.getProperty("user.dir"));
        LOG.info(() -> "clinic.externalToolsDir=" + System.getProperty("clinic.externalToolsDir"));
        LOG.info(() -> "javafx.runtime.version=" + System.getProperty("javafx.runtime.version"));

        stage.setTitle(Config.APP_NAME);
        stage.setScene(new Scene(new Label("Starting " + Config.APP_NAME + "!"), 360, 120));
        stage.show();

        try {
            openWindow("main-frame.fxml", "app.title.main", MainFrameController.class);
            stage.close();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed during startup window load", e);
            e.printStackTrace();
        }
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

    public void openHelpFrame() {
        openSoftwareInfoFrame("html-content-frame.fxml", "app.title.help", "help.html");
    }

    public void openAboutFrame() {
        openSoftwareInfoFrame("html-content-frame.fxml", "app.title.about", "about.html");
    }

    private void openSoftwareInfoFrame(String fxml, String title, String contentFileName) {
        Pair<HTMLContentFrameController, Stage> window = openWindow(fxml, title, HTMLContentFrameController.class);

        URL url = Thread.currentThread().getContextClassLoader().getResource(contentFileName);
        window.getKey().loadUrl(url.toExternalForm());
        trickClickedCounter++;

        if (judgeDivisible(trickClickedCounter)) {
            Locale locale = new Locale("en", "US");
            ResourceBundle languageBundle = ResourceBundle.getBundle("messages", locale);
            makeConfirmAlert(
                    languageBundle.getString("dlg.tooManyInfoWindows.header"),
                    languageBundle.getString("dlg.tooManyInfoWindows.content"),
                    () -> {
                    },
                    () -> {
                    });
        }
    }

    public <Con extends Controller> Pair<Con, Stage> openWindow(String fxml, String title, Class<Con> controllerClass) {
        // todo error handling
        try {
            Locale locale = new Locale("en", "US");
            ResourceBundle languageBundle = ResourceBundle.getBundle("messages", locale);

            // FXML files are located under Desktop/src/main/resources/com/potato/desktop/
            // so we must load them with an absolute classpath path.
            String fxmlPath = fxml.startsWith("/") ? fxml : "/com/potato/desktop/" + fxml;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), languageBundle);

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

//            Image appIcon = new Image("icon.png");
            Image appIcon = new Image(getClass().getResource("/icon.png").toExternalForm());
            stage.getIcons().add(appIcon);
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                try {
                    taskbar.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png")));
                } catch (Exception e) {
                }
            }

            stage.show();

            openedWindows.put(controller, stage);

            return new Pair<>(controller, stage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean judgeDivisible(int number) {
        for (int i = 2; i < 10; i++) {
            if ((number & ((1 << i) - 1)) == 0) {
                return true;
            }
        }

        return false;
    }
}
