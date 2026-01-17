package by.algorithm.alpha.api.rotation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class RotationRegistry {
    private static final Map<String, RotationStrategy> strategies = new HashMap<>();

    public static void register(RotationStrategy strategy) {
        if (strategy == null || strategy.getName() == null) return;
        strategies.put(strategy.getName(), strategy);
    }

    public static RotationStrategy get(String name) {
        return strategies.getOrDefault(name, strategies.get("Default"));
    }

    public static Map<String, RotationStrategy> getAll() {
        return Collections.unmodifiableMap(strategies);
    }

    // Регистрация стандартных стратегий при загрузке класса
    static {

    }
}
