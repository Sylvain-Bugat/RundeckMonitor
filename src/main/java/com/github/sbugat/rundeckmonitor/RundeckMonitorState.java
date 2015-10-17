package com.github.sbugat.rundeckmonitor;

/**
 * Class containing the current state of the monitor.
 *
 * @author Sylvain Bugat
 *
 */
public final class RundeckMonitorState {

	/** Failed jobs flag. */
	private boolean failedJobs;

	/** Late/long jobs flag. */
	private boolean lateJobs;

	/** Disconnected flag. */
	private boolean disconnected;

	/**
	 * Get the failed flag.
	 *
	 * @return failed flag
	 */
	public boolean isFailedJobs() {
		return failedJobs;
	}

	/**
	 * Set the failed flag.
	 *
	 * @param failedJobsArg failed flag
	 */
	public void setFailedJobs(final boolean failedJobsArg) {
		failedJobs = failedJobsArg;
	}

	/**
	 * Get the long/late flag.
	 *
	 * @return long/late flag
	 */
	public boolean isLateJobs() {
		return lateJobs;
	}

	/**
	 * Set the long/late flag.
	 *
	 * @param lateJobsArg long/late flag
	 */
	public void setLateJobs(final boolean lateJobsArg) {
		lateJobs = lateJobsArg;
	}

	/**
	 * Get the disconnected flag.
	 *
	 * @return true if disconnected
	 */
	public boolean isDisconnected() {
		return disconnected;
	}

	/**
	 * Set the disconnected state.
	 *
	 * @param disconnectedArg disconnected state
	 */
	public void setDisconnected(final boolean disconnectedArg) {
		disconnected = disconnectedArg;
	}
}
