module hr.lknezevic.brassbirmingham {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires static lombok;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;

    opens hr.lknezevic.brassbirmingham to javafx.fxml;
    exports hr.lknezevic.brassbirmingham;

    exports hr.lknezevic.brassbirmingham.scene;
    opens hr.lknezevic.brassbirmingham.scene to javafx.fxml;

    exports hr.lknezevic.brassbirmingham.factory;
    opens hr.lknezevic.brassbirmingham.factory to javafx.fxml;

    exports hr.lknezevic.brassbirmingham.enums;
    exports hr.lknezevic.brassbirmingham.app;
    opens hr.lknezevic.brassbirmingham.app to javafx.fxml;

    exports hr.lknezevic.brassbirmingham.controllers;
    opens hr.lknezevic.brassbirmingham.controllers to javafx.fxml;
}