package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

public class ExitCommand extends ServiceCommand {
    public ExitCommand(AuthorizationService service, String name) {
        super(service, name);
    }

    public ExitCommand(AuthorizationService service, String name, String description) {
        super(service, name, description);
    }

    @Override
    public void execute(String... args) {
        service.setState(AuthorizationService.State.FINISHED);
    }
}
