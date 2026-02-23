package com.focusloop.model;

public class Settings {
    private boolean soundEnabled = true;
    private boolean autoStartNext = false;
    private boolean runOnStartup = false;
    private String theme = "dark";

    // Getters and Setters
    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }
    public boolean isAutoStartNext() { return autoStartNext; }
    public void setAutoStartNext(boolean autoStartNext) { this.autoStartNext = autoStartNext; }
    public boolean isRunOnStartup() { return runOnStartup; }
    public void setRunOnStartup(boolean runOnStartup) { this.runOnStartup = runOnStartup; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
