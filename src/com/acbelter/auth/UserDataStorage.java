package com.acbelter.auth;

import com.acbelter.User;

import java.io.IOException;

public interface UserDataStorage {
    void loadUserData() throws IOException;
    boolean addUser(User user);
    boolean updateUser(User user);
    boolean isUserExists(String name);
    User getUser(String name);
}
