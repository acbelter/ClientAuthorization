package com.acbelter;

import com.acbelter.service.AuthService;
import com.acbelter.service.MessagingService;
import com.acbelter.storage.HistoryFileStorage;
import com.acbelter.storage.UserDataFileStorage;
import com.acbelter.storage.UserDataStorage;

import java.io.IOException;

public class Messenger {
    protected UserDataStorage userDataStorage;
    protected AuthService authService;
    protected MessagingService messagingService;

    public Messenger(UserDataStorage userDataStorage) {
        this.userDataStorage = userDataStorage;
        try {
            authService = new AuthService(userDataStorage);
            authService.init();
        } catch (IOException e) {
            System.err.println("Unable to create authorization service.");
            authService = null;
        }
    }

    public void start() {
        if (authService == null) {
            return;
        }

        while (true) {
            User user = authService.authorize();
            messagingService = new MessagingService(new HistoryFileStorage());
        }
    }

    public static void main(String[] args) {
        Messenger messenger = new Messenger(new UserDataFileStorage());
        messenger.start();
    }
}
