package by.algorithm.alpha.api.command.impl;

import by.algorithm.alpha.api.command.Logger;
import by.algorithm.alpha.system.utils.client.IMinecraft;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinecraftLogger implements Logger, IMinecraft {
    @Override
    public void log(String message) {
        print(message);
    }
}
