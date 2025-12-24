package com.potato.desktop.Controller;

import com.potato.kernel.Software.Windows;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.ResourceBundle;

public class ActivateWinFrameController extends Controller {
    @FXML
    private Label statusLabel;
    @FXML
    private Button activateBtn;

    @FXML
    private ResourceBundle resources;

    private Windows windows;

    @FXML
    public void initialize() {
        // todo error handling
        try {
            windows = Windows.getWindows();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setComponents();
    }

    @FXML
    private void onActivateBtnClick() {
        // todo error handling
        try {
            windows.activateWindows();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setComponents();
    }

    private void setComponents() {
        boolean status = windows.isActivated();
        activateBtn.setDisable(status);
        statusLabel.setText(updatedLabelText("lab.activationStatusText",
                status ? resources.getString("lang.true") : resources.getString("lang.false")));
    }

    private <T> String updatedLabelText(String label, T value) {
        return resources.getString(label) + " " + value;
    }

    private <T> String updatedLabelText(String label, T value, String unit) {
        return resources.getString(label) + " " + value + " " + unit;
    }
}
