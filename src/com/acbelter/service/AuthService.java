package com.acbelter.service;

import com.acbelter.ConsoleReader;
import com.acbelter.HashUtil;
import com.acbelter.User;
import com.acbelter.storage.UserDataStorage;

import java.io.Console;
import java.io.IOException;

public class AuthService {
    private UserDataStorage userDataStorage;
    private Console console;

    public enum State {
        START,
        CREATE_USER,
        LOGIN_USER
    }

    public AuthService(UserDataStorage userDataStorage) {
        this.userDataStorage = userDataStorage;
    }

    private boolean validatePassword(User user, String password) {
        if (user == null || user.getPasswordHash() == null || password == null) {
            return false;
        }

        return user.getPasswordHash().equalsIgnoreCase(HashUtil.generateHash(password));
    }

    public void init() throws IOException {
        console = System.console();
        if (console == null) {
            throw new IOException("No access to system console.");
        }

        try {
            userDataStorage.loadUserData();
        } catch (IOException e) {
            throw new IOException("Failed to load user data.");
        }
    }

    public User authorize() {
        State authState = State.START;

        while (true) {
            switch (authState) {
                case START: {
                    console.printf("Do you want to create new user? (y/n or yes/no)\n");
                    String answer = console.readLine().trim();
                    if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                        authState = State.CREATE_USER;
                    } else if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")) {
                        authState = State.LOGIN_USER;
                    }
                    break;
                }
                case CREATE_USER: {
                    String username = ConsoleReader.readUsername(console);
                    if (username.isEmpty()) {
                        authState = State.START;
                        continue;
                    }

                    if (userDataStorage.isUserExists(username)) {
                        console.printf("This user already exists.\n");
                        continue;
                    }

                    String password = ConsoleReader.readNewPassword(console);
                    User newUser = new User(username);
                    String passwordHash = HashUtil.generateHash(password);
                    if (passwordHash != null) {
                        newUser.setPasswordHash(passwordHash);
                        userDataStorage.addUser(newUser);
                        console.printf("New user is created.\n");
                        return newUser;
                    } else {
                        console.printf("Unable to create new user.\n");
                        authState = State.LOGIN_USER;
                    }
                    break;
                }
                case LOGIN_USER: {
                    String username = ConsoleReader.readUsername(console);
                    if (username.isEmpty()) {
                        authState = State.START;
                        continue;
                    }

                    if (userDataStorage.isUserExists(username)) {
                        User user = userDataStorage.getUser(username);
                        while (true) {
                            String password = ConsoleReader.readPassword(console);
                            if (validatePassword(user, password)) {
                                return user;
                            } else {
                                console.printf("Invalid password. Try again.\n");
                            }
                        }
                    } else {
                        console.printf("User with this name does not exist.\n");
                        authState = State.START;
                    }
                    break;
                }
            }
        }
    }
}
