package com.acbelter.storage;

import com.acbelter.User;

import java.io.IOException;

public interface HistoryStorage {
    void loadHistory() throws IOException;
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean isUserExists(String name);
    User getUser(String name);
}
