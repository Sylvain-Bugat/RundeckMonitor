package com.github.sbugat.rundeckmonitor.wizard;

public enum LateExecutionThreshold {

	LATE_EXECUTION_THRESHOLD_5M( 300, "5 minutes" ), //$NON-NLS-1$
	LATE_EXECUTION_THRESHOLD_10M( 600, "10 minutes" ), //$NON-NLS-1$
	LATE_EXECUTION_THRESHOLD_15M( 900, "15 minutes" ), //$NON-NLS-1$
	LATE_EXECUTION_THRESHOLD_20M( 1200, "20 minutes" ), //$NON-NLS-1$
	LATE_EXECUTION_THRESHOLD_30M( 1800, "30 minutes" ), //$NON-NLS-1$
	LATE_EXECUTION_THRESHOLD_45M( 2700, "45 minutes" ), //$NON-NLS-1$
	LATE_EXECUTION_THRESHOLD_1H( 3600, "1 hour" ), //$NON-NLS-1$
	LATE_EXECUTION_THRESHOLD_2H( 7200, "2 hours" ); //$NON-NLS-1$

	private final int threshold;
	private final String description;

	private LateExecutionThreshold( final int thresholdArg, final String descriptionArg ) {

		threshold =  thresholdArg;
		description = descriptionArg;
	}

	public int getThreshold() {
		return threshold;
	}

	public String toString() {
		return description;
	}
}
