package com.acbelter.storage;

import com.acbelter.User;

import java.io.IOException;

public interface UserDataStorage {
    void loadUsersData() throws IOException;
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean isUserExists(String login);
    User getUser(String login);
}
