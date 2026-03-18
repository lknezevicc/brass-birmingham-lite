module hr.lknezevic.brassbirmingham {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires static lombok;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires java.net.http;
    requires java.desktop;

    requires org.jgrapht.core;

    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.controlsfx.controls;

    requires java.rmi;
    requires java.naming;
    requires java.xml;

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
    exports hr.lknezevic.brassbirmingham.controllers.game;
    exports hr.lknezevic.brassbirmingham.controllers.component;
    opens hr.lknezevic.brassbirmingham.controllers.component to javafx.fxml;

    exports hr.lknezevic.brassbirmingham.model.game;
    exports hr.lknezevic.brassbirmingham.model.industry;
    exports hr.lknezevic.brassbirmingham.model.player;
    exports hr.lknezevic.brassbirmingham.model.action;
    exports hr.lknezevic.brassbirmingham.model.card;
    exports hr.lknezevic.brassbirmingham.engine;
    exports hr.lknezevic.brassbirmingham.engine.validation;

    exports hr.lknezevic.brassbirmingham.network.dto;
    exports hr.lknezevic.brassbirmingham.network.rmi;
    exports hr.lknezevic.brassbirmingham.network.jndi;
    exports hr.lknezevic.brassbirmingham.network.client;

    exports hr.lknezevic.brassbirmingham.viewmodel;
    exports hr.lknezevic.brassbirmingham.model.ui;
    exports hr.lknezevic.brassbirmingham.ui;

    exports hr.lknezevic.brassbirmingham.persistence.replay;
    exports hr.lknezevic.brassbirmingham.persistence.save;
    exports hr.lknezevic.brassbirmingham.reflection;
    exports hr.lknezevic.brassbirmingham.logging;
}