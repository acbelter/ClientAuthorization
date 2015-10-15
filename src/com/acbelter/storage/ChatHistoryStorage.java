package com.acbelter.storage;

import com.acbelter.User;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ChatHistoryStorage {
    void loadChatHistory(UUID chatId) throws IOException;
    boolean addMessage(UUID chatId, User fromUser, String message, long creationTime);
    List<String> getHistory(UUID chatId);
    List<String> getRecentMessages(int count);
    void clearHistory(UUID chatId);
}
