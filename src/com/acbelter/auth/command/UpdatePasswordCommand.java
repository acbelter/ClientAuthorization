package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

public class UpdatePasswordCommand extends ServiceCommand {
    public UpdatePasswordCommand(AuthorizationService service, String name) {
        super(service, name);
    }

    public UpdatePasswordCommand(AuthorizationService service, String name, String description) {
        super(service, name, description);
    }

    @Override
    public void execute(String... args) {
        service.setState(AuthorizationService.State.UPDATE_PASSWORD);
    }
}
