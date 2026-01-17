package by.algorithm.alpha.api.command.impl;

import by.algorithm.alpha.api.command.AdviceCommandFactory;
import by.algorithm.alpha.api.command.CommandProvider;
import by.algorithm.alpha.api.command.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommandFactoryImpl implements AdviceCommandFactory {

    final Logger logger;
    @Override
    public AdviceCommand adviceCommand(CommandProvider commandProvider) {
        return new AdviceCommand(commandProvider, logger);
    }
}
