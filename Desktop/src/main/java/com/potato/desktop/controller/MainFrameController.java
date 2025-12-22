package com.potato.desktop.controller;

import com.potato.desktop.MainApp;
import com.potato.kernel.Config;
import com.potato.kernel.Hardware.*;
import com.potato.kernel.Software.NetworkUtil;
import com.potato.kernel.Software.SystemType;
import com.potato.kernel.Software.Windows;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.potato.desktop.Util.DialogUtil.*;

public class MainFrameController extends Controller {
    @FXML
    private VBox monitorView;
    @FXML
    private VBox toolsView;
    @FXML
    private MenuItem helpMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private MenuItem versionMenuItem;
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
    private Label batteryChargingStatus;
    @FXML
    private Label batteryRemain;
    @FXML
    private Label batteryHealth;
    @FXML
    private Label batteryRate;
    @FXML
    private Label ramTotal;
    @FXML
    private Label ramUsage;
    @FXML
    private Button proxyResetBtn;
    @FXML
    private Button code65RepairBtn;
    @FXML
    private Button activateWinBtn;
    @FXML
    private Button enterBIOSBtn;
    @FXML
    private Button bitLockerBtn;
    @FXML
    private Button stressTestBtn;

    @FXML
    private ResourceBundle resources;

    private HardwareInfoManager hardwareInfoManager;
    private NetworkUtil networkUtil;
    private Windows windows;
    private ScheduledExecutorService executor;

    @FXML
    public void initialize() {
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
        networkUtil = NetworkUtil.getNetworkUtil();
        windows = Windows.getWindows();

        executor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                updateDataInternal();
            });
        }, 0, Config.HARDWARE_INFO_SEEK_RATE, TimeUnit.MILLISECONDS);
    }

    public void updateDataInternal() {
        CPU cpu = hardwareInfoManager.getCpu();
        GPU gpu = hardwareInfoManager.getGpu();
        Battery battery = hardwareInfoManager.getBattery();
        RAM ram = hardwareInfoManager.getRam();

        cpuTemp.setText(updatedLabelText("lab.cpuTemp", cpu.getPackageTemperature(), "°C"));
        cpuPower.setText(updatedLabelText("lab.cpuPower", cpu.getPower(), "W"));
        cpuClock.setText(updatedLabelText("lab.cpuClock", String.format("%.2f", cpu.getClock()), "GHz"));

        gpuTemp.setText(updatedLabelText("lab.gpuTemp", gpu.getTemperature(), "°C"));
        gpuPower.setText(updatedLabelText("lab.gpuPower", gpu.getPower(), "W"));
        gpuClock.setText(updatedLabelText("lab.gpuClock", gpu.getSpeed(), "MHz"));

        batteryChargingStatus.setText(updatedLabelText("lab.batCharging",
                battery.isCharging() ? resources.getString("lang.true") : resources.getString("lang.false")));
        batteryHealth.setText(updatedLabelText("lab.batHealth",
                String.format("%.2f", battery.getHealthPercentage()), "%"));
        batteryRemain.setText(updatedLabelText("lab.batRemain",
                String.format("%.2f", battery.getRemainPercentage()), "%"));
        batteryRate.setText(updatedLabelText("lab.batRate", battery.getRate(), "W"));

        ramTotal.setText(updatedLabelText("lab.ramTotal", ram.getTotalSize(), "GB"));
        ramUsage.setText(updatedLabelText("lab.ramUsage", String.format("%.2f", ram.getUsedPercentage()), "%"));

        systemName.setText(updatedLabelText("lab.systemName", windows.getSystemName()));
        osVersion.setText(updatedLabelText("lab.osVersion", windows.getSystemVersion()));
        laptopModel.setText(updatedLabelText("lab.laptopModel", windows.getSystemModel()));
        systemType.setText(updatedLabelText("lab.systemType",
                windows.getSystemType() == SystemType.X86 ? resources.getString("lang.bit32") : resources.getString("lang.bit64")));
        activationStatus.setText(updatedLabelText("lab.activationStatus",
                windows.isActivated() ? resources.getString("lang.true") : resources.getString("lang.false")));
    }

    @Override
    public void onClose() {
        hardwareInfoManager.close();
        executor.shutdownNow();
    }

    @FXML
    private void onMonitorBtnAction() {
        monitorView.setVisible(true);
        toolsView.setVisible(false);
    }

    @FXML
    private void onToolsBtnAction() {
        monitorView.setVisible(false);
        toolsView.setVisible(true);
    }

    @FXML
    private void onActivateWinBtnAction() {
        mainApp.openActivateWinFrame();
    }

    @FXML
    private void onStressTestBtnAction() {
        mainApp.openStressTestFrame();
    }

    @FXML
    private void onEnterBIOSBtnAction() {
        makeConfirmAlert(
                resources.getString("app.title.biosTitle"),
                resources.getString("app.title.biosHint"),
                () -> {
                    try {
                        windows.enterBIOS();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                }
        );
    }

    @FXML
    private void onRestProxyBtnAction() {
        makeConfirmAlert(
                resources.getString("app.title.resetProxyTitle"),
                resources.getString("app.title.resetProxyHint"),
                () -> {
                    try {
                        networkUtil.resetNetworkProxy();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                }
        );
    }

    @FXML
    private void onCode56BtnAction() {
        makeConfirmAlert(
                resources.getString("app.title.code56Title"),
                resources.getString("app.title.code56Hint"),
                () -> {
                    try {
                        networkUtil.deleteVMwareNetBridge();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                }
        );
    }

    @FXML
    private void onBitLockerBtnAction() {
        mainApp.openBitLockerFrame();
    }

    @FXML
    private void onHelpMenuItemAction() {
        mainApp.openHelpFrame();
    }

    @FXML
    private void onAboutMenuItemAction() {
        mainApp.openAboutFrame();
    }

    @FXML
    private void onVersionMenuItemAction() {
        makeConfirmAlert(
                resources.getString("app.title.version"),
                String.format("%s: %s\n%s: %s", resources.getString("lang.kernel"), Config.KERNEL_VERSION, resources.getString("lang.desktop"), MainApp.DESKTOP_VERSION),
                () -> {
                },
                () -> {
                }
        );
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private <T> String updatedLabelText(String label, T value) {
        return resources.getString(label) + " " + value;
    }

    private <T> String updatedLabelText(String label, T value, String unit) {
        return resources.getString(label) + " " + value + " " + unit;
    }
}
