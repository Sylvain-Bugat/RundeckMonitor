package com.github.sbugat.rundeckmonitor;

import java.util.Date;

/**
 * A Job information class.
 *
 * @author Sylvain Bugat
 *
 */
public final class JobExecutionInfo {

	private final Long executionId;

	private final Date startedAt;

	private final String description;

	private final boolean longExecution;

	private final boolean newJob;

	public JobExecutionInfo( final Long executionIdArg, final Date startedAtArg, final String descriptionArg, final boolean longExecutionArg, final boolean newJobArg ){
		executionId = executionIdArg;
		startedAt =  new Date( startedAtArg.getTime() );
		description = descriptionArg;
		longExecution = longExecutionArg;
		newJob = newJobArg;
	}

	public final Long getExecutionId() {
		return executionId;
	}

	public final Date getStartedAt() {
		return new Date( startedAt.getTime() );
	}

	public final String getDescription() {
		return description;
	}

	public final boolean isLongExecution() {
		return longExecution;
	}

	public final boolean isNewJob() {
		return newJob;
	}
}
