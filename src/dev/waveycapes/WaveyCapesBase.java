package dev.waveycapes;

import dev.waveycapes.config.Config;

public class WaveyCapesBase {
    public static WaveyCapesBase INSTANCE;
    public static Config config = new Config(); // Initialize statically

    public void init() {
        INSTANCE = this;
        // Optionally, allow re-initialization or configuration loading here
        if (config == null) {
            config = new Config();
        }
    }
}