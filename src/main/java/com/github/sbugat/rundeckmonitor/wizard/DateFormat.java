package com.github.sbugat.rundeckmonitor.wizard;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Possible standard date format for the wizard
 *
 * @author Sylvain Bugat
 *
 */
public enum DateFormat {

	DATE_FORMAT_STANDARD( "dd/MM/yyyy HH:mm:ss" ), //$NON-NLS-1$
	DATE_FORMAT_UNITED_STATES( "MM/dd/yyyy HH:mm:ss" ), //$NON-NLS-1$
	DATE_FORMAT_EAST_ASIA( "yyyy/MM/dd HH:mm:ss" ); //$NON-NLS-1$

	/**Date format to use*/
	private final String dateFormat;
	/**Example of the current date to display*/
	private final String example;

	private DateFormat( final String dateFormatArg ) {

		dateFormat = dateFormatArg;

		final SimpleDateFormat formatter = new SimpleDateFormat( dateFormat );
		example = formatter.format( new Date() );
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String toString() {
		return dateFormat + " - "  + example ; //$NON-NLS-1$
	}
}
