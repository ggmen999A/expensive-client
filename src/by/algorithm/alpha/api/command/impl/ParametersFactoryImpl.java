package by.algorithm.alpha.api.command.impl;

import by.algorithm.alpha.api.command.Parameters;
import by.algorithm.alpha.api.command.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}
