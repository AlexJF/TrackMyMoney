package net.alexjf.tmm.exceptions;

/**
 * Created by alex on 18/09/14.
 */
public class ExitingException extends TMMException {
	public ExitingException() {
		super("Application is exiting. Nothing else should be done.");
	}
}
