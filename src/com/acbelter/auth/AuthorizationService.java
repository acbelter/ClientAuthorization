package com.acbelter.auth;

import com.acbelter.User;

import java.io.Console;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthorizationService {
    protected UserDataStorage userDataStorage;

    public AuthorizationService() throws IOException {
        userDataStorage = new UserDataStorage();
        userDataStorage.loadUserData();
    }

    public boolean validatePassword(User user, String password) {
        if (user == null || user.getPasswordHash() == null || password == null) {
            return false;
        }

        return user.getPasswordHash().equalsIgnoreCase(generateHash(password));
    }

    public void run() {
        Console console = System.console();
        if (console == null) {
            System.out.println("No access to system console.\n");
            return;
        }

        try {
            userDataStorage.loadUserData();
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

        if (!userDataStorage.isUserExists(name)) {
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

            User newUser = new User(name);
            String passwordHash = generateHash(newPassword);
            if (passwordHash != null) {
                newUser.setPasswordHash(passwordHash);
                userDataStorage.addUser(newUser);
                console.printf("%s", "New user is created.\n");
            } else {
                console.printf("%s", "Unable to create new user.\n");
            }
        } else {
            User user = userDataStorage.getUser(name);
            String password;
            while (true) {
                console.printf("%s", "Password:");
                password = new String(console.readPassword());
                if (password.trim().isEmpty()) {
                    continue;
                }
                if (validatePassword(user, password)) {
                    console.printf("%s", "Successful authorization.\n");
                    break;
                } else {
                    console.printf("%s", "Invalid password. Try again.\n");
                }
            }
        }
    }

    private static String generateHash(String data) {
        if (data == null) {
            return null;
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        md.update(data.getBytes());

        byte[] byteData = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : byteData) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        AuthorizationService authService;
        try {
            authService = new AuthorizationService();
            authService.run();
        } catch (IOException e) {
            System.out.println("Unable to create authorization service.");
        }
    }
}
