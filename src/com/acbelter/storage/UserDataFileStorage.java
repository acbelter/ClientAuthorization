package com.acbelter.storage;

import com.acbelter.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class UserDataFileStorage implements UserDataStorage {
    protected String storageFilename;
    protected Map<String, User> usersMap;

    public UserDataFileStorage(String storageFilename) {
        this.storageFilename = storageFilename;
        usersMap = new HashMap<>();
    }

    @Override
    public void loadUsersData() throws IOException {
        usersMap.clear();

        if (Files.notExists(Paths.get(storageFilename))) {
            Files.createFile(Paths.get(storageFilename));
        }

        for (String line : Files.readAllLines(Paths.get(storageFilename))) {
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
        if (user == null || user.getLogin() == null || user.getPasswordHash() == null) {
            return false;
        }

        try {
            String userData = user.getLogin() + " " + user.getPasswordHash() + "\n";
            Files.write(Paths.get(storageFilename), userData.getBytes(),
                    StandardOpenOption.APPEND);
            usersMap.put(user.getLogin(), user);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null ||
                user.getLogin() == null ||
                user.getPasswordHash() == null ||
                !usersMap.containsKey(user.getLogin())) {
            return false;
        }


        User oldUser = usersMap.put(user.getLogin(), user);
        try {
            Files.write(Paths.get(storageFilename), getUserDataString().getBytes());
        } catch (IOException e) {
            usersMap.put(oldUser.getLogin(), oldUser);
            return false;
        }

        return true;
    }

    private String getUserDataString() {
        StringBuilder builder = new StringBuilder();
        for (User user : usersMap.values()) {
            builder.append(user.getLogin()).append(" ").append(user.getPasswordHash()).append("\n");
        }
        return builder.toString();
    }

    @Override
    public boolean isUserExists(String login) {
        return login != null && usersMap.containsKey(login);
    }

    @Override
    public User getUser(String login) {
        return usersMap.get(login);
    }
}
