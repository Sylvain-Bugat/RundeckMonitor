package com.github.sbugat.rundeckmonitor.wizard;

/**
 * Possible value of the refresh delay for the wizard.
 *
 * @author Sylvain Bugat
 *
 */
public enum RefreshDelay {

	/** 10 seconds refresh delay. */
	REFRESH_DELAY_10S(10, "10 seconds"), //$NON-NLS-1$
	/** 30 seconds refresh delay. */
	REFRESH_DELAY_30S(30, "30 seconds"), //$NON-NLS-1$
	/** 1 minute refresh delay. */
	REFRESH_DELAY_1M(60, "1 minute"), //$NON-NLS-1$
	/** 2 minutes refresh delay. */
	REFRESH_DELAY_2M(120, "2 minutes"), //$NON-NLS-1$
	/** 5 minutes refresh delay. */
	REFRESH_DELAY_5M(300, "5 minutes"), //$NON-NLS-1$
	/** 10 minutes refresh delay. */
	REFRESH_DELAY_10M(600, "10 minutes"); //$NON-NLS-1$

	/** Refresh delay. */
	private final Integer delay;
	/** Translation in seconds/minutes of the delay. */
	private final String description;

	/**
	 * RefreshDelay constructor copye arguments into constant values.
	 *
	 * @param delayArg delay to set
	 * @param descriptionArg descriptino to set
	 */
	private RefreshDelay(final int delayArg, final String descriptionArg) {

		delay = Integer.valueOf(delayArg);
		description = descriptionArg;
	}

	/**
	 * Get the refresh delay.
	 *
	 * @return refresh delay
	 */
	public Integer getDelay() {
		return delay;
	}

	/**
	 * Return the description to be displayed.
	 *
	 * @return displayed String
	 */
	@Override
	public String toString() {
		return description;
	}
}
