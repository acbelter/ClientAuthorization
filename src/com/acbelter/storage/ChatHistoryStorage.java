package com.acbelter.storage;

import com.acbelter.User;

import java.io.IOException;
import java.util.List;

public interface ChatHistoryStorage {
    void loadHistory() throws IOException;
    void saveHistory() throws IOException;
    void appendMessage(User fromUser, String message, long creationTime);
    List<String> getHistory();
}
