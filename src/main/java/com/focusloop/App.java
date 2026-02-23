package com.focusloop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class App extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        Platform.setImplicitExit(false);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        com.focusloop.controller.MainController controller = loader.getController();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        stage.setTitle("FocusLoop");
        stage.setScene(scene);
        stage.show();

        setupSystemTray(controller);
    }

    private void setupSystemTray(com.focusloop.controller.MainController controller) {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        // Just a placeholder icon for now
        java.awt.Image image = Toolkit.getDefaultToolkit()
                .createImage(getClass().getResource("/assets/icons/tray_icon.png"));

        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Open FocusLoop");
        showItem.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener(e -> {
            tray.remove(tray.getTrayIcons()[0]);
            System.exit(0);
        });

        popup.add(showItem);
        popup.addSeparator();
        popup.add(quitItem);

        TrayIcon trayIcon = new TrayIcon(image, "FocusLoop", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        try {
            tray.add(trayIcon);
            // Connect to timer service for notifications
            controller.getTimerService().setNotificationServiceTrayIcon(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
