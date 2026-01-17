package by.algorithm.alpha.api.command.impl;

import by.algorithm.alpha.api.command.Logger;

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("message = " + message);
    }
}
