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

	public JobExecutionInfo( final Long executionIdArg, final Date startedAtArg, final String descriptionArg, final boolean longExecutionArg ){
		executionId = executionIdArg;
		startedAt = startedAtArg;
		description = descriptionArg;
		longExecution = longExecutionArg;
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
}
