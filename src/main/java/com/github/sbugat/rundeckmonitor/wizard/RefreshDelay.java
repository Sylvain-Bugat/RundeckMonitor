package com.github.sbugat.rundeckmonitor.wizard;

public enum RefreshDelay {

	REFRESH_DELAY_10S( 10, "10 seconds" ), //$NON-NLS-1$
	REFRESH_DELAY_30S( 30, "30 seconds" ), //$NON-NLS-1$
	REFRESH_DELAY_1M( 60, "1 minute" ), //$NON-NLS-1$
	REFRESH_DELAY_2M( 120, "2 minutes" ), //$NON-NLS-1$
	REFRESH_DELAY_5M( 300, "5 minutes" ), //$NON-NLS-1$
	REFRESH_DELAY_10M( 600, "10 minutes" ); //$NON-NLS-1$

	private final Integer delay;
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
