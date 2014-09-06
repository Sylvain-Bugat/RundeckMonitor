package com.github.sbugat.rundeckmonitor.wizard;

public enum JobTabRedirection {

	SUMMARY( "show", "", "Execution summary", "1.0" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	//REPORT( "show", "#state", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//LOG_OUTPUT( "show", "#output", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//DEFINITION( "show", "", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	RENDER_OUTPUT_TXT( "downloadOutput", "?view=inline&formatted=false&stripansi=true", "View raw log", "2.0" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	RENDER_OUTPUT_HTML( "renderOutput", "?ansicolor=on&loglevels=on", "View html formated log", "2.0" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	DOWNLOAD_OUTPUT( "downloadOutput", "", "Download raw log", "1.0" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private final String accessUrlPrefix;

	private final String accessUrlSuffix;

	private final String description;

	/**First version using this redirection*/
	private final String sinceRundeckVersion;

	private JobTabRedirection( final String accessUrlPrefixArg, final String accessUrlSuffixArg, final String descriptionArg, final String sinceRundeckVersionArg ) {

		accessUrlPrefix = accessUrlPrefixArg;
		accessUrlSuffix = accessUrlSuffixArg;
		description = descriptionArg;
		sinceRundeckVersion = sinceRundeckVersionArg;
	}

	public String getAccessUrlPrefix() {
		return accessUrlPrefix;
	}

	public String getAccessUrlSuffix() {
		return accessUrlSuffix;
	}

	public String getSinceRundeckVersion() {
		return sinceRundeckVersion;
	}

	public String toString() {
		return description;
	}
}
