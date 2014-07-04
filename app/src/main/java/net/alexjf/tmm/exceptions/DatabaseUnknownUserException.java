/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.exceptions;

public class DatabaseUnknownUserException extends DatabaseException {
    static final long serialVersionUID = 1;

    public DatabaseUnknownUserException() {
        super("Database cannot be opened: no user defined");
    }
}

