package com.github.sbugat.rundeckmonitor.wizard;

public enum JobTabRedirection {

	SUMMARY( "show", "", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	REPORT( "show", "#state", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	LOG_OUTPUT( "show", "#output", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DEFINITION( "show", "", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	RENDER_OUTPUT_TXT( "downloadOutput", "", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	RENDER_OUTPUT_HTML( "show", "", "summary" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DOWNLOAD_OUTPUT( "downloadOutput", "", "summary" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private final String accessUrlPrefix;

	private final String accessUrlSuffix;

	private final String description;

	private JobTabRedirection( final String accessUrlPrefixArg, final String accessUrlSuffixArg, final String descriptionArg ) {

		accessUrlPrefix = accessUrlPrefixArg;
		accessUrlSuffix = accessUrlSuffixArg;
		description = descriptionArg;
	}


	public String toString() {

		return description;
	}
}
