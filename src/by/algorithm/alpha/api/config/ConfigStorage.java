package by.algorithm.alpha.api.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigStorage {
    public static final Logger logger = Logger.getLogger(ConfigStorage.class.getName());

    public final File CONFIG_DIR = new File(Minecraft.getInstance().gameDir, "\\expensive\\configs");
    public final File AUTOCFG_DIR = new File(CONFIG_DIR, "default.json");

    public final JsonParser jsonParser = new JsonParser();
    private Config currentConfig; // Track the currently loaded config

    public void init() throws IOException {
        setupFolder();
        loadLatestConfig();
        // Register shutdown hook to save current config on exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveCurrentConfigOnShutdown, "ConfigShutdownHook"));
    }

    public void setupFolder() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        } else if (AUTOCFG_DIR.exists()) {
            loadConfiguration("default");
            System.out.println("Loaded system configuration...");
        } else {
            System.out.println("Creating system configuration...");
            try {
                AUTOCFG_DIR.createNewFile();
                saveConfiguration("default");
                System.out.println("Created system configuration!");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create system configuration file", e);
            }
        }
    }

    public boolean isEmpty() {
        return getConfigs().isEmpty();
    }

    public List<Config> getConfigs() {
        List<Config> configs = new ArrayList<>();
        File[] configFiles = CONFIG_DIR.listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".json")) {
                    String configName = configFile.getName().replace(".json", "");
                    Config config = findConfig(configName);
                    if (config != null) {
                        configs.add(config);
                    }
                }
            }
        }

        return configs;
    }

    public void loadLatestConfig() {
        List<Config> configs = getConfigs();
        if (configs.isEmpty()) {
            logger.log(Level.WARNING, "No configurations found");
            return;
        }
        Config latestConfig = configs.stream()
                .max(Comparator.comparingLong(Config::getLastUpdated))
                .orElse(null);

        if (latestConfig != null) {
            loadConfiguration(latestConfig.getName());
            logger.log(Level.INFO, "Loaded latest configuration: {0}", latestConfig.getName());
        }
    }

    public void loadConfiguration(String configuration) {
        Config config = findConfig(configuration);
        if (config == null) {
            logger.log(Level.WARNING, "Configuration {0} not found", configuration);
            return;
        }
        try (FileReader reader = new FileReader(config.getFile())) {
            JsonElement element = jsonParser.parse(reader);
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                config.loadConfig(object);
                currentConfig = config; // Set the current config
            } else {
                logger.log(Level.WARNING, "Configuration file {0} is empty or contains invalid JSON", configuration);
                config.loadConfig(new JsonObject());
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Configuration file not found: {0}", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading configuration {0}", e);
        }
    }

    public void saveCurrentConfigOnShutdown() {
        if (currentConfig != null) {
            saveConfiguration(currentConfig.getName());
            logger.log(Level.INFO, "Auto-saved current configuration on shutdown: {0}", currentConfig.getName());
        } else {
            logger.log(Level.WARNING, "No current configuration to save on shutdown");
        }
    }

    public static void saveConfiguration(String configuration) {
        Config config = new Config(configuration);
        JsonObject configData = config.saveConfig();
        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(configData);
        try {
            FileWriter writer = new FileWriter(config.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
            logger.log(Level.INFO, "Saved configuration: {0}", configuration);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save configuration: {0}", e.getMessage());
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Fatal error in config saving!", e);
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        File configFile = new File(CONFIG_DIR, configName + ".json");
        if (configFile.exists()) {
            Config config = new Config(configName);
            try (FileReader reader = new FileReader(configFile)) {
                JsonElement element = jsonParser.parse(reader);
                if (element.isJsonObject() && element.getAsJsonObject().has("lastUpdated")) {
                    config.setLastUpdated(element.getAsJsonObject().get("lastUpdated").getAsLong());
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error reading config timestamp: {0}", e.getMessage());
            }
            return config;
        }
        return null;
    }

    // Getter for current config
    public Config getCurrentConfig() {
        return currentConfig;
    }

    // Setter for current config (e.g., when switching configs)
    public void setCurrentConfig(Config config) {
        this.currentConfig = config;
    }
}