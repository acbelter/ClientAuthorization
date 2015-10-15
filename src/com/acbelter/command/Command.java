package com.acbelter.command;

import java.util.Arrays;

public abstract class Command {
    protected String description;

    public Command(String description) {
        this.description = description;
    }

    public static boolean isCommand(String line) {
        return line.matches("^/\\S+.*$");
    }

    public static String parseName(String line) {
        if (!isCommand(line)) {
            return null;
        }

        return line.split(" ")[0];
    }

    public static String[] parseArgs(String line) {
        if (!isCommand(line)) {
            return null;
        }

        String[] parts = line.split(" ");
        if (parts.length == 1) {
            return new String[0];
        } else {
            return Arrays.copyOfRange(parts, 1, parts.length);
        }
    }

    public abstract void execute(String[] args);

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
