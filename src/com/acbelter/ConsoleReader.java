package com.acbelter;

import java.io.Console;

public class ConsoleReader {
    public static String readUsername(Console console) {
        console.printf("Username:");
        return console.readLine().trim();
    }

    public static String readPassword(Console console) {
        String password;
        while (true) {
            console.printf("Password:");
            password = new String(console.readPassword());
            if (!password.trim().isEmpty()) {
                return password;
            }
        }
    }

    public static String readNewPassword(Console console) {
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
}
