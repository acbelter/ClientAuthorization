package com.acbelter;

import com.acbelter.command.Command;
import com.acbelter.service.AuthService;
import com.acbelter.service.ChatService;
import com.acbelter.storage.ChatHistoryFileStorage;
import com.acbelter.storage.ChatHistoryStorage;
import com.acbelter.storage.UserDataFileStorage;
import com.acbelter.storage.UserDataStorage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Messenger {
    private Scanner in;
    private PrintStream out;
    private InputHandler mainHandler;
    private AuthService authService;
    private ChatService chatService;
    private UserDataStorage userDataStorage;
    private ChatHistoryStorage chatHistoryStorage;
    private Map<String, Command> commands;

    public Messenger(Scanner in,
                     PrintStream out,
                     UserDataStorage userDataStorage,
                     ChatHistoryStorage chatHistoryStorage) {
        this.in = in;
        this.out = out;
        this.userDataStorage = userDataStorage;
        this.chatHistoryStorage = chatHistoryStorage;

        commands = new TreeMap<>();
        commands.put("/login", new LoginCommand("Use /login to create new user or /login <login> <password> to authorize."));
        commands.put("/help", new HelpCommand("Use this command to show supported commands."));
        mainHandler = new MainInputHandler(in, out, commands);
    }

    public void start() {
        try {
            authService = new AuthService(in, out, userDataStorage);
            mainHandler.handle();
        } catch (IOException e) {
            out.println("Unable to create authorization service.");
        }
    }

    private class MainInputHandler extends InputHandler {
        private PrintStream out;

        public MainInputHandler(Scanner scanner, PrintStream out, Map<String, Command> commandMap) {
            super(scanner, commandMap);
            this.out = out;
        }

        @Override
        public void handle() {
            while (true) {
                String line = scanner.nextLine();
                if (Command.isCommand(line)) {
                    String name = Command.parseName(line);
                    String[] args = Command.parseArgs(line);
                    if (commandMap.containsKey(name)) {
                        commandMap.get(name).execute(args);
                    } else {
                        out.println("Unknown command. Try again.");
                    }
                }
            }
        }
    }

    private class LoginCommand extends Command {
        public LoginCommand(String description) {
            super(description);
        }

        @Override
        public void execute(String[] args) {
            User user;
            if (args.length == 0) {
                user = authService.createUser();
            } else if (args.length == 2) {
                user = authService.loginUser(args[0], args[1]);
            } else {
                out.println("Unknown command syntax. Use /login or /login <login> <password>");
                return;
            }

            if (user != null) {
                chatService = new ChatService(user, in, out, chatHistoryStorage);
                chatService.start();
            } else {
                out.println("Unable to authorize.");
            }
        }
    }

    private class HelpCommand extends Command {
        public HelpCommand(String description) {
            super(description);
        }

        @Override
        public void execute(String[] args) {
            out.println("Supported commands:");
            for (Map.Entry<String, Command> entry : commands.entrySet()) {
                out.printf("\t%s - %s\n", entry.getKey(), entry.getValue().getDescription());
            }
        }
    }

    public static void main(String[] args) {
        Messenger messenger = new Messenger(
                new Scanner(System.in),
                System.out,
                new UserDataFileStorage("userdata"),
                new ChatHistoryFileStorage());
        messenger.start();
    }
}
