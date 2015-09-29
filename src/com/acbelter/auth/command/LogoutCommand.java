package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

public class LogoutCommand extends ServiceCommand {
    public LogoutCommand(AuthorizationService service, String name) {
        super(service, name);
    }

    public LogoutCommand(AuthorizationService service, String name, String description) {
        super(service, name, description);
    }

    @Override
    public void execute(String... args) {
        service.stopHandleCommandProcess();
    }
}
