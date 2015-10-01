package com.acbelter.auth;

import com.acbelter.User;
import com.acbelter.auth.command.*;

import java.io.Console;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class AuthorizationService {
    protected UserDataStorage userDataStorage;
    protected Map<String, ServiceCommand> serviceCommands;

    private State currentState;

    private Console console;
    private String currentUserName;

    public enum State {
        STARTED,
        CREATE_USER,
        LOGIN_USER,
        UPDATE_PASSWORD,
        HANDLE_COMMAND,
        FINISHED
    }

    public AuthorizationService(UserDataStorage userDataStorage) throws IOException {
        this.userDataStorage = userDataStorage;
        userDataStorage.loadUserData();

        serviceCommands = new HashMap<>();
        addCommand(new HelpCommand(this, "help", "Use this command to show supported commands."));
        addCommand(new UpdatePasswordCommand(this, "chpwd", "Use this command to change password."));
        addCommand(new LogoutCommand(this, "logout", "Use this command to switch between users."));
        addCommand(new ExitCommand(this, "exit", "Use this command to exit."));
    }

    public TreeSet<ServiceCommand> getSupportedCommands() {
        return new TreeSet<>(serviceCommands.values());
    }

    private boolean validatePassword(User user, String password) {
        if (user == null || user.getPasswordHash() == null || password == null) {
            return false;
        }

        return user.getPasswordHash().equalsIgnoreCase(generateHash(password));
    }

    public void run() {
        console = System.console();
        if (console == null) {
            System.err.println("No access to system console.\n");
            return;
        }

        try {
            userDataStorage.loadUserData();
        } catch (IOException e) {
            console.printf("%s", "Failed to load user data.\n");
            return;
        }

        startAuthorizationLoop();
    }

    private void addCommand(ServiceCommand command) {
        serviceCommands.put(command.getName(), command);
    }

    private void startAuthorizationLoop() {
        currentState = State.STARTED;

        while (true) {
            switch (currentState) {
                case STARTED: {
                    currentUserName = null;
                    console.printf("%s", "Do you want to create new user? (yes/no)\n");
                    String answer = console.readLine().trim();
                    if (answer.equalsIgnoreCase("yes")) {
                        currentState = State.CREATE_USER;
                    } else if (answer.equalsIgnoreCase("no")) {
                        currentState = State.LOGIN_USER;
                    }
                    break;
                }
                case CREATE_USER: {
                    String username = readUsername();
                    if (userDataStorage.isUserExists(username)) {
                        console.printf("%s", "This user already exists.\n");
                        continue;
                    }

                    String password = readNewPassword();
                    User newUser = new User(username);
                    String passwordHash = generateHash(password);
                    if (passwordHash != null) {
                        newUser.setPasswordHash(passwordHash);
                        userDataStorage.addUser(newUser);
                        currentUserName = username;
                        console.printf("%s", "New user is created.\n");
                        console.printf("%s", "Hello, " + currentUserName + "!\n");
                        currentState = State.HANDLE_COMMAND;
                    } else {
                        console.printf("%s", "Unable to create new user.\n");
                    }
                    break;
                }
                case LOGIN_USER: {
                    String username = readUsername();
                    if (userDataStorage.isUserExists(username)) {
                        User user = userDataStorage.getUser(username);
                        while (true) {
                            String password = readPassword();
                            if (validatePassword(user, password)) {
                                console.printf("%s", "Hello, " + username + "!\n");
                                currentUserName = username;
                                currentState = State.HANDLE_COMMAND;
                                break;
                            } else {
                                console.printf("%s", "Invalid password. Try again.\n");
                            }
                        }
                    } else {
                        console.printf("%s", "User with this name does not exist.\n");
                        currentState = State.STARTED;
                    }
                    break;
                }
                case HANDLE_COMMAND: {
                    String commandData = console.readLine();
                    String[] commandParts = commandData.split(" ");
                    if (commandParts.length == 0) {
                        continue;
                    }

                    String commandName = commandParts[0];
                    if (!serviceCommands.containsKey(commandName)) {
                        console.printf("%s", "Unknown command. Try again.\n");
                        continue;
                    }

                    ServiceCommand command = serviceCommands.get(commandName);
                    if (commandParts.length == 1) {
                        command.execute();
                    } else {
                        String[] commandArgs = new String[commandParts.length - 1];
                        System.arraycopy(commandParts, 1, commandArgs, 0, commandParts.length - 1);
                        command.execute(commandArgs);
                    }
                    break;
                }
                case UPDATE_PASSWORD: {
                    if (currentUserName == null) {
                        console.printf("%s", "Unknown user.\n");
                        currentState = State.STARTED;
                        continue;
                    }

                    String newPassword = readNewPassword();
                    User user = new User(currentUserName);
                    String passwordHash = generateHash(newPassword);
                    if (passwordHash != null) {
                        user.setPasswordHash(passwordHash);
                        userDataStorage.updateUser(user);
                        console.printf("%s", "Password is updated.\n");
                    } else {
                        console.printf("%s", "Unable to update password.\n");
                    }
                    currentState = State.HANDLE_COMMAND;
                    break;
                }
                case FINISHED: {
                    currentUserName = null;
                    return;
                }
            }
        }
    }

    private String readUsername() {
        String username;
        while (true) {
            console.printf("%s", "Username:");
            username = console.readLine().trim();
            if (username.isEmpty()) {
                console.printf("%s", "Username is empty. Try again.\n");
            } else {
                return username;
            }
        }
    }

    private String readPassword() {
        String password;
        while (true) {
            console.printf("%s", "Password:");
            password = new String(console.readPassword());
            if (!password.trim().isEmpty()) {
                return password;
            }
        }
    }

    private String readNewPassword() {
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
                return newPassword;
            } else {
                console.printf("%s", "Passwords are not equal. Try again.\n");
            }
        }
    }

    public void setState(State state) {
        currentState = state;
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
            authService = new AuthorizationService(new UserDataFileStorage());
            authService.run();
        } catch (IOException e) {
            System.err.println("Unable to create authorization service.");
        }
    }
}
