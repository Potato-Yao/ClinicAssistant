package com.potato.desktop.controller;

import com.potato.kernel.Config;
import com.potato.kernel.Hardware.CPU;
import com.potato.kernel.Hardware.GPU;
import com.potato.kernel.Hardware.HardwareInfoManager;
import com.potato.kernel.Software.StressTestUtil;
import com.potato.kernel.Software.StressTestUtilBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.Instant;
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
    private Label statusLabel;
    @FXML
    private Label cpuInfoLabel;
    @FXML
    private Label gpuInfoLabel;
    @FXML
    private Label warningLabel;
    @FXML
    private TextField powerInputField;

    @FXML
    private ResourceBundle resources;

    private HardwareInfoManager hardwareInfoManager;
    private ScheduledExecutorService executor;
    private StressTestUtil stressTestUtil;

    private static final int MAX_TIME = 60;  // in seconds
    private boolean isRunning = false;
    private Instant startTime;
    private XYChart.Series<Number, Number> cpuTempSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> gpuTempSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> cpuPowerSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> gpuPowerSeries = new XYChart.Series<>();

    @FXML
    public void initialize() {
        try {
            hardwareInfoManager = HardwareInfoManager.getHardwareInfoManager();
            executor = Executors.newSingleThreadScheduledExecutor();

            configureChartItem(cpuTempChart);
            configureChartItem(gpuTempChart);
            configureChartItem(cpuPowerChart);
            configureChartItem(gpuPowerChart);

            frameUpdate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureChartItem(LineChart<Number, Number> chart) {
        NumberAxis x = (NumberAxis) chart.getXAxis();
        x.setAutoRanging(false);
        x.setUpperBound(MAX_TIME);
        x.setTickUnit(5);
        x.setTickLabelsVisible(false);
        x.setTickMarkVisible(false);
        x.setVisible(false);

        NumberAxis y = (NumberAxis) chart.getYAxis();
        y.setAutoRanging(true);
        y.setForceZeroInRange(false);

        chart.setCreateSymbols(false);
        chart.setLegendVisible(true);
        chart.setAnimated(false);
    }

    private void frameUpdate() {
        executor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (isRunning) {
                    chartUpdate();
                }
            });
        }, 0, Config.HARDWARE_INFO_SEEK_RATE, TimeUnit.MILLISECONDS);
    }

    private void chartUpdate() {
        double currTime = ((double) Instant.now().getEpochSecond() - startTime.getEpochSecond());  // in seconds
        CPU cpu = hardwareInfoManager.getCpu();
        GPU gpu = hardwareInfoManager.getGpu();

        chartItemUpdate(cpuTempChart, cpuTempSeries, currTime, cpu.getAverageTemperature());
        chartItemUpdate(gpuTempChart, gpuTempSeries, currTime, gpu.getTemperature());
        chartItemUpdate(cpuPowerChart, cpuPowerSeries, currTime, cpu.getPower());
        chartItemUpdate(gpuPowerChart, gpuPowerSeries, currTime, gpu.getPower());
    }

    private void chartItemUpdate(LineChart<Number, Number> chart, XYChart.Series series, double time, double value) {
        NumberAxis x = (NumberAxis) chart.getXAxis();
        if (time > x.getUpperBound()) {
            x.setLowerBound(Math.max(0, 0 + time - MAX_TIME));
            x.setUpperBound(time);
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
