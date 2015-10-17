package com.acbelter;

import com.acbelter.command.Command;

import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

public abstract class InputHandler {
    protected Scanner in;
    protected PrintStream out;
    protected Map<String, Command> commandMap;
    protected boolean interrupt;

    public InputHandler(Scanner in, PrintStream out, Map<String, Command> commandMap) {
        this.in = in;
        this.out = out;
        this.commandMap = commandMap;
    }

    public void handle() {
        interrupt = false;
        while (!interrupt) {
            String line = in.nextLine();
            if (Command.isCommand(line)) {
                String name = Command.parseName(line);
                String[] args = Command.parseArgs(line);
                if (commandMap.containsKey(name)) {
                    commandMap.get(name).execute(args);
                } else {
                    out.println("Unknown command. Try again.");
                }
            } else {
                handleString(line);
            }
        }
        interrupted();
    }

    protected abstract void handleString(String string);
    protected abstract void interrupted();

    public void interrupt() {
        interrupt = true;
    }
}
