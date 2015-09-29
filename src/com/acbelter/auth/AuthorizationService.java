package com.acbelter.auth;

import com.acbelter.User;
import com.acbelter.auth.command.ChangePasswordCommand;
import com.acbelter.auth.command.HelpCommand;
import com.acbelter.auth.command.LogoutCommand;
import com.acbelter.auth.command.ServiceCommand;

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

    private Console console;
    private String currentUserName;
    private boolean interruptCommandHandleProcess;

    public AuthorizationService(UserDataStorage userDataStorage) throws IOException {
        this.userDataStorage = userDataStorage;
        userDataStorage.loadUserData();

        serviceCommands = new HashMap<>();
        addCommand(new HelpCommand(this, "help", "Use this command to show supported commands."));
        addCommand(new ChangePasswordCommand(this, "pass", "Use this command to change password."));
        addCommand(new LogoutCommand(this, "logout", "Use this command to switch between users."));
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
            System.out.println("No access to system console.\n");
            return;
        }

        try {
            userDataStorage.loadUserData();
        } catch (IOException e) {
            console.printf("%s", "Failed to load user data.\n");
            return;
        }

        startLoginProcess();
    }

    private void addCommand(ServiceCommand command) {
        serviceCommands.put(command.getName(), command);
    }

    private void startLoginProcess() {
        currentUserName = null;

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
            startCreateUserProcess(name);
        } else {
            startEnterPasswordProcess(name);
        }
    }

    private void startEnterPasswordProcess(String name) {
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
                currentUserName = name;
                startHandleCommandProcess();
                return;
            } else {
                console.printf("%s", "Invalid password. Try again.\n");
            }
        }
    }

    private void startCreateUserProcess(String name) {
        String newPassword = startEnterNewPasswordProcess();

        User newUser = new User(name);
        String passwordHash = generateHash(newPassword);
        if (passwordHash != null) {
            newUser.setPasswordHash(passwordHash);
            userDataStorage.addUser(newUser);
            console.printf("%s", "New user is created.\n");
            currentUserName = name;
        } else {
            console.printf("%s", "Unable to create new user.\n");
        }

        startHandleCommandProcess();
    }

    public String startEnterNewPasswordProcess() {
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

    public void updateUserPassword(String newPassword) {
        User user = new User(currentUserName);
        String passwordHash = generateHash(newPassword);
        if (passwordHash != null) {
            user.setPasswordHash(passwordHash);
            userDataStorage.updateUser(user);
            console.printf("%s", "Password is updated.\n");
        } else {
            console.printf("%s", "Unable to update password.\n");
        }
    }

    public void startHandleCommandProcess() {
        String commandData;
        String commandName;
        String[] commandArgs;
        ServiceCommand command;
        while (true) {
            if (interruptCommandHandleProcess) {
                interruptCommandHandleProcess = false;
                startLoginProcess();
                break;
            }

            commandData = console.readLine();
            String[] commandParts = commandData.split(" ");
            if (commandParts.length == 0) {
                continue;
            }

            commandName = commandParts[0];
            if (!serviceCommands.containsKey(commandName)) {
                console.printf("%s", "Unknown command. Try again.\n");
                continue;
            }

            command = serviceCommands.get(commandName);
            if (commandParts.length == 1) {
                command.execute();
            } else {
                commandArgs = new String[commandParts.length - 1];
                System.arraycopy(commandParts, 1, commandArgs, 0, commandParts.length - 1);
                command.execute(commandArgs);
            }
        }
    }

    public void stopHandleCommandProcess() {
        interruptCommandHandleProcess = true;
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
            System.out.println("Unable to create authorization service.");
        }
    }
}
