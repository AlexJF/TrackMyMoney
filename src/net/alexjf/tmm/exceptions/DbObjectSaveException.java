/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.exceptions;

public class DbObjectSaveException extends DatabaseException {
    static final long serialVersionUID = 1;

    public DbObjectSaveException(String message) {
        this("", null);
    }

    public DbObjectSaveException(Throwable cause) {
        this("", cause);
    }

    public DbObjectSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}

