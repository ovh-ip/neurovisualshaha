package dev.testvisuals.hud;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class Config {

    public static final class ComponentData {
        public boolean enabled = true;
        public String anchor = "TOP_LEFT";
        public float offsetX = 10f;
        public float offsetY = 10f;
    }

    public static final class AltData {
        public String name;
        public long added;

        public AltData() {
        }

        public AltData(String name) {
            this.name = name;
            this.added = System.currentTimeMillis();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path;
    private static Map<String, ComponentData> components = new HashMap<>();
    private static String theme = "DARK";
    private static List<AltData> alts = new ArrayList<>();

    private Config() {
    }

    private static Path path() {
        if (path == null) {
            path = FabricLoader.getInstance().getConfigDir().resolve("testvisuals.json");
        }
        return path;
    }

    public static void load() {
        Path file = path();
        if (!Files.exists(file)) {
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Data data = GSON.fromJson(json, Data.class);
            if (data != null) {
                if (data.components != null) {
                    components = data.components;
                }
                if (data.theme != null) {
                    theme = data.theme;
                }
                if (data.alts != null) {
                    alts = data.alts;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        Data data = new Data();
        data.components = components;
        data.theme = theme;
        data.alts = alts;
        try {
            Path file = path();
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, ComponentData> components() {
        return components;
    }

    public static String theme() {
        return theme;
    }

    public static void theme(String value) {
        theme = value;
        save();
    }

    public static List<AltData> alts() {
        return alts;
    }

    public static void addAlt(String name) {
        alts.add(new AltData(name));
        save();
    }

    public static void removeAlt(int index) {
        if (index >= 0 && index < alts.size()) {
            alts.remove(index);
            save();
        }
    }

    public static final class Data {
        public Map<String, ComponentData> components;
        public String theme;
        public List<AltData> alts;
    }
}