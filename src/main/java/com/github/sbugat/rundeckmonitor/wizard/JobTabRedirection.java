package com.github.sbugat.rundeckmonitor.wizard;

/**
 * Enumeration of possible execution default redirection.
 * 
 * @author Sylvain Bugat
 * 
 */
public enum JobTabRedirection {

	/** Summary of an execution. */
	SUMMARY("show", "", "Execution summary", "1.0"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	//REPORT( "show", "#state", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//LOG_OUTPUT( "show", "#output", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//DEFINITION( "show", "", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	/** Display all logs of an execution as textual format. */
	RENDER_OUTPUT_TXT("downloadOutput", "?view=inline&formatted=false&stripansi=true", "View raw log", "2.0"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/** Display all logs of an execution as html format. */
	RENDER_OUTPUT_HTML("renderOutput", "?ansicolor=on&loglevels=on", "View html formated log", "2.0"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/** Download the log file of an execution. */
	DOWNLOAD_OUTPUT("downloadOutput", "", "Download raw log", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/** First poart of the opened URL. */
	private final String accessUrlPrefix;

	/** Last part of the openened URL. */
	private final String accessUrlSuffix;

	/** Description for configuration. */
	private final String description;

	/** First version using this redirection. */
	private final String sinceRundeckVersion;

	/**
	 * Copy all arguments.
	 * 
	 * @param accessUrlPrefixArg URL prefix
	 * @param accessUrlSuffixArg URL suffix
	 * @param descriptionArg description
	 * @param sinceRundeckVersionArg first version compatible with this redirection
	 */
	private JobTabRedirection(final String accessUrlPrefixArg, final String accessUrlSuffixArg, final String descriptionArg, final String sinceRundeckVersionArg) {

		accessUrlPrefix = accessUrlPrefixArg;
		accessUrlSuffix = accessUrlSuffixArg;
		description = descriptionArg;
		sinceRundeckVersion = sinceRundeckVersionArg;
	}

	/**
	 * get the URL prefix.
	 * 
	 * @return URL prefix
	 */
	public String getAccessUrlPrefix() {
		return accessUrlPrefix;
	}

	/**
	 * Get the URL suffix.
	 * 
	 * @return URL suffix
	 */
	public String getAccessUrlSuffix() {
		return accessUrlSuffix;
	}

	/**
	 * Get the first version.
	 * 
	 * @return first version
	 */
	public String getSinceRundeckVersion() {
		return sinceRundeckVersion;
	}

	/**
	 * Get a redirection description.
	 * 
	 * @return redirection description
	 */
	@Override
	public String toString() {
		return description;
	}
}
