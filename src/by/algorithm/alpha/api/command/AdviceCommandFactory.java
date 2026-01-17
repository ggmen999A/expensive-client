package by.algorithm.alpha.api.command;

import by.algorithm.alpha.api.command.impl.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
