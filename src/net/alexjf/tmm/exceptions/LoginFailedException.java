package net.alexjf.tmm.exceptions;

public class LoginFailedException extends TMMException {
    static final long serialVersionUID = 1;

    private String username;

    public LoginFailedException(String username) {
        this(username, null);
    }

    public LoginFailedException(String username, Throwable cause) {
        super("Login failed with user " + username, cause);
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
