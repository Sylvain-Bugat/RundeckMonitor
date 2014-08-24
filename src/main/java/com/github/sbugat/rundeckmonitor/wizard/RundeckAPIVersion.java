package com.github.sbugat.rundeckmonitor.wizard;

import org.rundeck.api.RundeckClient.Version;

public enum RundeckAPIVersion {

	RUNDECK_APIVERSION_5( Version.V5, "1.4.4" ), //$NON-NLS-1$
	RUNDECK_APIVERSION_6( Version.V6, "1.5.1" ), //$NON-NLS-1$
	RUNDECK_APIVERSION_7( Version.V7, "1.5.3" ), //$NON-NLS-1$
	RUNDECK_APIVERSION_8( Version.V8, "1.6.0" ), //$NON-NLS-1$
	RUNDECK_APIVERSION_9( Version.V9, "1.6.1" ), //$NON-NLS-1$
	RUNDECK_APIVERSION_10( Version.V10, "2.0.0" ), //$NON-NLS-1$
	RUNDECK_APIVERSION_11( Version.V11, "2.1.0" ); //$NON-NLS-1$

	/**Rundeck API Version*/
	private final Version version;
	/**First version using this API version*/
	private final String sinceRundeckVersion;

	private RundeckAPIVersion( final Version versionArg, final String sinceRundeckVersionArg ) {

		version = versionArg;
		sinceRundeckVersion = sinceRundeckVersionArg;
	}

	public Version getVersion() {
		return version;
	}

	public String getSinceReturnVersion() {
		return sinceRundeckVersion;
	}

	public String toString() {
		return version + " - since " + sinceRundeckVersion; //$NON-NLS-1$
	}
}
