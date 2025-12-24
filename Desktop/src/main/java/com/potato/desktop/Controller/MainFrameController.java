package com.potato.desktop.Controller;

import com.potato.desktop.MainApp;
import com.potato.kernel.Config;
import com.potato.kernel.External.OPHelper;
import com.potato.kernel.Hardware.*;
import com.potato.kernel.Software.DiskManager;
import com.potato.kernel.Software.NetworkUtil;
import com.potato.kernel.Software.SystemType;
import com.potato.kernel.Software.Windows;
import com.potato.kernel.Utils.FolderItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.potato.desktop.Utils.DialogUtil.*;
import static com.potato.kernel.Utils.Admin.*;

public class MainFrameController extends Controller {
    @FXML
    private VBox monitorView;
    @FXML
    private VBox toolsView;
    @FXML
    private ScrollPane externalsView;
    @FXML
    private VBox externalsPane;
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
    private Label adminStatus;
    @FXML
    private Label disksInfo;
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
    private DiskManager diskManager;
    private OPHelper opHelper;
    private NetworkUtil networkUtil;
    private Windows windows;
    private ScheduledExecutorService executor;
    private Node[] panes;

    @FXML
    public void initialize() {
        panes = new Node[]{monitorView, toolsView, externalsView};

        // todo error handling
        try {
            executor = Executors.newSingleThreadScheduledExecutor();
            hardwareInfoManager = HardwareInfoManager.getHardwareInfoManager();
            diskManager = DiskManager.getDiskManager();
            opHelper = OPHelper.getOpHelper();
            networkUtil = NetworkUtil.getNetworkUtil();
            windows = Windows.getWindows();

            makeExternalsPane();
            updateMonitorData();

            switchPane(monitorView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeExternalsPane() {
        HashMap<String, FolderItem> toolMap = opHelper.getToolMap();

        for (String name : toolMap.keySet()) {
            TitledPane titledPane = new TitledPane();
            titledPane.setText(name);
            VBox contentBox = new VBox();
            contentBox.setSpacing(6);

            for (Path path : toolMap.get(name).getFiles()) {
                Button toolBtn = new Button(path.getFileName().toString());
                toolBtn.setMaxWidth(Double.MAX_VALUE);
                toolBtn.setOnAction(event -> {
                    try {
                        Runtime.getRuntime().exec(path.toAbsolutePath().toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                contentBox.getChildren().add(toolBtn);
            }

            titledPane.setContent(contentBox);
            externalsPane.getChildren().add(titledPane);
        }
    }

    private void updateMonitorData() throws IOException {

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

        adminStatus.setText(isAdmin() ? resources.getString("lang.true") : resources.getString("lang.false"));

        StringBuilder disksTextBuilder = new StringBuilder();
        diskManager.getDiskItems().forEach((item) -> {
            disksTextBuilder.append(item.getId())
                    .append("\t")
                    .append(String.format("%.2f", item.getSize()))
                    .append(resources.getString("lang.GB"))
                    .append("\n");
        });
        disksInfo.setText(disksTextBuilder.toString());
    }

    @Override
    public void onClose() {
        hardwareInfoManager.close();
        executor.shutdownNow();
    }

    @FXML
    private void onMonitorBtnAction() {
        switchPane(monitorView);
    }

    @FXML
    private void onToolsBtnAction() {
        switchPane(toolsView);
    }

    @FXML
    private void onExternalsBtnAction() {
        switchPane(externalsView);
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

    private void switchPane(Node paneToShow) {
        for (Node pane : panes) {
            pane.setVisible(pane == paneToShow);
            pane.setManaged(pane == paneToShow);
        }
    }

    private <T> String updatedLabelText(String label, T value) {
        return resources.getString(label) + " " + value;
    }

    private <T> String updatedLabelText(String label, T value, String unit) {
        return resources.getString(label) + " " + value + " " + unit;
    }
}
