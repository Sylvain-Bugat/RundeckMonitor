package com.github.sbugat.rundeckmonitor.configuration;

/**
 * Exception to use when a mandatory property is missing.
 *
 * @author Sylvain Bugat
 *
 */
public final class MissingPropertyException extends Exception {

	/** Unique Serial ID. */
	private static final long serialVersionUID = -4199859651106152630L;

	/** Name of the missing property. */
	private final String property;

	/**
	 * Constructor copy the missing property name.
	 *
	 * @param propertyArg missing property name
	 */
	public MissingPropertyException(final String propertyArg) {
		property = propertyArg;
	}

	/**
	 * Get the missing property name.
	 *
	 * @return missing property name
	 */
	public String getProperty() {
		return property;
	}
}
