package com.acbelter;

import com.acbelter.command.Command;
import com.acbelter.command.HelpCommand;
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
    private InputHandler inputHandler;
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
        addCommand(new HelpCommand("/help",
                "Use /help to show supported commands.", out, commands));
        addCommand(new LoginCommand("/login",
                "Use /login to create new user or /login <login> <password> to authorize."));
        addCommand(new QuitCommand("/quit",
                "Use /quit to quit from messenger."));
        inputHandler = new InputHandler(in, out, commands) {
            @Override
            protected void handleString(String string) {}

            @Override
            protected void interrupted() {}
        };
    }

    private void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public void start() {
        try {
            authService = new AuthService(in, out, userDataStorage);
            inputHandler.handle();
        } catch (IOException e) {
            out.println("Unable to create authorization service.");
        }
    }



    private class LoginCommand extends Command {
        public LoginCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            User user;
            switch (args.length) {
                case 0: {
                    user = authService.createUser();
                    break;
                }
                case 2: {
                    user = authService.loginUser(args[0], args[1]);
                    break;
                }
                default: {
                    out.println("Incorrect number of arguments.\n" + description);
                    return;
                }
            }

            if (user != null) {
                chatService = new ChatService(user, in, out, userDataStorage, chatHistoryStorage);
                chatService.start();
            } else {
                out.println("Unable to authorize.");
            }
        }
    }

    private class QuitCommand extends Command {
        public QuitCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            inputHandler.interrupt();
        }
    }

    public static void main(String[] args) {
        Messenger messenger = new Messenger(
                new Scanner(System.in),
                System.out,
                new UserDataFileStorage("userdata"),
                new ChatHistoryFileStorage("history"));
        messenger.start();
    }
}
