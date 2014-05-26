package com.github.sbugat.rundeckmonitor;

import java.awt.Image;

public class RundeckMonitorState {

	private boolean failedJobs;

	private boolean lateJobs;

	private boolean disconnected;

	public boolean isFailedJobs() {
		return failedJobs;
	}

	public void setFailedJobs( final boolean failedJobsArg ) {
		failedJobs = failedJobsArg;
	}

	public boolean isLateJobs() {
		return lateJobs;
	}

	public void setLateJobs( final boolean lateJobsArg ) {
		lateJobs = lateJobsArg;
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void setDisconnected( final boolean disconnectedArg ) {
		disconnected = disconnectedArg;
	}
}
