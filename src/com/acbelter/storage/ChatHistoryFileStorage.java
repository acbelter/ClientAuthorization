package com.acbelter.storage;

import com.acbelter.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatHistoryFileStorage implements ChatHistoryStorage {
    private String historyFilename;
    private List<String> history;

    public ChatHistoryFileStorage(String historyFilename) {
        this.historyFilename = historyFilename;
        history = new ArrayList<>();
    }

    @Override
    public void loadHistory() throws IOException {
        history.clear();

        if (Files.notExists(Paths.get(historyFilename))) {
            Files.createFile(Paths.get(historyFilename));
        }

        history.addAll(Files.readAllLines(Paths.get(historyFilename)).stream().collect(Collectors.toList()));
    }

    @Override
    public void saveHistory() throws IOException {
        Files.write(Paths.get(historyFilename), history);
    }

    @Override
    public void appendMessage(User fromUser, String message, long creationTime) {
        history.add(fromUser.getNick() + " " + new Timestamp(creationTime) + " " + message);
    }

    @Override
    public List<String> getHistory() {
        return history;
    }
}
