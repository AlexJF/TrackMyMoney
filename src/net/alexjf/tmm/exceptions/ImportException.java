/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.exceptions;

public class ImportException extends Exception {
    static final long serialVersionUID = 1;

    public ImportException(String s) {
        super(s);
    }

    public ImportException(Throwable e) {
        super(e.getMessage(), e);
    }
}

