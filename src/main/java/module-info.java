module com.focusloop {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.media;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires transitive java.desktop;
    requires java.sql;

    opens com.focusloop.controller to javafx.fxml;
    opens com.focusloop.model to com.google.gson, javafx.base;

    exports com.focusloop;
    exports com.focusloop.model;
    exports com.focusloop.service;
}
