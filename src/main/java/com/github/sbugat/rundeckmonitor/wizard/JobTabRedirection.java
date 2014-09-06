package com.github.sbugat.rundeckmonitor.wizard;

public enum JobTabRedirection {

	SUMMARY( "show", "", "Exceution summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//REPORT( "show", "#state", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//LOG_OUTPUT( "show", "#output", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	//DEFINITION( "show", "", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	RENDER_OUTPUT_TXT( "downloadOutput", "?view=inline&formatted=false&stripansi=true", "View raw log" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	RENDER_OUTPUT_HTML( "renderOutput", "?ansicolor=on&loglevels=on", "View html formated log" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DOWNLOAD_OUTPUT( "downloadOutput", "", "Download raw log" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private final String accessUrlPrefix;

	private final String accessUrlSuffix;

	private final String description;

	private JobTabRedirection( final String accessUrlPrefixArg, final String accessUrlSuffixArg, final String descriptionArg ) {

		accessUrlPrefix = accessUrlPrefixArg;
		accessUrlSuffix = accessUrlSuffixArg;
		description = descriptionArg;
	}

	public String getAccessUrlPrefix() {
		return accessUrlPrefix;
	}


	public String getAccessUrlSuffix() {
		return accessUrlSuffix;
	}

	public String toString() {

		return description;
	}
}
