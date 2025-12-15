module com.potato.desktop {
    requires com.potato.kernel;

    requires javafx.controls;
    requires javafx.fxml;

    exports com.potato.desktop;

    opens com.potato.desktop to javafx.fxml;
}