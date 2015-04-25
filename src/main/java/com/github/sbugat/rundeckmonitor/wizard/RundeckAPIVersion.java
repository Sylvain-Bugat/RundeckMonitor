package com.github.sbugat.rundeckmonitor.wizard;

import org.rundeck.api.RundeckClient.Version;

/**
 * Possible API version usable with rundeck monitor.
 * 
 * @author Sylvain Bugat
 * 
 */
public enum RundeckAPIVersion {

	/** API version 5. */
	RUNDECK_APIVERSION_5(Version.V5, "1.4.4"), //$NON-NLS-1$
	/** API version 6. */
	RUNDECK_APIVERSION_6(Version.V6, "1.5.1"), //$NON-NLS-1$
	/** API version 7. */
	RUNDECK_APIVERSION_7(Version.V7, "1.5.3"), //$NON-NLS-1$
	/** API version 8. */
	RUNDECK_APIVERSION_8(Version.V8, "1.6.0"), //$NON-NLS-1$
	/** API version 9. */
	RUNDECK_APIVERSION_9(Version.V9, "1.6.1"), //$NON-NLS-1$
	/** API version 10. */
	RUNDECK_APIVERSION_10(Version.V10, "2.0.0"), //$NON-NLS-1$
	/** API version 11. */
	RUNDECK_APIVERSION_11(Version.V11, "2.1.0"), //$NON-NLS-1$
	/** API version 12. */
	RUNDECK_APIVERSION_12(Version.V12, "2.2.0"), //$NON-NLS-1$
	/** API version 13. */
	RUNDECK_APIVERSION_13(Version.V13, "2.5.0"); //$NON-NLS-1$

	/** Rundeck API Version. */
	private final Version version;
	/** First version using this API version. */
	private final String sinceRundeckVersion;

	/**
	 * Copy arguments tp constants.
	 * 
	 * @param versionArg API version
	 * @param sinceRundeckVersionArg first RunDeck version using this API version
	 */
	private RundeckAPIVersion(final Version versionArg, final String sinceRundeckVersionArg) {

		version = versionArg;
		sinceRundeckVersion = sinceRundeckVersionArg;
	}

	/**
	 * Get the API version.
	 * 
	 * @return API version
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Get the first RunDeck version using this API version.
	 * 
	 * @return RunDeck version
	 */
	public String getSinceReturnVersion() {
		return sinceRundeckVersion;
	}

	/**
	 * Return the version and the first API version.
	 * 
	 * @return displayed String
	 */
	@Override
	public String toString() {
		return version + " - since " + sinceRundeckVersion; //$NON-NLS-1$
	}
}
