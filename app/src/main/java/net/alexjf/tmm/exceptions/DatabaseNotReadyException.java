/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.exceptions;

public class DatabaseNotReadyException extends DatabaseException {
	static final long serialVersionUID = 1;

	public DatabaseNotReadyException() {
		super("Database not ready: null or closed");
	}

	public DatabaseNotReadyException(DatabaseException e) {
		super("Database not ready: " + e.getMessage(), e);
	}
}

