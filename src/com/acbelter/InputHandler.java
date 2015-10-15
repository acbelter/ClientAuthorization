package com.acbelter;

import com.acbelter.command.Command;

import java.util.Map;
import java.util.Scanner;

public abstract class InputHandler {
    protected Scanner scanner;
    protected Map<String, Command> commandMap;

    public InputHandler(Scanner scanner, Map<String, Command> commandMap) {
        this.scanner = scanner;
        this.commandMap = commandMap;
    }

    public abstract void handle();
}
