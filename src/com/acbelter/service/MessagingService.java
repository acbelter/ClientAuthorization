package com.acbelter.service;

import com.acbelter.ConsoleReader;
import com.acbelter.HashUtil;
import com.acbelter.User;
import com.acbelter.storage.HistoryStorage;
import com.acbelter.storage.UserDataStorage;

import java.io.Console;
import java.util.*;

public class MessagingService {
    private static final int STATUS_LOGOUT = 0;
    private static final int STATUS_EXIT = 1;
    private User user;
    private Console console;
    private Map<String, Command> serviceCommands;
    private UserDataStorage userDataStorage;
    private HistoryStorage historyStorage;

    private boolean logoutFlag;
    private boolean exitFlag;

    public MessagingService(User user, UserDataStorage userDataStorage, HistoryStorage historyStorage) {
        this.user = user;
        this.userDataStorage = userDataStorage;
        this.historyStorage = historyStorage;
        console = System.console();
        serviceCommands = new HashMap<>();

        addCommand(new HelpCommand("/help", "Use this command to show supported commands."));
        addCommand(new UpdatePasswordCommand("/chpwd", "Use this command to change password."));
        addCommand(new LogoutCommand("/logout", "Use this command to switch between users."));
        addCommand(new ExitCommand("/exit", "Use this command to exit."));
    }

    public int start() {
        logoutFlag = false;
        exitFlag = false;

        while (true) {
            String commandData = console.readLine();
            String[] commandParts = commandData.split(" ");
            if (commandParts.length == 0) {
                continue;
            }

            String commandName = commandParts[0];
            if (!serviceCommands.containsKey(commandName)) {
                console.printf("Unknown command. Try again.\n");
                continue;
            }

            Command command = serviceCommands.get(commandName);
            if (commandParts.length == 1) {
                command.execute(new String[0]);
            } else {
                String[] commandArgs = Arrays.copyOfRange(commandParts, 1, commandParts.length - 1);
                command.execute(commandArgs);
            }
        }
    }

    public Set<Command> getSupportedCommands() {
        Set<Command> commands = new TreeSet<>((cmd1, cmd2) -> {
            return cmd1.getName().compareTo(cmd2.getName());
        });
        commands.addAll(serviceCommands.values());
        return commands;
    }

    private void addCommand(Command command) {
        serviceCommands.put(command.getName(), command);
    }

    private boolean updatePassword() {
        String newPassword = ConsoleReader.readNewPassword(console);
        String passwordHash = HashUtil.generateHash(newPassword);
        if (passwordHash != null) {
            user.setPasswordHash(passwordHash);
            userDataStorage.updateUser(user);
            console.printf("Password is updated.\n");
            return true;
        } else {
            console.printf("Unable to update password.\n");
            return false;
        }
    }




    private class ExitCommand extends Command {
        public ExitCommand(String name) {
            super(name);
        }

        public ExitCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            exitFlag = true;
        }
    }

    private class HelpCommand extends Command {
        public HelpCommand(String name) {
            super(name);
        }

        public HelpCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            console.printf("Supported commands:\n");
            for (Command command : getSupportedCommands()) {
                console.printf("\t%s - %s\n", command.getName(), command.getDescription());
            }
        }
    }

    private class LogoutCommand extends Command {
        public LogoutCommand(String name) {
            super(name);
        }

        public LogoutCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            logoutFlag = true;
        }
    }

    private class UpdatePasswordCommand extends Command {
        public UpdatePasswordCommand(String name) {
            super(name);
        }

        public UpdatePasswordCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            updatePassword();
        }
    }
}
