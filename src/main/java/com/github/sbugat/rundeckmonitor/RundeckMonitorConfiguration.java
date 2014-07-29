package com.github.sbugat.rundeckmonitor;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * Configurationloading  class of the Rundeck Monitor
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitorConfiguration {

	/**Configuration file name*/
	static final String RUNDECK_MONITOR_PROPERTIES_FILE = "rundeckMonitor.properties"; //$NON-NLS-1$

	static final String RUNDECK_MONITOR_PROPERTY_URL = "rundeck.monitor.url"; //$NON-NLS-1$
	static final String RUNDECK_MONITOR_PROPERTY_API_KEY = "rundeck.monitor.api.key"; //$NON-NLS-1$
	static final String RUNDECK_MONITOR_PROPERTY_LOGIN = "rundeck.monitor.login"; //$NON-NLS-1$
	static final String RUNDECK_MONITOR_PROPERTY_PASSWORD = "rundeck.monitor.password"; //$NON-NLS-1$
	static final String RUNDECK_MONITOR_PROPERTY_PROJECT = "rundeck.monitor.project"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_NAME = "rundeck.monitor.name"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE = "Rundeck monitor"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY = "rundeck.monitor.refresh.delay"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE = "60"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD = "rundeck.monitor.execution.late.threshold"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE = "1800"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER = "rundeck.monitor.failed.job.number"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE = "10"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT = "rundeck.monitor.date.format"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE = "dd/MM/yyyy HH:mm:ss"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_API_VERSION = "rundeck.monitor.api.version"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE = "10"; //$NON-NLS-1$

	private final String rundeckUrl;

	private final String rundeckApiKey;

	private final String rundeckLogin;

	private final String rundeckPassword;

	/**Name of the rundeck project to access*/
	private final String rundeckProject;

	private final String rundeckMonitorName;

	/**Delay between 2 refresh of rundeck's data*/
	private final int refreshDelay;

	/**Threshold for detecting long execution*/
	private final int lateThreshold;

	private final int failedJobNumber;

	private final String dateFormat;

	private final String rundeckAPIversion;

	/**
	 * Load configuration
	 *
	 * @throws IOException in case of loading configuration error
	 * @throws InvalidPropertyException
	 * @throws MissingPropertyException
	 */
	public RundeckMonitorConfiguration() throws IOException, MissingPropertyException, InvalidPropertyException {

		//Configuration loading
		final Path propertyFile = Paths.get( RUNDECK_MONITOR_PROPERTIES_FILE );
		if( ! Files.exists( propertyFile ) ){

			JOptionPane.showMessageDialog( null, "Copy and configure " + RUNDECK_MONITOR_PROPERTIES_FILE + " file", RUNDECK_MONITOR_PROPERTIES_FILE + " file is missing", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit( 1 );
		}

		//Load the configuration file and extract properties
		final Properties properties = new Properties();
		try( final Reader propertyFileReader = Files.newBufferedReader( propertyFile, StandardCharsets.UTF_8 ) ) {
			properties.load( propertyFileReader );
		}

		rundeckUrl = loadMandatoryStringProperty( properties, RUNDECK_MONITOR_PROPERTY_URL );
		rundeckProject = loadMandatoryStringProperty( properties, RUNDECK_MONITOR_PROPERTY_PROJECT );

		String loadedRundeckApiKey;
		try {
			loadedRundeckApiKey = loadMandatoryStringProperty( properties, RUNDECK_MONITOR_PROPERTY_API_KEY );
		}
		catch( final MissingPropertyException | InvalidPropertyException e ) {
			loadedRundeckApiKey = null;
		}

		rundeckApiKey = loadedRundeckApiKey;

		if( null == loadedRundeckApiKey ) {
			rundeckLogin = loadMandatoryStringProperty( properties, RUNDECK_MONITOR_PROPERTY_LOGIN );
			rundeckPassword = loadMandatoryStringProperty( properties, RUNDECK_MONITOR_PROPERTY_PASSWORD );
		}
		else {
			rundeckLogin = ""; //$NON-NLS-1$
			rundeckPassword = ""; //$NON-NLS-1$
		}

		rundeckMonitorName = loadOptionalStringProperty( properties, RUNDECK_MONITOR_PROPERTY_NAME, RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE );
		refreshDelay = loadOptionalIntegerProperty( properties, RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY, RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE );
		lateThreshold = loadOptionalIntegerProperty( properties, RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD, RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE );
		failedJobNumber = loadOptionalIntegerProperty( properties, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE );
		dateFormat = loadOptionalStringProperty( properties, RUNDECK_MONITOR_PROPERTY_DATE_FORMAT, RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE );
		rundeckAPIversion = loadOptionalStringProperty( properties, RUNDECK_MONITOR_PROPERTY_API_VERSION, RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE );
	}

	private static String loadMandatoryStringProperty( final Properties properties, final String propertyName ) throws MissingPropertyException, InvalidPropertyException {

		final String propertyValue = properties.getProperty( propertyName );

		if( null == propertyValue ) {
			throw new MissingPropertyException( propertyName );
		}
		else if( propertyValue.isEmpty() ) {
			throw new InvalidPropertyException( propertyName, propertyValue );
		}

		return propertyValue;
	}

	private static String loadOptionalStringProperty( final Properties properties, final String propertyName, final String defaultValue ) {

		final String propertyValue = properties.getProperty( propertyName, defaultValue );

		if( propertyValue.isEmpty() ) {
			return defaultValue;
		}

		return propertyValue;
	}

	private static int loadOptionalIntegerProperty( final Properties properties, final String propertyName, final String defaultValue ) throws InvalidPropertyException {

		String propertyValue = properties.getProperty( propertyName, defaultValue );

		if( propertyValue.isEmpty() ) {
			propertyValue = defaultValue;
		}

		try {
			return Integer.parseInt( propertyValue );
		}
		catch( final NumberFormatException e ) {
			throw new InvalidPropertyException( propertyName, propertyValue );
		}
	}

	public String getRundeckUrl() {
		return rundeckUrl;
	}

	public String getRundeckApiKey() {
		return rundeckApiKey;
	}

	public String getRundeckLogin() {
		return rundeckLogin;
	}

	public String getRundeckPassword() {
		return rundeckPassword;
	}

	public String getRundeckProject() {
		return rundeckProject;
	}

	public String getRundeckMonitorName() {
		return rundeckMonitorName;
	}

	public int getRefreshDelay() {
		return refreshDelay;
	}

	public int getLateThreshold() {
		return lateThreshold;
	}

	public int getFailedJobNumber() {
		return failedJobNumber;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public String getRundeckAPIversion() {
		return rundeckAPIversion;
	}
}