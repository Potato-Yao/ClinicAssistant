module com.potato.desktop {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.potato.desktop to javafx.fxml;
    exports com.potato.desktop;
}