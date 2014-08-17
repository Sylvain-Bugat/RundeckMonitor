package com.github.sbugat.rundeckmonitor.wizard;

/**
 * Possible value of the refresh delay for the wizard
 *
 * @author Sylvain Bugat
 *
 */
public enum RefreshDelay {

	REFRESH_DELAY_10S( 10, "10 seconds" ), //$NON-NLS-1$
	REFRESH_DELAY_30S( 30, "30 seconds" ), //$NON-NLS-1$
	REFRESH_DELAY_1M( 60, "1 minute" ), //$NON-NLS-1$
	REFRESH_DELAY_2M( 120, "2 minutes" ), //$NON-NLS-1$
	REFRESH_DELAY_5M( 300, "5 minutes" ), //$NON-NLS-1$
	REFRESH_DELAY_10M( 600, "10 minutes" ); //$NON-NLS-1$

	/**Refresh delay*/
	private final Integer delay;
	/**Translation in seconds/minutes of the delay*/
	private final String description;

	private RefreshDelay( final int delayArg, final String descriptionArg ) {

		delay= Integer.valueOf( delayArg );
		description = descriptionArg;
	}

	public Integer getDelay() {
		return delay;
	}

	public String toString() {
		return description;
	}
}
