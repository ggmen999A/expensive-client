package by.algorithm.alpha.api.modules.settings;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> setVisible(Supplier<Boolean> bool);
}