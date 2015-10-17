package com.github.sbugat.rundeckmonitor.configuration;

/**
 * Exception to use when a property is invalid (value type mismatch or value mismatch).
 *
 * @author Sylvain Bugat
 *
 */
public final class InvalidPropertyException extends Exception {

	/** Unique Serial ID. */
	private static final long serialVersionUID = 4577273435503511931L;

	/** Name of the invalid property. */
	private final String property;

	/** Value of the invalid property. */
	private final String propertyValue;

	/**
	 * Constructor copy property name and his value.
	 *
	 * @param propertyArg property name
	 * @param propertyValueArg value of the property
	 */
	public InvalidPropertyException(final String propertyArg, final String propertyValueArg) {
		property = propertyArg;
		propertyValue = propertyValueArg;
	}

	/**
	 * Get the property name.
	 *
	 * @return property name
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Get the property value.
	 *
	 * @return property value
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
}
