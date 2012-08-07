package net.alexjf.tmm.exceptions;

public class DatabaseNotReadyException extends DatabaseException {
    static final long serialVersionUID = 1;

    public DatabaseNotReadyException() {
        super("Database not ready: null or closed");
    }
}

