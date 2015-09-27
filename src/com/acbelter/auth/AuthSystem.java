package com.acbelter.auth;

import com.acbelter.User;
import com.acbelter.Utils;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class AuthSystem {
    private static final String USER_DATA_FILE_NAME = "userdata";

    protected Map<String, User> usersMap;

    public AuthSystem() {
        usersMap = new HashMap<>();
    }

    public void loadUserData() throws IOException {
        usersMap.clear();

        if (!new File(USER_DATA_FILE_NAME).exists()) {
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

    public boolean appendUser(User user) {
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

    public boolean isUserHasPassword(User user, String password) {
        if (user == null || user.getPasswordHash() == null || password == null) {
            return false;
        }

        return user.getPasswordHash().equalsIgnoreCase(Utils.generatePasswordHash(password));
    }

    public User getUser(String name) {
        return usersMap.get(name);
    }


    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.out.println("No access to system console.\n");
            return;
        }

        AuthSystem authSystem = new AuthSystem();
        try {
            authSystem.loadUserData();
        } catch (IOException e) {
            console.printf("%s", "Failed to load user data from file.\n");
            return;
        }

        String name;
        while (true) {
            console.printf("%s", "Username:");
            name = console.readLine().trim();
            if (name.isEmpty()) {
                console.printf("%s", "Username is empty. Try again.\n");
            } else {
                break;
            }
        }

        if (!authSystem.isUserExists(name)) {
            String newPassword;
            String confirmedNewPassword;
            while (true) {
                console.printf("%s", "New password:");
                newPassword = new String(console.readPassword());
                if (newPassword.trim().isEmpty()) {
                    continue;
                }
                console.printf("%s", "Confirm new password:");
                confirmedNewPassword = new String(console.readPassword());
                if (newPassword.equals(confirmedNewPassword)) {
                    break;
                } else {
                    console.printf("%s", "Passwords are not equal. Try again.\n");
                }
            }

            User newUser = User.createUser(name, newPassword);
            authSystem.appendUser(newUser);
            console.printf("%s", "New user is created.\n");
        } else {
            User user = authSystem.getUser(name);
            String password;
            while (true) {
                console.printf("%s", "Password:");
                password = new String(console.readPassword());
                if (password.trim().isEmpty()) {
                    continue;
                }
                if (authSystem.isUserHasPassword(user, password)) {
                    console.printf("%s", "Success login.\n");
                    break;
                } else {
                    console.printf("%s", "Invalid password. Try again.\n");
                }
            }
        }
    }
}
