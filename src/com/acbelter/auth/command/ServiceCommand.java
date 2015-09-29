package com.acbelter.auth.command;

import com.acbelter.auth.AuthorizationService;

public abstract class ServiceCommand implements Comparable<ServiceCommand> {
    protected AuthorizationService service;
    protected String name;
    protected String description;

    public ServiceCommand(AuthorizationService service, String name) {
        this.service = service;
        this.name = name;
    }

    public ServiceCommand(AuthorizationService service, String name, String description) {
        this.service = service;
        this.name = name;
        this.description = description;
    }

    public abstract void execute(String... args);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceCommand serviceCommand = (ServiceCommand) o;

        return name.equals(serviceCommand.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(ServiceCommand o) {
        return name.compareTo(o.name);
    }
}
