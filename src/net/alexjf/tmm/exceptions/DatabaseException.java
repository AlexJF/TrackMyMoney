/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.exceptions;

public class DatabaseException extends TMMException {
    static final long serialVersionUID = 1;

    public DatabaseException(String error) {
        super(error);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }

    public DatabaseException(String error, Throwable cause) {
        super(error, cause);
    }
}
