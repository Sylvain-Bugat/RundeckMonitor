package com.github.sbugat.rundeckmonitor;

import java.util.Date;

/**
 * A runDeck execution information class.
 *
 * @author Sylvain Bugat
 *
 */
public final class JobExecutionInfo {

	/** RunDeck execution id. */
	private final Long executionId;

	/** RunDeck start job date. */
	private final Date startedAt;

	/** Rundeck job description. */
	private final String description;

	/** Flag for a long execution. */
	private final boolean longExecution;

	/** Flag to mark the execution as already known. */
	private final boolean newJob;

	/**
	 * Constructor to copy all arguments into new JobExecutionInfo object.
	 *
	 * @param executionIdArg RunDeck identifier of the execution
	 * @param startedAtArg starting date of the execution
	 * @param descriptionArg description of the execution
	 * @param longExecutionArg flag to indicate if it's a long execution
	 * @param newJobArg flag to indicate if this execution is already known
	 */
	public JobExecutionInfo(final Long executionIdArg, final Date startedAtArg, final String descriptionArg, final boolean longExecutionArg, final boolean newJobArg) {
		executionId = executionIdArg;
		startedAt = new Date(startedAtArg.getTime());
		description = descriptionArg;
		longExecution = longExecutionArg;
		newJob = newJobArg;
	}

	/**
	 * Return the RunDeck execution identifier.
	 *
	 * @return RunDeck execution identifier
	 */
	public Long getExecutionId() {
		return executionId;
	}

	/**
	 * Return the RunDeck execution starting date.
	 *
	 * @return RunDeck execution starting date
	 */
	public Date getStartedAt() {
		return new Date(startedAt.getTime());
	}

	/**
	 * Return the RunDeck job description.
	 *
	 * @return RunDeck job description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return the long execution flag.
	 *
	 * @return long execution flag
	 */
	public boolean isLongExecution() {
		return longExecution;
	}

	/**
	 * Return the new execution flag.
	 *
	 * @return new execution flag
	 */
	public boolean isNewJob() {
		return newJob;
	}
}
