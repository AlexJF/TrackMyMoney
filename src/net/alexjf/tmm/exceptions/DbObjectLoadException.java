package net.alexjf.tmm.exceptions;

public class DbObjectLoadException extends DatabaseException {
    static final long serialVersionUID = 1;

    public DbObjectLoadException(String message) {
        this("", null);
    }

    public DbObjectLoadException(Throwable cause) {
        this("", cause);
    }

    public DbObjectLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

