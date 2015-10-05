package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

public class LogoutCommand extends Command {
    protected AuthorizationService service;

    public LogoutCommand(AuthorizationService service, String name) {
        super(name);
        this.service = service;
    }

    public LogoutCommand(AuthorizationService service, String name, String description) {
        super(name, description);
        this.service = service;
    }

    @Override
    public void execute(String[] args) {
        service.setState(AuthorizationService.State.START);
    }
}
