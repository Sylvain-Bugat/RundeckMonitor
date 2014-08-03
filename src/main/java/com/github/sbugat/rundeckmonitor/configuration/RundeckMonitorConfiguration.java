package com.github.sbugat.rundeckmonitor.configuration;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	public static final String RUNDECK_MONITOR_PROPERTIES_FILE = "rundeckMonitor.properties"; //$NON-NLS-1$

	public static final String RUNDECK_MONITOR_PROPERTY_URL = "rundeck.monitor.url"; //$NON-NLS-1$
	public static final String RUNDECK_MONITOR_PROPERTY_API_KEY = "rundeck.monitor.api.key"; //$NON-NLS-1$
	public static final String RUNDECK_MONITOR_PROPERTY_LOGIN = "rundeck.monitor.login"; //$NON-NLS-1$
	public static final String RUNDECK_MONITOR_PROPERTY_PASSWORD = "rundeck.monitor.password"; //$NON-NLS-1$
	public static final String RUNDECK_MONITOR_PROPERTY_PROJECT = "rundeck.monitor.project"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_NAME = "rundeck.monitor.name"; //$NON-NLS-1$
	public static final String RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE = "RundeckMonitor"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY = "rundeck.monitor.refresh.delay"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE = "60"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD = "rundeck.monitor.execution.late.threshold"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE = "1800"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER = "rundeck.monitor.failed.job.number"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE = "10"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT = "rundeck.monitor.date.format"; //$NON-NLS-1$
	public static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE = "dd/MM/yyyy HH:mm:ss"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_API_VERSION = "rundeck.monitor.api.version"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE = "10"; //$NON-NLS-1$

	private String rundeckUrl;

	private String rundeckAPIKey;

	private String rundeckLogin;

	private String rundeckPassword;

	/**Name of the rundeck project to access*/
	private String rundeckProject;

	private String rundeckMonitorName;

	/**Delay between 2 refresh of rundeck's data*/
	private int refreshDelay;

	/**Threshold for detecting long execution*/
	private int lateThreshold;

	private int failedJobNumber;

	private String dateFormat;

	private String rundeckAPIversion;

	public RundeckMonitorConfiguration() {

	}

	/**
	 * Load configuration
	 *
	 * @throws IOException in case of loading configuration error
	 * @throws InvalidPropertyException
	 * @throws MissingPropertyException
	 */
	public void loadMonitorConfigurationPropertieFile() throws IOException, MissingPropertyException, InvalidPropertyException {

		//Configuration loading
		final Path propertyFile = Paths.get( RUNDECK_MONITOR_PROPERTIES_FILE );
		if( ! Files.exists( propertyFile ) ){

			JOptionPane.showMessageDialog( null, "Copy and configure " + RUNDECK_MONITOR_PROPERTIES_FILE + " file", RUNDECK_MONITOR_PROPERTIES_FILE + " file is missing", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

		rundeckAPIKey = loadedRundeckApiKey;

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

	public void saveMonitorConfigurationPropertieFile() throws IOException {

		//Add all properties
		final Properties properties = new Properties();

		properties.put( RUNDECK_MONITOR_PROPERTY_URL, rundeckUrl );
		properties.put( RUNDECK_MONITOR_PROPERTY_API_KEY, rundeckAPIKey );
		properties.put( RUNDECK_MONITOR_PROPERTY_LOGIN, rundeckLogin );
		properties.put( RUNDECK_MONITOR_PROPERTY_PASSWORD, rundeckPassword );
		properties.put( RUNDECK_MONITOR_PROPERTY_PROJECT, rundeckProject );
		properties.put( RUNDECK_MONITOR_PROPERTY_NAME, rundeckMonitorName);
		properties.put( RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY, String.valueOf( refreshDelay ) );
		properties.put( RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD, String.valueOf( lateThreshold ) );
		properties.put( RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER, String.valueOf( failedJobNumber ) );
		properties.put( RUNDECK_MONITOR_PROPERTY_DATE_FORMAT, dateFormat );
		properties.put( RUNDECK_MONITOR_PROPERTY_API_VERSION, rundeckAPIversion );

		//Comment header
		final StringBuilder commentStringBuilder = new StringBuilder();
		commentStringBuilder.append( "Generated by RundeckMonitor wizard at:" ); //$NON-NLS-1$
		try {
			final SimpleDateFormat formatter = new SimpleDateFormat( dateFormat );
			commentStringBuilder.append( formatter.format( new Date() ) );
		}
		catch( final IllegalArgumentException e ) {
			commentStringBuilder.append( new Date() );
		}
		commentStringBuilder.append(  System.lineSeparator() );
		commentStringBuilder.append( "-------------------------------------------------------" ); //$NON-NLS-1$

		//Properties file writing
		final Path propertyFile = Paths.get( RUNDECK_MONITOR_PROPERTIES_FILE );
		try( final Writer propertyFilewriter = Files.newBufferedWriter( propertyFile, StandardCharsets.UTF_8 ) ) {
			properties.store( propertyFilewriter, commentStringBuilder.toString() );
		}
	}

	public static boolean propertiesFileExists() {

		final Path propertyFile = Paths.get( RUNDECK_MONITOR_PROPERTIES_FILE );
		return Files.exists( propertyFile );
	}

	public String getRundeckUrl() {
		return rundeckUrl;
	}

	public String getRundeckAPIKey() {
		return rundeckAPIKey;
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

	public void setRundeckUrl(String rundeckUrl) {
		this.rundeckUrl = rundeckUrl;
	}

	public void setRundeckAPIKey(String rundeckPIKey) {
		this.rundeckAPIKey = rundeckPIKey;
	}

	public void setRundeckLogin(String rundeckLogin) {
		this.rundeckLogin = rundeckLogin;
	}

	public void setRundeckPassword(String rundeckPassword) {
		this.rundeckPassword = rundeckPassword;
	}

	public void setRundeckProject(String rundeckProject) {
		this.rundeckProject = rundeckProject;
	}

	public void setRundeckMonitorName(String rundeckMonitorName) {
		this.rundeckMonitorName = rundeckMonitorName;
	}

	public void setRefreshDelay(int refreshDelay) {
		this.refreshDelay = refreshDelay;
	}

	public void setLateThreshold(int lateThreshold) {
		this.lateThreshold = lateThreshold;
	}

	public void setFailedJobNumber(int failedJobNumber) {
		this.failedJobNumber = failedJobNumber;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setRundeckAPIversion(String rundeckAPIversion) {
		this.rundeckAPIversion = rundeckAPIversion;
	}
}