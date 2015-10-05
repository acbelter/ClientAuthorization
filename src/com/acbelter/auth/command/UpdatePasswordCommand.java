package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

public class UpdatePasswordCommand extends Command {
    protected AuthorizationService service;

    public UpdatePasswordCommand(AuthorizationService service, String name) {
        super(name);
        this.service = service;
    }

    public UpdatePasswordCommand(AuthorizationService service, String name, String description) {
        super(name, description);
        this.service = service;
    }

    @Override
    public void execute(String[] args) {
        service.updatePassword();
    }
}
