module com.potato.desktop {
    requires com.potato.kernel;

    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;

    exports com.potato.desktop;

    opens com.potato.desktop to javafx.fxml;
    opens com.potato.desktop.controller to javafx.fxml;
    opens com.potato.desktop.Util to javafx.fxml;
}