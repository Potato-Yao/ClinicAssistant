package com.potato.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HTMLContentFrameController extends Controller {
    @FXML
    private WebView webView;
    @FXML
    private MenuItem helpMenuItem;
    @FXML
    private MenuItem aboutMenuItem;

    private WebEngine engine;

    @FXML
    private void initialize() {
        engine = webView.getEngine();
    }

    public void loadUrl(String url) {
        engine.load(url);
    }

    @FXML
    private void onHelpMenuItemClick() {
        mainApp.openHelpFrame();
    }

    @FXML
    private void onAboutMenuItemClick() {
        mainApp.openAboutFrame();
    }
}
