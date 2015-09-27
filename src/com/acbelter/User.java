package com.acbelter;

public class User {
    protected String name;
    protected String passwordHash;

    public User(String name) {
        this.name = name;
    }

    public User(String name, String passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public static User createUser(String name, String password) {
        User user = new User(name);
        if (password != null) {
            user.passwordHash = Utils.generatePasswordHash(password);
        }
        return user;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return name.equals(user.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                '}';
    }
}
