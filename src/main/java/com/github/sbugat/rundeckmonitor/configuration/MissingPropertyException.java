package com.github.sbugat.rundeckmonitor.configuration;

/**
 * Exception to use when a mandatory property is missing
 *
 * @author Sylvain Bugat
 *
 */
public class MissingPropertyException extends Exception {

	private static final long serialVersionUID = -4199859651106152630L;

	/**Name of the missing property */
	private final String property;

	public MissingPropertyException( final String propertyArg ){
		property = propertyArg;
	}

	public String getProperty() {
		return property;
	}
}
