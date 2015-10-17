package com.github.sbugat.rundeckmonitor.wizard;

/**
 * Possible value of possible failed and late jobs numbers for the wizard.
 *
 * @author Sylvain Bugat
 *
 */
public enum FailedJobsNumber {

	/** 5 failed jobs. */
	FAILED_JOBS_5(5),
	/** 6 failed jobs. */
	FAILED_JOBS_6(6),
	/** 7 failed jobs. */
	FAILED_JOBS_7(7),
	/** 8 failed jobs. */
	FAILED_JOBS_8(8),
	/** 9 failed jobs. */
	FAILED_JOBS_9(9),
	/** 10 failed jobs. */
	FAILED_JOBS_10(10),
	/** 11 failed jobs. */
	FAILED_JOBS_11(11),
	/** 12 failed jobs. */
	FAILED_JOBS_12(12),
	/** 13 failed jobs. */
	FAILED_JOBS_13(13),
	/** 14 failed jobs. */
	FAILED_JOBS_14(14),
	/** 15 failed jobs. */
	FAILED_JOBS_15(15),
	/** 16 failed jobs. */
	FAILED_JOBS_16(16),
	/** 17 failed jobs. */
	FAILED_JOBS_17(17),
	/** 18 failed jobs. */
	FAILED_JOBS_18(18),
	/** 19 failed jobs. */
	FAILED_JOBS_19(19),
	/** 20 failed jobs. */
	FAILED_JOBS_20(20);

	/** Number of failed/late jobs to display. */
	private final int failedJobsNumber;

	/**
	 * Copy the failed job number argument.
	 *
	 * @param failedJobsNumberArg failed job number to set
	 */
	private FailedJobsNumber(final int failedJobsNumberArg) {

		failedJobsNumber = failedJobsNumberArg;
	}

	/**
	 * Get the failed job number.
	 *
	 * @return failed job number
	 */
	public Integer getFailedJobsNumber() {
		return failedJobsNumber;
	}

	/**
	 * Convert the faild job number to a String.
	 *
	 * @return displayed String
	 */
	@Override
	public String toString() {
		return String.valueOf(failedJobsNumber);
	}
}
