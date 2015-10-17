package com.acbelter.command;

import java.io.PrintStream;
import java.util.Map;

public class HelpCommand extends Command {
    private PrintStream out;
    private Map<String, Command> commands;

    public HelpCommand(String name, String description, PrintStream out, Map<String, Command> commands) {
        super(name, description);
        this.out = out;
        this.commands = commands;
    }

    @Override
    public void execute(String[] args) {
        out.println("Supported commands:");
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            out.printf("\t%s - %s\n", entry.getKey(), entry.getValue().getDescription());
        }
    }
}
