package com.potato.desktop.controller;

import com.potato.kernel.Config;
import com.potato.kernel.Software.DiskManager;
import com.potato.kernel.Software.PartitionItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BitLockerFrameController extends Controller {
    @FXML
    private VBox partitionList;

    private DiskManager diskManager;

    private ScheduledExecutorService executor;
    private HashMap<PartitionItem, Pair<ProgressBar, Label>> partitionComponentMap = new HashMap<>();

    @FXML
    public void initialize() {
        executor = Executors.newSingleThreadScheduledExecutor();
        // todo error handling
        try {
            diskManager = DiskManager.getDiskManager();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        addPartitionItem();

        executor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                updateInfo();
            });
        }, 0, Config.HARDWARE_INFO_SEEK_RATE, TimeUnit.MILLISECONDS);
    }

    private void updateInfo() {
        partitionComponentMap.forEach((partition, pair) -> {
            pair.getKey().setProgress(partition.getBitlockerPercentage() / 100);
            pair.getValue().setText(updateLabel(partition.getBitlockerPercentage()));
        });
    }

    private void addPartitionItem() {
        ArrayList<PartitionItem> partitionItems = diskManager.getPartitionItems();

        partitionItems.forEach((item) -> {
            String name = item.getLabel();
            double percentage = item.getBitlockerPercentage();

            Label partitionLabel = new Label(name + ":");
            partitionLabel.setPrefWidth(60);

            ProgressBar progressBar = new ProgressBar(percentage / 100);  // normalize!
            progressBar.setPrefWidth(300);

            Label percentageLabel = new Label(updateLabel(percentage));

            HBox partitionRow = new HBox(10, partitionLabel, progressBar, percentageLabel);
            partitionRow.setPadding(new Insets(6));
            partitionRow.setStyle("""
                     -fx-border-color: #cccccc;
                    -fx-border-radius: 4;
                    -fx-background-radius: 4;
                    -fx-cursor: hand;
                    """);

            partitionList.getChildren().add(partitionRow);
            partitionComponentMap.put(item, new Pair<>(progressBar, percentageLabel));
        });
    }

    private String updateLabel(double percentage) {
        return String.format("%.2f", percentage) + "%";
    }

    public void onClose() {
        executor.close();
        diskManager.disconnect();
    }
}
