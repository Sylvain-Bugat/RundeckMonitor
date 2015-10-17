package com.github.sbugat.rundeckmonitor.wizard;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Possible standard date format for the wizard.
 *
 * @author Sylvain Bugat
 *
 */
public enum DateFormat {

	/** European date format. */
	DATE_FORMAT_STANDARD("dd/MM/yyyy HH:mm:ss"), //$NON-NLS-1$
	/** North America date format. */
	DATE_FORMAT_UNITED_STATES("MM/dd/yyyy HH:mm:ss"), //$NON-NLS-1$
	/** East Asia date format. */
	DATE_FORMAT_EAST_ASIA("yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$

	/** Date format to use. */
	private final String dateFormat;
	/** Example of the current date to display. */
	private final String example;

	/**
	 * Enumeration constructor with an example of the format with the system date.
	 *
	 * @param dateFormatArg date format
	 */
	private DateFormat(final String dateFormatArg) {

		dateFormat = dateFormatArg;

		final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		example = formatter.format(new Date());
	}

	/**
	 * Get the date format.
	 *
	 * @return the date format.
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * Return a String to display this date format.
	 *
	 * @return String to display
	 */
	@Override
	public String toString() {
		return dateFormat + " - " + example; //$NON-NLS-1$
	}
}
