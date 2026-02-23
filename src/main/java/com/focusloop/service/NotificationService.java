package com.focusloop.service;

import java.awt.*;

public class NotificationService {
    private TrayIcon trayIcon;

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void showNotification(String title, String message) {
        if (!enabled)
            return;

        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }

        // For macOS native feel, we could use osascript
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            showMacNotification(title, message);
        }
    }

    private void showMacNotification(String title, String message) {
        try {
            String[] cmd = {
                    "osascript",
                    "-e",
                    "display notification \"" + message + "\" with title \"" + title + "\" sound name \"Glass\""
            };
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
