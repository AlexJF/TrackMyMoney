/*******************************************************************************
 * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
 * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
 ******************************************************************************/
package net.alexjf.tmm.exceptions;

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
