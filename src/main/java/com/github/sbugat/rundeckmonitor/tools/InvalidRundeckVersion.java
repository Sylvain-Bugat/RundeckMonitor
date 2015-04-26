package com.github.sbugat.rundeckmonitor.tools;

/**
 * Exception to use when the detected Rundeck version is invalid.
 * 
 * @author Sylvain Bugat
 * 
 */
public final class InvalidRundeckVersion extends Exception {

	/** Unique Serial ID. */
	private static final long serialVersionUID = -9196136377007898216L;

	/** Version of the Rundeck. */
	private final String rundeckVersion;

	/**
	 * Constructor copy detected Rundeck version.
	 * 
	 * @param rundeckVersionArg Rundeck version
	 */
	public InvalidRundeckVersion(final String rundeckVersionArg) {
		rundeckVersion = rundeckVersionArg;
	}

	/**
	 * Get the invalid Rundeck version.
	 * 
	 * @return Rundeck version
	 */
	public String getRundeckVersion() {
		return rundeckVersion;
	}
}
