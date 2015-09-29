package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

import java.io.Console;

public class HelpCommand extends ServiceCommand {
    public HelpCommand(AuthorizationService service, String name) {
        super(service, name);
    }

    public HelpCommand(AuthorizationService service, String name, String description) {
        super(service, name, description);
    }

    @Override
    public void execute(String... args) {
        Console console = System.console();
        console.printf("%s", "Supported commands:\n");
        for (ServiceCommand command : service.getSupportedCommands()) {
            console.printf("%s", "\t" + command.getName() + " - " + command.getDescription() + "\n");
        }
    }
}
