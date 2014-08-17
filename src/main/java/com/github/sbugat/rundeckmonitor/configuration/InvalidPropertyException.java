package com.github.sbugat.rundeckmonitor.configuration;

/**
 * Exception to use when a property is invalid (value type mismatch or value mismatch)
 *
 * @author Sylvain Bugat
 *
 */
public class InvalidPropertyException extends Exception{

	private static final long serialVersionUID = 4577273435503511931L;

	/**Name of the invalid property */
	private final String property;

	/**Value of the invalid property */
	private final String propertyValue;

	public InvalidPropertyException( final String propertyArg, final String propertyValueArg ){
		property = propertyArg;
		propertyValue = propertyValueArg;
	}

	public String getProperty() {
		return property;
	}

	public String getPropertyValue() {
		return propertyValue;
	}
}
