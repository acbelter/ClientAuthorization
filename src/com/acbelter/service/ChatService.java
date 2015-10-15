package com.acbelter.service;

import com.acbelter.InputHandler;
import com.acbelter.User;
import com.acbelter.command.Command;
import com.acbelter.storage.ChatHistoryStorage;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ChatService {
    private PrintStream out;
    private User user;
    private ChatHistoryStorage chatHistoryStorage;
    private ChatInputHandler inputHandler;

    public ChatService(User user, Scanner in, PrintStream out, ChatHistoryStorage chatHistoryStorage) {
        this.out = out;
        this.user = user;
        this.chatHistoryStorage = chatHistoryStorage;
        inputHandler = new ChatInputHandler(in, new HashMap<>());
    }

    public void start() {
        inputHandler.handle();
    }

    private class ChatInputHandler extends InputHandler {
        public ChatInputHandler(Scanner scanner, Map<String, Command> commandMap) {
            super(scanner, commandMap);
        }

        @Override
        public void handle() {
            out.println("CHATTING");
        }
    }
}
