package com.github.sbugat.rundeckmonitor.tools;

import java.util.Locale;

/**
 * Tools class to detect Operating System type
 *
 * @author Sylvain Bugat
 *
 */
public class EnvironmentTools {

	private static final String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	private static final String WINDOWS_OS_NAME = "windows"; //$NON-NLS-1$

	/**
	 * Check if the operating system is a windows based on a property, otherwise it's a Linux/Mac-OS
	 *
	 * @return true if the OS is a windows*
	 */
	public static boolean isWindows() {

		final String operatingSystem = System.getProperty( OS_NAME_PROPERTY );

		if( null == operatingSystem ) {
			return false;
		}

		return operatingSystem.toLowerCase( Locale.getDefault() ).startsWith( WINDOWS_OS_NAME );
	}
}
