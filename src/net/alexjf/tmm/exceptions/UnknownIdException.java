package net.alexjf.tmm.exceptions;

public class UnknownIdException extends DatabaseException {
    static final long serialVersionUID = 1;

    public UnknownIdException() {
        super("Unknown id");
    }
}

