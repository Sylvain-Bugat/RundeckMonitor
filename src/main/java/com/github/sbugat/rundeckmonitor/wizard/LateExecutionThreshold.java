package com.github.sbugat.rundeckmonitor.wizard;

/**
 * Possible value of the late execution threshold for the wizard.
 * 
 * @author Sylvain Bugat
 * 
 */
public enum LateExecutionThreshold {

	/** 5 minutes threshold. */
	LATE_EXECUTION_THRESHOLD_5M(300, "5 minutes"), //$NON-NLS-1$
	/** 10 minutes threshold. */
	LATE_EXECUTION_THRESHOLD_10M(600, "10 minutes"), //$NON-NLS-1$
	/** 15 minutes threshold. */
	LATE_EXECUTION_THRESHOLD_15M(900, "15 minutes"), //$NON-NLS-1$
	/** 20 minutes threshold. */
	LATE_EXECUTION_THRESHOLD_20M(1200, "20 minutes"), //$NON-NLS-1$
	/** 30 minutes threshold. */
	LATE_EXECUTION_THRESHOLD_30M(1800, "30 minutes"), //$NON-NLS-1$
	/** 45 minutes threshold. */
	LATE_EXECUTION_THRESHOLD_45M(2700, "45 minutes"), //$NON-NLS-1$
	/** 1 hour threshold. */
	LATE_EXECUTION_THRESHOLD_1H(3600, "1 hour"), //$NON-NLS-1$
	/** 2 hours threshold. */
	LATE_EXECUTION_THRESHOLD_2H(7200, "2 hours"); //$NON-NLS-1$

	/** Late execution threshold. */
	private final int threshold;
	/** Translation in minutes/hours of the late execution threshold. */
	private final String description;

	/**
	 * Copy arguments to constants.
	 * 
	 * @param thresholdArg threshold
	 * @param descriptionArg associated description
	 */
	private LateExecutionThreshold(final int thresholdArg, final String descriptionArg) {

		threshold = thresholdArg;
		description = descriptionArg;
	}

	/**
	 * Get the threshold.
	 * 
	 * @return threshold
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * Return the description of a threshold.
	 * 
	 * @return description of the threshold
	 */
	@Override
	public String toString() {
		return description;
	}
}
