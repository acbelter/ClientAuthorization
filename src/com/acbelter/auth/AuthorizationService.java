package com.acbelter.auth;

import com.acbelter.User;
import com.acbelter.auth.command.*;

import java.io.Console;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AuthorizationService {
    protected UserDataStorage userDataStorage;
    protected Map<String, Command> serviceCommands;

    private State currentState;

    private Console console;
    private String currentUserName;

    public enum State {
        START,
        CREATE_USER,
        LOGIN_USER,
        HANDLE_COMMAND,
        FINISH
    }

    public AuthorizationService(UserDataStorage userDataStorage) throws IOException {
        this.userDataStorage = userDataStorage;
        userDataStorage.loadUserData();

        serviceCommands = new HashMap<>();
        addCommand(new HelpCommand(this, "/help", "Use this command to show supported commands."));
        addCommand(new UpdatePasswordCommand(this, "/chpwd", "Use this command to change password."));
        addCommand(new LogoutCommand(this, "/logout", "Use this command to switch between users."));
        addCommand(new ExitCommand(this, "/exit", "Use this command to exit."));
    }

    public Set<Command> getSupportedCommands() {
        Set<Command> commands = new TreeSet<>((cmd1, cmd2) -> {
            return cmd1.getName().compareTo(cmd2.getName());
        });
        commands.addAll(serviceCommands.values());
        return commands;
    }

    private boolean validatePassword(User user, String password) {
        if (user == null || user.getPasswordHash() == null || password == null) {
            return false;
        }

        return user.getPasswordHash().equalsIgnoreCase(generateHash(password));
    }

    public void init() {
        console = System.console();
        if (console == null) {
            System.err.println("No access to system console.\n");
            return;
        }

        try {
            userDataStorage.loadUserData();
        } catch (IOException e) {
            console.printf("Failed to load user data.\n");
            return;
        }

        startAuthorizationLoop();
    }

    private void addCommand(Command command) {
        serviceCommands.put(command.getName(), command);
    }

    private void startAuthorizationLoop() {
        currentState = State.START;

        while (true) {
            System.out.println(currentState.name());
            switch (currentState) {
                case START: {
                    currentUserName = null;
                    console.printf("Do you want to create new user? (y/n or yes/no)\n");
                    String answer = console.readLine().trim();
                    if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                        currentState = State.CREATE_USER;
                    } else if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")) {
                        currentState = State.LOGIN_USER;
                    }
                    break;
                }
                case CREATE_USER: {
                    String username = readUsername();
                    if (username.isEmpty()) {
                        currentState = State.START;
                        continue;
                    }

                    if (userDataStorage.isUserExists(username)) {
                        console.printf("This user already exists.\n");
                        continue;
                    }

                    String password = readNewPassword();
                    User newUser = new User(username);
                    String passwordHash = generateHash(password);
                    if (passwordHash != null) {
                        newUser.setPasswordHash(passwordHash);
                        userDataStorage.addUser(newUser);
                        currentUserName = username;
                        console.printf("New user is created.\n");
                        console.printf("Hello, %s!\n", currentUserName);
                        currentState = State.HANDLE_COMMAND;
                    } else {
                        console.printf("Unable to create new user.\n");
                        currentState = State.LOGIN_USER;
                    }
                    break;
                }
                case LOGIN_USER: {
                    String username = readUsername();
                    if (username.isEmpty()) {
                        currentState = State.START;
                        continue;
                    }

                    if (userDataStorage.isUserExists(username)) {
                        User user = userDataStorage.getUser(username);
                        while (true) {
                            String password = readPassword();
                            if (validatePassword(user, password)) {
                                currentUserName = username;
                                console.printf("Hello, %s!\n", currentUserName);
                                currentState = State.HANDLE_COMMAND;
                                break;
                            } else {
                                console.printf("Invalid password. Try again.\n");
                            }
                        }
                    } else {
                        console.printf("User with this name does not exist.\n");
                        currentState = State.START;
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
                        console.printf("Unknown command. Try again.\n");
                        continue;
                    }

                    Command command = serviceCommands.get(commandName);
                    if (commandParts.length == 1) {
                        command.execute(new String[0]);
                    } else {
                        String[] commandArgs = Arrays.copyOfRange(commandParts, 1, commandParts.length - 1);
                        command.execute(commandArgs);
                    }
                    break;
                }
                case FINISH: {
                    currentUserName = null;
                    return;
                }
            }
        }
    }

    private String readUsername() {
        console.printf("Username:");
        return console.readLine().trim();
    }

    private String readPassword() {
        String password;
        while (true) {
            console.printf("Password:");
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
            console.printf("New password:");
            newPassword = new String(console.readPassword());
            if (newPassword.trim().isEmpty()) {
                continue;
            }
            console.printf("Confirm new password:");
            confirmedNewPassword = new String(console.readPassword());
            if (newPassword.equals(confirmedNewPassword)) {
                return newPassword;
            } else {
                console.printf("Passwords are not equal. Try again.\n");
            }
        }
    }

    public boolean updatePassword() {
        if (currentUserName == null) {
            console.printf("Unknown user.\n");
            return false;
        }

        String newPassword = readNewPassword();
        User user = new User(currentUserName);
        String passwordHash = generateHash(newPassword);
        if (passwordHash != null) {
            user.setPasswordHash(passwordHash);
            userDataStorage.updateUser(user);
            console.printf("Password is updated.\n");
            return true;
        } else {
            console.printf("Unable to update password.\n");
            return false;
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
            authService.init();
        } catch (IOException e) {
            System.err.println("Unable to create authorization service.");
        }
    }
}
