package com.acbelter.auth;

import com.acbelter.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class UserDataStorage {
    private static final String USER_DATA_FILE_NAME = "userdata";

    protected Map<String, User> usersMap;

    public UserDataStorage() {
        usersMap = new HashMap<>();
    }

    public void loadUserData() throws IOException {
        usersMap.clear();

        if (!Files.exists(Paths.get(USER_DATA_FILE_NAME))) {
            Files.createFile(Paths.get(USER_DATA_FILE_NAME));
        }

        for (String line : Files.readAllLines(Paths.get(USER_DATA_FILE_NAME))) {
            if (!line.isEmpty()) {
                String[] data = line.split(" ");
                if (data.length != 2) {
                    continue;
                }

                User newUser = new User(data[0], data[1]);
                usersMap.put(data[0], newUser);
            }
        }
    }

    public boolean addUser(User user) {
        if (user == null || user.getName() == null || user.getPasswordHash() == null) {
            return false;
        }

        try {
            String userData = user.getName() + " " + user.getPasswordHash() + "\n";
            Files.write(Paths.get(USER_DATA_FILE_NAME), userData.getBytes(), StandardOpenOption.APPEND);
            usersMap.put(user.getName(), user);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean isUserExists(String name) {
        return name != null && usersMap.containsKey(name);
    }

    public User getUser(String name) {
        return usersMap.get(name);
    }
}
