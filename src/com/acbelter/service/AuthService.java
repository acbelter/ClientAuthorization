package com.acbelter.service;

import com.acbelter.User;
import com.acbelter.storage.UserDataStorage;

import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AuthService {
    private Scanner in;
    private PrintStream out;
    private UserDataStorage userDataStorage;

    public AuthService(Scanner in,
                       PrintStream out,
                       UserDataStorage userDataStorage) throws IOException {
        this.in = in;
        this.out = out;
        this.userDataStorage = userDataStorage;

        try {
            userDataStorage.loadUsersData();
        } catch (IOException e) {
            throw new IOException("Failed to load users data.");
        }
    }

    public User loginUser(final String login, final String password) {
        if (login == null || login.isEmpty()) {
            return null;
        }

        if (userDataStorage.isUserExists(login)) {
            User user = userDataStorage.getUser(login);
            if (validatePassword(user, password)) {
                return user;
            } else {
                final int attempts = 3;
                for (int i = 0; i < attempts; i++) {
                    out.println("Invalid password. Try again. Attempts: " + (attempts - i));
                    out.println("Password:");
                    String pass = in.nextLine().trim();
                    if (pass.isEmpty()) {
                        return null;
                    }
                    if (validatePassword(user, pass)) {
                        return user;
                    }
                }
                return null;
            }
        } else {
            out.println("User with this login does not exist.");
            out.println("Do you want to create new user? (y/n or yes/no)");
            String answer = in.nextLine();
            if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                return createUser();
            }
        }
        return null;
    }

    public User createUser() {
        String login;
        while (true) {
            out.println("New login:");
            login = in.nextLine().trim();

            if (login.isEmpty()) {
                return null;
            }

            if (userDataStorage.isUserExists(login)) {
                out.println("This user already exists.");
            } else {
                break;
            }
        }

        String password = readNewPassword();
        User newUser = new User(login);
        String passwordHash = generateHash(password);
        if (passwordHash != null) {
            newUser.setPasswordHash(passwordHash);
            userDataStorage.addUser(newUser);
            out.println("New user is created.");
            return newUser;
        } else {
            out.println("Unable to create new user.\n");
            return null;
        }
    }

    private String readNewPassword() {
        String newPassword;
        String confirmedNewPassword;
        while (true) {
            out.println("New password:");
            newPassword = in.nextLine();
            if (newPassword.trim().isEmpty()) {
                continue;
            }
            out.println("Confirm new password:");
            confirmedNewPassword = in.nextLine();
            if (newPassword.equals(confirmedNewPassword)) {
                return newPassword;
            } else {
                out.println("Passwords are not equal. Try again.");
            }
        }
    }

    public static boolean validatePassword(User user, String password) {
        if (user == null || user.getPasswordHash() == null || password == null) {
            return false;
        }

        return user.getPasswordHash().equalsIgnoreCase(generateHash(password));
    }

    public static String generateHash(String data) {
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
}
