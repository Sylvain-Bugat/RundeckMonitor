package com.github.sbugat.rundeckmonitor.tools;

import java.util.Locale;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Tools class to detect Operating System type.
 * 
 * @author Sylvain Bugat
 * 
 */
public final class EnvironmentTools {

	/** SLF4J XLogger. */
	private static final XLogger LOG = XLoggerFactory.getXLogger(EnvironmentTools.class);

	/** Windows OS name property. */
	private static final String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	/** Windows name in OS name property. */
	private static final String WINDOWS_OS_NAME = "windows"; //$NON-NLS-1$

	/**
	 * Private constructor to prevent Instantiating.
	 */
	private EnvironmentTools() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Check if the operating system is a windows based on a property, otherwise it's a Linux/Mac-OS.
	 * 
	 * @return true if the OS is a windows*
	 */
	public static boolean isWindows() {

		LOG.entry();
		final String operatingSystem = System.getProperty(OS_NAME_PROPERTY);

		if (null == operatingSystem) {
			LOG.exit(false);
			return false;
		}

		final boolean isWindows = operatingSystem.toLowerCase(Locale.getDefault()).startsWith(WINDOWS_OS_NAME);
		LOG.exit(isWindows);
		return isWindows;
	}
}
