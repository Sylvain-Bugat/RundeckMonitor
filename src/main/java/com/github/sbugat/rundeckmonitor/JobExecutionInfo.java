package com.github.sbugat.rundeckmonitor;

import java.util.Date;

/**
 * A Job information class
 *
 * @author Sylvain Bugat
 *
 */
public class JobExecutionInfo {

	private final Long executionId;

	private final Date startedAt;

	private final String description;

	private final boolean longExecution;

	private final boolean newJob;

	public JobExecutionInfo( final Long executionIdArg, final Date startedAtArg, final String descriptionArg, final boolean longExecutionArg, final boolean newJobArg ){
		executionId = executionIdArg;
		startedAt = startedAtArg;
		description = descriptionArg;
		longExecution = longExecutionArg;
		newJob = newJobArg;
	}

	public Long getExecutionId() {
		return executionId;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public String getDescription() {
		return description;
	}

	public boolean isLongExecution() {
		return longExecution;
	}

	public boolean isNewJob() {
		return newJob;
	}
}
