package com.github.sbugat.rundeckmonitor;

/**
 *
 * @author Sylvain Bugat
 *
 */
public class InvalidPropertyException extends Exception{

	private static final long serialVersionUID = 4577273435503511931L;

	private final String property;
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
