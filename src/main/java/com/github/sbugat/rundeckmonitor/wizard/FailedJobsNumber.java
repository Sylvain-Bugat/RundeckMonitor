package com.github.sbugat.rundeckmonitor.wizard;

public enum FailedJobsNumber {

	FAILED_JOBS_5( 5 ),
	FAILED_JOBS_6( 6 ),
	FAILED_JOBS_7( 7 ),
	FAILED_JOBS_8( 8 ),
	FAILED_JOBS_9( 9 ),
	FAILED_JOBS_10( 10 ),
	FAILED_JOBS_11( 11 ),
	FAILED_JOBS_12( 12 ),
	FAILED_JOBS_13( 13 ),
	FAILED_JOBS_14( 14 ),
	FAILED_JOBS_15( 15 ),
	FAILED_JOBS_16( 16 ),
	FAILED_JOBS_17( 17 ),
	FAILED_JOBS_18( 18 ),
	FAILED_JOBS_19( 19 ),
	FAILED_JOBS_20( 20 );

	private final int failedJobsNumber;

	private FailedJobsNumber( final int failedJobsNumberArg ) {

		failedJobsNumber= failedJobsNumberArg;
	}

	public Integer getFailedJobsNumber() {
		return failedJobsNumber;
	}

	public String toString() {
		return String.valueOf( failedJobsNumber );
	}
}
