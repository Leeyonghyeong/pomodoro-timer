package com.focusloop.controller;

import com.focusloop.model.Preset;
import com.focusloop.model.TimerState;
import com.focusloop.service.StorageService;
import com.focusloop.service.TimerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class MainController {
    @FXML
    private VBox setupView;
    @FXML
    private VBox timerView;
    @FXML
    private Spinner<Integer> focusSpinner;
    @FXML
    private Spinner<Integer> breakSpinner;
    @FXML
    private Spinner<Integer> cyclesSpinner;
    @FXML
    private FlowPane presetContainer;
    @FXML
    private Button startButton;
    @FXML
    private Button settingsButton;
    @FXML
    private VBox settingsOverlay;
    @FXML
    private CheckBox soundCheckBox;
    @FXML
    private CheckBox autoNextCheckBox;
    @FXML
    private Button closeSettingsButton;

    @FXML
    private Label statusBadge;
    @FXML
    private Text timerText;
    @FXML
    private Label cycleLabel;
    @FXML
    private Button pauseButton;
    @FXML
    private Button skipButton;
    @FXML
    private Button stopButton;

    @FXML
    private Text todayFocusText;
    @FXML
    private Text weekFocusText;

    private final TimerService timerService = new TimerService();
    private final StorageService storageService = new StorageService();

    @FXML
    public void initialize() {
        setupSpinners();
        loadPresets();
        updateStats();

        startButton.setOnAction(e -> startTimer());
        stopButton.setOnAction(e -> stopTimer());
        pauseButton.setOnAction(e -> togglePause());
        skipButton.setOnAction(e -> timerService.skip());

        settingsButton.setOnAction(e -> settingsOverlay.setVisible(true));
        closeSettingsButton.setOnAction(e -> {
            settingsOverlay.setVisible(false);
            timerService.setNotificationsEnabled(soundCheckBox.isSelected());
            timerService.setAutoStartNext(autoNextCheckBox.isSelected());
        });

        timerService.remainingSecondsProperty().addListener((obs, oldVal, newVal) -> {
            updateTimerDisplay(newVal.intValue());
        });

        timerService.stateProperty().addListener((obs, oldVal, newVal) -> {
            updateUIState(newVal);
            if (newVal == TimerState.IDLE || newVal == TimerState.PAUSED || newVal == TimerState.COMPLETED) {
                updateStats();
            }
        });

        timerService.currentCycleProperty().addListener((obs, oldVal, newVal) -> {
            updateCycleDisplay(newVal.intValue(), timerService.totalCyclesProperty().get());
        });
    }

    private void setupSpinners() {
        focusSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, 25));
        breakSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 180, 5));
        cyclesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 4));
    }

    private void loadPresets() {
        List<Preset> presets = storageService.loadPresets();
        presetContainer.getChildren().clear();
        for (Preset preset : presets) {
            Button pBtn = new Button(preset.getName());
            pBtn.getStyleClass().add("button-secondary");
            pBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
            pBtn.setOnAction(e -> {
                focusSpinner.getValueFactory().setValue(preset.getFocusMinutes());
                breakSpinner.getValueFactory().setValue(preset.getBreakMinutes());
                cyclesSpinner.getValueFactory().setValue(preset.getCycles());
            });
            presetContainer.getChildren().add(pBtn);
        }
    }

    private void startTimer() {
        int focus = focusSpinner.getValue();
        int breakTime = breakSpinner.getValue();
        int cycles = cyclesSpinner.getValue();

        timerService.start(focus, breakTime, cycles);
        setupView.setVisible(false);
        timerView.setVisible(true);
    }

    private void stopTimer() {
        timerService.stop();
        setupView.setVisible(true);
        timerView.setVisible(false);
        updateStats();
    }

    private void togglePause() {
        if (timerService.stateProperty().get() == TimerState.PAUSED) {
            timerService.resume();
        } else {
            timerService.pause();
        }
    }

    private void updateTimerDisplay(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public TimerService getTimerService() {
        return timerService;
    }

    private void updateUIState(TimerState state) {
        switch (state) {
            case IDLE -> {
                setupView.setVisible(true);
                timerView.setVisible(false);
            }
            case FOCUS_RUNNING -> {
                setupView.setVisible(false);
                timerView.setVisible(true);
                statusBadge.setText("집중 중");
                statusBadge.getStyleClass().setAll("badge-focus");
                pauseButton.setText("일시정지");
            }
            case BREAK_RUNNING -> {
                setupView.setVisible(false);
                timerView.setVisible(true);
                statusBadge.setText("휴식 중");
                statusBadge.getStyleClass().setAll("badge-break");
                pauseButton.setText("일시정지");
            }
            case PAUSED -> {
                setupView.setVisible(false);
                timerView.setVisible(true);
                pauseButton.setText("다시 시작");
            }
            case COMPLETED -> {
                setupView.setVisible(true);
                timerView.setVisible(false);
                // Could show a "Done" dialog
            }
        }
    }

    private void updateCycleDisplay(int current, int total) {
        cycleLabel.setText(String.format("%d / %d 세트", current, total));
    }

    private void updateStats() {
        List<com.focusloop.model.DailyStat> stats = storageService.loadStats();
        String today = java.time.LocalDate.now().toString();

        int todaySeconds = stats.stream()
                .filter(s -> s.getDate().equals(today))
                .mapToInt(com.focusloop.model.DailyStat::getFocusSecondsTotal)
                .sum();

        int totalSeconds = stats.stream()
                .mapToInt(com.focusloop.model.DailyStat::getFocusSecondsTotal)
                .sum();

        int todayMins = todaySeconds / 60;
        int totalHours = totalSeconds / 3600;

        todayFocusText.setText(todayMins + "분");
        weekFocusText.setText(totalHours + "시간");
    }
}
