package by.algorithm.alpha.api.command;

import by.algorithm.alpha.api.command.impl.DispatchResult;

public interface CommandDispatcher  {
    DispatchResult dispatch(String command);
}