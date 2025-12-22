package com.potato.desktop.controller;

import com.potato.desktop.Util.CSVUtil;
import com.potato.desktop.component.ChartType;
import com.potato.desktop.component.NumberLineChart;
import com.potato.kernel.Config;
import com.potato.kernel.Hardware.CPU;
import com.potato.kernel.Hardware.GPU;
import com.potato.kernel.Hardware.HardwareInfoManager;
import com.potato.kernel.Software.StressTestUtil;
import com.potato.kernel.Software.StressTestUtilBuilder;
import com.potato.kernel.Software.TestState;
import com.potato.kernel.Software.TestStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StressTestFrameController extends Controller {
    @FXML
    private CheckBox cpuCheckBox;
    @FXML
    private CheckBox gpuCheckBox;
    @FXML
    private Button runBtn;
    @FXML
    private LineChart<Number, Number> cpuTempChart;
    @FXML
    private LineChart<Number, Number> cpuPowerChart;
    @FXML
    private LineChart<Number, Number> gpuTempChart;
    @FXML
    private LineChart<Number, Number> gpuPowerChart;
    @FXML
    private Label cpuInfoLabel;
    @FXML
    private Label gpuInfoLabel;
    @FXML
    private Label powerInfoLabel;
    @FXML
    private Label warningLabel;
    @FXML
    private Label lastingTimeLabel;
    @FXML
    private TextField powerInputField;

    @FXML
    private ResourceBundle resources;

    private HardwareInfoManager hardwareInfoManager;
    private ScheduledExecutorService executor;
    private StressTestUtil stressTestUtil;
    private CSVUtil csvUtil;

    private static final int MAX_TIME = 60;  // in seconds
    private boolean isRunning = false;
    private double currTime = 0;
    private Instant startTime;
    private NumberLineChart cpuTempLineChart;
    private NumberLineChart gpuTempLineChart;
    private NumberLineChart cpuPowerLineChart;
    private NumberLineChart gpuPowerLineChart;
    private XYChart.Series<Number, Number> cpuTempSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> gpuTempSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> cpuPowerSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> gpuPowerSeries = new XYChart.Series<>();

    @FXML
    public void initialize() {
        try {
            hardwareInfoManager = HardwareInfoManager.getHardwareInfoManager();
            executor = Executors.newSingleThreadScheduledExecutor();

            cpuTempLineChart = new NumberLineChart(ChartType.TEMPERATURE, cpuTempChart, Double.valueOf(MAX_TIME), null, Double.valueOf(100), Double.valueOf(30));
            gpuTempLineChart = new NumberLineChart(ChartType.TEMPERATURE, gpuTempChart, Double.valueOf(MAX_TIME), null, Double.valueOf(100), Double.valueOf(30));
            cpuPowerLineChart = new NumberLineChart(ChartType.POWER, cpuPowerChart, Double.valueOf(MAX_TIME), null, null, Double.valueOf(0));
            gpuPowerLineChart = new NumberLineChart(ChartType.POWER, gpuPowerChart, Double.valueOf(MAX_TIME), null, null, Double.valueOf(0));

            frameUpdate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void frameUpdate() {
        executor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (isRunning) {
                    if (!stressTestUtil.isRunning()) {
                        closeTest();
                    }

                    currTime = ((double) Instant.now().getEpochSecond() - startTime.getEpochSecond());  // in seconds
                    CPU cpu = hardwareInfoManager.getCpu();
                    GPU gpu = hardwareInfoManager.getGpu();

                    chartUpdate();
                    lastingTimeLabel.setText(String.format(
                            resources.getString("lab.stressTestLastingTime.format"),
                            currTime));

                    try {
                        csvUtil.write(
                                String.valueOf(currTime),
                                String.valueOf(cpu.getPackageTemperature()),
                                String.valueOf(cpu.getPower()),
                                String.valueOf(gpu.getTemperature()),
                                String.valueOf(gpu.getPower()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }, 0, Config.HARDWARE_INFO_SEEK_RATE, TimeUnit.MILLISECONDS);
    }

    private void chartUpdate() {
        CPU cpu = hardwareInfoManager.getCpu();
        GPU gpu = hardwareInfoManager.getGpu();

        chartItemUpdate(cpuTempLineChart, cpuTempSeries, currTime, cpu.getPackageTemperature());
        chartItemUpdate(gpuTempLineChart, gpuTempSeries, currTime, gpu.getTemperature());
        chartItemUpdate(cpuPowerLineChart, cpuPowerSeries, currTime, cpu.getPower());
        chartItemUpdate(gpuPowerLineChart, gpuPowerSeries, currTime, gpu.getPower());

        TestState[] testStates = stressTestUtil.getTestStates();
        cpuInfoLabel.setText(testStates[0].getInfo());
        gpuInfoLabel.setText(testStates[1].getInfo());
        powerInfoLabel.setText(testStates[2].getInfo());

        StringBuilder warningInfo = new StringBuilder();
        for (TestState testState : testStates) {
            if (testState == null) {
                continue;
            }
            if (testState.getTestStatus() == TestStatus.CRITICAL) {
                warningInfo.append(testState.getInfo()).append("\n");
            }
        }
        warningLabel.setText(warningInfo.toString().isEmpty() ?
                resources.getString("lab.stressTestNoWarning") : warningInfo.toString().trim());
    }

    private void chartItemUpdate(NumberLineChart chart, XYChart.Series<Number, Number> series, double time, double value) {
        chart.setxAxisInRange(time);

        if (chart.getChartType() == ChartType.POWER) {
            chart.setyUpperBoundByRadio(value, 1.3);
        }

        series.getData().add(new XYChart.Data<>(time, value));

        if (series.getData().size() > MAX_TIME) {
            series.getData().removeFirst();
        }
    }

    @FXML
    private void onRunBtnClick() {
        if (!isRunning) {
            runTest();
        } else {
            closeTest();
        }
        inverseRunButtonText();
    }

    private void runTest() {
        try {
            stressTestUtil = new StressTestUtilBuilder()
                    .cpuTest(cpuCheckBox.isSelected())
                    .gpuTest(gpuCheckBox.isSelected())
                    .totalPower(powerInputField.getText().isEmpty() ? -1 : Integer.parseInt(powerInputField.getText()))
                    .build();

            lastingTimeLabel.setText(resources.getString("lab.stressTestLastingTime"));
            cpuCheckBox.setDisable(true);
            gpuCheckBox.setDisable(true);
            powerInfoLabel.setDisable(true);

            cpuTempChart.getData().clear();
            gpuTempChart.getData().clear();
            cpuPowerChart.getData().clear();
            gpuPowerChart.getData().clear();

            cpuTempSeries = new XYChart.Series<>();
            gpuTempSeries = new XYChart.Series<>();
            cpuPowerSeries = new XYChart.Series<>();
            gpuPowerSeries = new XYChart.Series<>();
            cpuTempChart.getData().add(cpuTempSeries);
            gpuTempChart.getData().add(gpuTempSeries);
            cpuPowerChart.getData().add(cpuPowerSeries);
            gpuPowerChart.getData().add(gpuPowerSeries);

            LocalDateTime time = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            csvUtil = new CSVUtil(
                    "stress_test_" + time.format(formatter) + ".csv",
                    true,
                    true,
                    resources.getString("csv.stressTest.timeSeconds"),
                    resources.getString("csv.stressTest.cpuTemp"),
                    resources.getString("csv.stressTest.cpuPower"),
                    resources.getString("csv.stressTest.gpuTemp"),
                    resources.getString("csv.stressTest.gpuPower"));

            stressTestUtil.runStressTest(StressTestUtil.AUTO_JUDGE_MODE);

            isRunning = true;
            startTime = Instant.now();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeTest() {
        stressTestUtil.stopStressTest();
        isRunning = false;
        cpuCheckBox.setDisable(false);
        gpuCheckBox.setDisable(false);
        powerInfoLabel.setDisable(false);
        try {
            csvUtil.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void inverseRunButtonText() {
        runBtn.setText(
                runBtn.getText().equals(resources.getString("btn.stressTest.runTest"))
                        ? resources.getString("btn.stressTest.stopTest") : resources.getString("btn.stressTest.runTest"));
    }

    @Override
    public void onClose() {
        if (isRunning) {
            closeTest();
        }
        executor.close();
    }
}
