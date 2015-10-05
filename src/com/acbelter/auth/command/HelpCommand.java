package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

import java.io.Console;

public class HelpCommand extends Command {
    protected AuthorizationService service;

    public HelpCommand(AuthorizationService service, String name) {
        super(name);
        this.service = service;
    }

    public HelpCommand(AuthorizationService service, String name, String description) {
        super(name, description);
        this.service = service;
    }

    @Override
    public void execute(String[] args) {
        Console console = System.console();
        console.printf("Supported commands:\n");
        for (Command command : service.getSupportedCommands()) {
            console.printf("\t%s - %s\n", command.getName(), command.getDescription());
        }
    }
}
