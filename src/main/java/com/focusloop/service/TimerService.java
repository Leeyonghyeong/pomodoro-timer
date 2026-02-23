package com.focusloop.service;

import com.focusloop.model.TimerState;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService {
    private final IntegerProperty remainingSeconds = new SimpleIntegerProperty(0);
    private final ObjectProperty<TimerState> state = new SimpleObjectProperty<>(TimerState.IDLE);
    private final IntegerProperty currentCycle = new SimpleIntegerProperty(1);
    private final IntegerProperty totalCycles = new SimpleIntegerProperty(1);
    private final StorageService storageService = new StorageService();

    private int focusMinutes;
    private int breakMinutes;
    private Timer timer;
    private boolean isFocusPhase = true;

    public void start(int focusMinutes, int breakMinutes, int cycles) {
        this.focusMinutes = focusMinutes * 60;
        this.breakMinutes = breakMinutes * 60;
        this.totalCycles.set(cycles);
        this.currentCycle.set(1);
        startFocusPhase();
    }

    private void startFocusPhase() {
        isFocusPhase = true;
        state.set(TimerState.FOCUS_RUNNING);
        remainingSeconds.set(focusMinutes); // Minutes value is now treated as seconds
        runTimer();
    }

    private void startBreakPhase() {
        isFocusPhase = false;
        state.set(TimerState.BREAK_RUNNING);
        remainingSeconds.set(breakMinutes); // Minutes value now seconds
        runTimer();
    }

    private void runTimer() {
        if (timer != null)
            timer.cancel();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (remainingSeconds.get() > 0) {
                        remainingSeconds.set(remainingSeconds.get() - 1);
                    } else {
                        timer.cancel();
                        handlePhaseCompletion();
                    }
                });
            }
        }, 1000, 1000);
    }

    private boolean autoStartNext = false;
    private final NotificationService notificationService = new NotificationService();

    public void setAutoStartNext(boolean autoStartNext) {
        this.autoStartNext = autoStartNext;
    }

    public void setNotificationsEnabled(boolean enabled) {
        notificationService.setEnabled(enabled);
    }

    private void handlePhaseCompletion() {
        if (state.get() == TimerState.FOCUS_RUNNING) {
            storageService.updateDailyStatSeconds(focusMinutes, false); // Record focus time
            notificationService.showNotification("집중 종료!", "휴식 시간입니다.");
            if (autoStartNext) {
                startBreakPhase();
            } else {
                remainingSeconds.set(breakMinutes);
                isFocusPhase = false;
                state.set(TimerState.PAUSED);
            }
        } else if (state.get() == TimerState.BREAK_RUNNING) {
            if (currentCycle.get() < totalCycles.get()) {
                notificationService.showNotification("휴식 종료!", "다시 시작할 시간입니다.");
                currentCycle.set(currentCycle.get() + 1);
                if (autoStartNext) {
                    startFocusPhase();
                } else {
                    remainingSeconds.set(focusMinutes);
                    isFocusPhase = true;
                    state.set(TimerState.PAUSED);
                }
            } else {
                notificationService.showNotification("완료!", "모든 목표를 달성했습니다.");
                state.set(TimerState.COMPLETED);
            }
        }
    }

    public void setNotificationServiceTrayIcon(java.awt.TrayIcon trayIcon) {
        notificationService.setTrayIcon(trayIcon);
    }

    public void pause() {
        if (timer != null)
            timer.cancel();
        state.set(TimerState.PAUSED);
    }

    public void resume() {
        if (state.get() == TimerState.PAUSED) {
            if (isFocusPhase) {
                state.set(TimerState.FOCUS_RUNNING);
            } else {
                state.set(TimerState.BREAK_RUNNING);
            }
            runTimer();
        }
    }

    public void skip() {
        if (timer != null)
            timer.cancel();
        handlePhaseCompletion();
    }

    public void stop() {
        if (timer != null)
            timer.cancel();
        state.set(TimerState.IDLE);
        remainingSeconds.set(0);
        currentCycle.set(1);
    }

    // Getters
    public IntegerProperty remainingSecondsProperty() {
        return remainingSeconds;
    }

    public ObjectProperty<TimerState> stateProperty() {
        return state;
    }

    public IntegerProperty currentCycleProperty() {
        return currentCycle;
    }

    public IntegerProperty totalCyclesProperty() {
        return totalCycles;
    }
}
