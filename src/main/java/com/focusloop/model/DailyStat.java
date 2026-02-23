package com.focusloop.model;

public class DailyStat {
    private String date; // YYYY-MM-DD
    private int focusSecondsTotal;
    private int sessionsCompleted;

    public DailyStat(String date) {
        this.date = date;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public int getFocusSecondsTotal() {
        return focusSecondsTotal;
    }

    public void setFocusSecondsTotal(int focusSecondsTotal) {
        this.focusSecondsTotal = focusSecondsTotal;
    }

    public int getSessionsCompleted() {
        return sessionsCompleted;
    }

    public void setSessionsCompleted(int sessionsCompleted) {
        this.sessionsCompleted = sessionsCompleted;
    }

    public void addFocusSeconds(int seconds) {
        this.focusSecondsTotal += seconds;
    }

    public void incrementSessions() {
        this.sessionsCompleted++;
    }
}
