package com.potato.desktop.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;

public class DialogUtil {
    public static void makeConfirmAlert(String header, String content, Runnable okAction, Runnable denyAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait().ifPresent((result) -> {
            if (result == ButtonType.OK) {
                okAction.run();
            } else if (result == ButtonType.CANCEL) {
                denyAction.run();
            }
        });
    }
}
