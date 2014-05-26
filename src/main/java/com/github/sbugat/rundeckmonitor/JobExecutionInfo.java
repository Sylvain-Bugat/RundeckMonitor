package com.github.sbugat.rundeckmonitor;

import java.util.Date;

public class JobExecutionInfo {

	private final Long executionId;

	private final Date startedAt;

	private final String description;

	public JobExecutionInfo( final Long executionIdArg, final Date startedAtArg, final String descriptionArg ){
		executionId = executionIdArg;
		startedAt = startedAtArg;
		description = descriptionArg;
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
}
