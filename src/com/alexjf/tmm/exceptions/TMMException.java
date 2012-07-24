package com.alexjf.tmm.exceptions;

public class TMMException extends Exception {
    static final long serialVersionUID = 1;

    public TMMException(String error) {
        super(error);
    }

    public TMMException(Throwable cause) {
        super(cause);
    }

    public TMMException(String error, Throwable cause) {
        super(error, cause);
    }
}
