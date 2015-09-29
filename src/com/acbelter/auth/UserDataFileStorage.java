package com.acbelter.auth;

import com.acbelter.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class UserDataFileStorage implements UserDataStorage {
    private static final String USER_DATA_FILE_NAME = "userdata";

    protected Map<String, User> usersMap;

    public UserDataFileStorage() {
        usersMap = new HashMap<>();
    }

    @Override
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

    @Override
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

    @Override
    public boolean updateUser(User user) {
        if (user == null ||
                user.getName() == null ||
                user.getPasswordHash() == null ||
                !usersMap.containsKey(user.getName())) {
            return false;
        }


        User oldUser = usersMap.put(user.getName(), user);
        try {
            Files.write(Paths.get(USER_DATA_FILE_NAME), getUserDataString().getBytes());
        } catch (IOException e) {
            usersMap.put(oldUser.getName(), oldUser);
            return false;
        }

        return true;
    }

    private String getUserDataString() {
        StringBuilder builder = new StringBuilder();
        for (User user : usersMap.values()) {
            builder.append(user.getName()).append(" ").append(user.getPasswordHash()).append("\n");
        }
        return builder.toString();
    }

    @Override
    public boolean isUserExists(String name) {
        return name != null && usersMap.containsKey(name);
    }

    @Override
    public User getUser(String name) {
        return usersMap.get(name);
    }
}
