package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

public class ChangePasswordCommand extends ServiceCommand {
    public ChangePasswordCommand(AuthorizationService service, String name) {
        super(service, name);
    }

    public ChangePasswordCommand(AuthorizationService service, String name, String description) {
        super(service, name, description);
    }

    @Override
    public void execute(String... args) {
        String newPassword = service.startEnterNewPasswordProcess();
        service.updateUserPassword(newPassword);
    }
}
