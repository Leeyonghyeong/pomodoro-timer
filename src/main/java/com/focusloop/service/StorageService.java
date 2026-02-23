package com.focusloop.service;

import com.focusloop.model.DailyStat;
import com.focusloop.model.Preset;
import com.focusloop.model.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StorageService {
    private static final String APP_DIR = System.getProperty("user.home") + "/.focusloop";
    private static final String SETTINGS_FILE = APP_DIR + "/settings.json";
    private static final String PRESETS_FILE = APP_DIR + "/presets.json";
    private static final String STATS_FILE = APP_DIR + "/stats.json";

    private final Gson gson;

    public StorageService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(APP_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Settings loadSettings() {
        return loadFromFile(SETTINGS_FILE, Settings.class, new Settings());
    }

    public void saveSettings(Settings settings) {
        saveToFile(SETTINGS_FILE, settings);
    }

    public List<Preset> loadPresets() {
        Type type = new TypeToken<ArrayList<Preset>>() {
        }.getType();
        List<Preset> presets = loadFromFile(PRESETS_FILE, type, new ArrayList<Preset>());
        if (presets.isEmpty()) {
            // Add default presets
            presets.add(new Preset("강력한 집중 (50/10)", 50, 10, 4));
            presets.add(new Preset("표준 뽀모도로 (25/5)", 25, 5, 4));
            presets.add(new Preset("짧은 집중 (10/2)", 10, 2, 1));
            savePresets(presets);
        }
        return presets;
    }

    public void savePresets(List<Preset> presets) {
        saveToFile(PRESETS_FILE, presets);
    }

    public List<DailyStat> loadStats() {
        Type type = new TypeToken<ArrayList<DailyStat>>() {
        }.getType();
        return loadFromFile(STATS_FILE, type, new ArrayList<DailyStat>());
    }

    public void saveStats(List<DailyStat> stats) {
        saveToFile(STATS_FILE, stats);
    }

    public void updateDailyStatSeconds(int focusSeconds, boolean sessionCompleted) {
        List<DailyStat> stats = loadStats();
        String today = LocalDate.now().toString();
        DailyStat todayStat = stats.stream()
                .filter(s -> s.getDate().equals(today))
                .findFirst()
                .orElseGet(() -> {
                    DailyStat s = new DailyStat(today);
                    stats.add(s);
                    return s;
                });

        todayStat.addFocusSeconds(focusSeconds);
        if (sessionCompleted) {
            todayStat.incrementSessions();
        }
        saveStats(stats);
    }

    private <T> T loadFromFile(String filePath, Type type, T defaultValue) {
        File file = new File(filePath);
        if (!file.exists())
            return defaultValue;
        try (Reader reader = new FileReader(file)) {
            T result = gson.fromJson(reader, type);
            return result != null ? result : defaultValue;
        } catch (IOException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    private <T> T loadFromFile(String filePath, Class<T> clazz, T defaultValue) {
        File file = new File(filePath);
        if (!file.exists())
            return defaultValue;
        try (Reader reader = new FileReader(file)) {
            T result = gson.fromJson(reader, clazz);
            return result != null ? result : defaultValue;
        } catch (IOException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    private void saveToFile(String filePath, Object data) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
