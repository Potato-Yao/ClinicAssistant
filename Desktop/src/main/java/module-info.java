module com.potato.desktop {
    requires com.potato.kernel;

    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires javafx.web;
    requires org.apache.commons.io;
    requires org.apache.commons.csv;
    requires java.desktop;
    requires java.logging;

    exports com.potato.desktop;

    opens com.potato.desktop to javafx.fxml;
    opens com.potato.desktop.Controller to javafx.fxml;
    opens com.potato.desktop.Utils to javafx.fxml;
}