package com.acbelter.service;

import com.acbelter.InputHandler;
import com.acbelter.User;
import com.acbelter.command.Command;
import com.acbelter.command.HelpCommand;
import com.acbelter.storage.ChatHistoryStorage;
import com.acbelter.storage.UserDataStorage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class ChatService {
    private PrintStream out;
    private User user;
    private UserDataStorage userDataStorage;
    private ChatHistoryStorage chatHistoryStorage;
    private List<String> history;
    private InputHandler inputHandler;
    private Map<String, Command> commands;

    public ChatService(User user,
                       Scanner in,
                       PrintStream out,
                       UserDataStorage userDataStorage,
                       ChatHistoryStorage chatHistoryStorage) {
        this.out = out;
        this.user = user;
        this.userDataStorage = userDataStorage;
        this.chatHistoryStorage = chatHistoryStorage;

        commands = new TreeMap<>();
        addCommand(new HelpCommand("/help",
                "Use /help to show supported commands.", out, commands));
        addCommand(new SetNickCommand("/user",
                "Use /user <nick> to set nick."));
        addCommand(new ChangePasswordCommand("/newpass",
                "Use /newpass <current_pass> <new_pass> <confirm_new_pass> to change password."));
        addCommand(new HistoryCommand("/history",
                "Use /history to show messages from current user. Use /history <N> to show last N messages."));
        addCommand(new FindCommand("/find",
                "Use /find <regex> to filter messages by regular expression."));
        addCommand(new QuitCommand("/quit",
                "Use /quit to quit from chat."));
        inputHandler = new InputHandler(in, out, commands) {
            @Override
            protected void handleString(String string) {
                chatHistoryStorage.appendMessage(user, string, System.currentTimeMillis());
            }

            @Override
            protected void interrupted() {
                out.println(user.getNick() + " leaves from chat.");
                user.resetNick();
            }
        };
    }

    private void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public void start() {
        try {
            chatHistoryStorage.loadHistory();
            history = chatHistoryStorage.getHistory();
        } catch (IOException e) {
            out.println("Unable to load history.");
        }
        out.println(user.getNick() + " joins to chat.");
        inputHandler.handle();
    }

    private List<String> getRecentMessages(int count) {
        if (count < 0 || history.size() <= count) {
            return history;
        } else {
            return history.subList(history.size() - count, history.size());
        }
    }

    private List<String> findMessagesByNick(String nick) {
        return history
                .stream()
                .filter(line -> line.startsWith(nick + " "))
                .collect(Collectors.toList());
    }

    private List<String> findMessagesByRegex(String regex) throws PatternSyntaxException {
        return history
                .stream()
                .filter(Pattern.compile(regex).asPredicate())
                .collect(Collectors.toList());
    }



    private class SetNickCommand extends Command {
        public SetNickCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            if (args.length == 1) {
                user.setNick(args[0]);
                out.println("Now you nick is: " + args[0]);
            } else {
                out.println("Incorrect number of arguments.\n" + description);
            }
        }
    }

    private class ChangePasswordCommand extends Command {
        public ChangePasswordCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            if (args.length == 3) {
                if (AuthService.validatePassword(user, args[0])) {
                    if (args[1].equals(args[2])) {
                        String oldPasswordHash = user.getPasswordHash();
                        user.setPasswordHash(AuthService.generateHash(args[1]));
                        if (userDataStorage.updateUser(user)) {
                            out.println("Password is updated.");
                        } else {
                            user.setPasswordHash(oldPasswordHash);
                            out.println("Unable to update password.");
                        }
                    } else {
                        out.println("Two new passwords are not equal.");
                    }
                } else {
                    out.println("Incorrect current password.");
                }
            } else {
                out.println("Incorrect number of arguments.\n" + description);
            }
        }
    }

    private class HistoryCommand extends Command {
        public HistoryCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            switch (args.length) {
                case 0: {
                    for (String line : findMessagesByNick(user.getNick())) {
                        out.println("\t" + line);
                    }
                    break;
                }
                case 1: {
                    int count = 0;
                    try {
                        count = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        // Use default value
                    }
                    for (String line : getRecentMessages(count)) {
                        out.println("\t" + line);
                    }
                    break;
                }
                default: {
                    out.println("Incorrect number of arguments.\n" + description);
                    return;
                }
            }
        }
    }

    private class FindCommand extends Command {
        public FindCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            if (args.length == 1) {
                for (String line : findMessagesByRegex(args[0])) {
                    out.println("\t" + line);
                }
            } else {
                out.println("Incorrect number of arguments.\n" + description);
            }
        }
    }

    private class QuitCommand extends Command {
        public QuitCommand(String name, String description) {
            super(name, description);
        }

        @Override
        public void execute(String[] args) {
            try {
                chatHistoryStorage.saveHistory();
            } catch (IOException e) {
                out.println("Unable to save history.");
            }
            inputHandler.interrupt();
        }
    }
}
