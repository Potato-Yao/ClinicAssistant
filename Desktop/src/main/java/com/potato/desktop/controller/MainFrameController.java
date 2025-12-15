package com.potato.desktop.controller;

import com.potato.kernel.Hardware.CPU;
import com.potato.kernel.Hardware.GPU;
import com.potato.kernel.Hardware.HardwareInfoManager;
import com.potato.kernel.Software.SystemType;
import com.potato.kernel.Software.Windows;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainFrameController {
    @FXML
    private VBox monitorView;
    @FXML
    private VBox toolsView;
    @FXML
    private Button monitorBtn;
    @FXML
    private Button toolsBtn;
    @FXML
    private Label cpuTemp;
    @FXML
    private Label cpuPower;
    @FXML
    private Label cpuClock;
    @FXML
    private Label systemName;
    @FXML
    private Label osVersion;
    @FXML
    private Label laptopModel;
    @FXML
    private Label systemType;
    @FXML
    private Label activationStatus;
    @FXML
    private Label gpuTemp;
    @FXML
    private Label gpuPower;
    @FXML
    private Label gpuClock;

    @FXML
    private ResourceBundle resources;

    private HardwareInfoManager hardwareInfoManager;
    private Windows windows;
    private ScheduledExecutorService executor;

    @FXML
    public void initialize() {
        monitorBtn.setOnAction((e) -> {
            monitorBtnAction();
        });
        toolsBtn.setOnAction((e) -> {
            toolsBtnAction();
        });

        // todo error handling
        try {
            updateMonitorData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateMonitorData() throws IOException {
        executor = Executors.newSingleThreadScheduledExecutor();
        hardwareInfoManager = HardwareInfoManager.getHardwareInfoManager();
        windows = Windows.getWindows();

        executor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                updateDataInternal();
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void updateDataInternal() {
        CPU cpu = hardwareInfoManager.getCpu();
        GPU gpu = hardwareInfoManager.getGpu();

        cpuTemp.setText(updatedLabelText("lab.cpuTemp", cpu.getPackageTemperature(), "°C"));
        cpuPower.setText(updatedLabelText("lab.cpuPower", cpu.getPower(), "W"));
        cpuClock.setText(updatedLabelText("lab.cpuClock", cpu.getClockBegin(), "MHz"));

        gpuTemp.setText(updatedLabelText("lab.gpuTemp", gpu.getTemperature(), "°C"));
        gpuPower.setText(updatedLabelText("lab.gpuPower", gpu.getPower(), "W"));
        gpuClock.setText(updatedLabelText("lab.gpuClock", gpu.getSpeed(), "MHz"));

        systemName.setText(updatedLabelText("lab.systemName", windows.getSystemName()));
        osVersion.setText(updatedLabelText("lab.osVersion", windows.getSystemVersion()));
        laptopModel.setText(updatedLabelText("lab.laptopModel", windows.getSystemModel()));
        systemType.setText(updatedLabelText("lab.systemType",
                windows.getSystemType() == SystemType.X86 ? resources.getString("lang.bit32") : resources.getString("lang.bit64")));
        activationStatus.setText(updatedLabelText("lab.activationStatus",
                windows.isActivated() ? resources.getString("lang.true") : resources.getString("lang.false")));
    }

    public void closeFrame() {
        hardwareInfoManager.close();
        executor.shutdownNow();
    }

    private void monitorBtnAction() {
        monitorView.setVisible(true);
        toolsView.setVisible(false);
    }

    private void toolsBtnAction() {
        monitorView.setVisible(false);
        toolsView.setVisible(true);
    }

    private <T> String updatedLabelText(String label, T value) {
        return resources.getString(label) + " " + value;
    }

    private <T> String updatedLabelText(String label, T value, String unit) {
        return resources.getString(label) + " " + value + " " + unit;
    }
}
