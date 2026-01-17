package by.algorithm.alpha.api.command;

public interface Command {
    void execute(Parameters parameters);

    String name();

    String description();
}
