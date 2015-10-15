package com.acbelter.storage;

import com.acbelter.User;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ChatHistoryFileStorage implements ChatHistoryStorage {
    @Override
    public void loadChatHistory(UUID chatId) throws IOException {

    }

    @Override
    public boolean addMessage(UUID chatId, User fromUser, String message, long creationTime) {
        return false;
    }

    @Override
    public List<String> getHistory(UUID chatId) {
        return null;
    }

    @Override
    public List<String> getRecentMessages(int count) {
        return null;
    }

    @Override
    public void clearHistory(UUID chatId) {

    }
}
