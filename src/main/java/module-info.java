module com.takeshi.sharedlink {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.takeshi.sharedlink to javafx.fxml;
    exports com.takeshi.sharedlink;
    exports com.takeshi.sharedlink.bo;
    opens com.takeshi.sharedlink.bo to javafx.fxml;
    exports com.takeshi.sharedlink.controller;
    opens com.takeshi.sharedlink.controller to javafx.fxml;
}