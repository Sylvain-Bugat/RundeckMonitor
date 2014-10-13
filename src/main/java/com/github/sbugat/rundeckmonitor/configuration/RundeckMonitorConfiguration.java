package com.github.sbugat.rundeckmonitor.configuration;

import java.io.FileNotFoundException;
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

import com.github.sbugat.rundeckmonitor.wizard.InterfaceType;
import com.github.sbugat.rundeckmonitor.wizard.JobTabRedirection;

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
	private static final int RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE = 60;
	private static final String RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD = "rundeck.monitor.execution.late.threshold"; //$NON-NLS-1$
	private static final int RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE = 1800;
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER = "rundeck.monitor.failed.job.number"; //$NON-NLS-1$
	private static final int RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE = 10;
	private static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT = "rundeck.monitor.date.format"; //$NON-NLS-1$
	public static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE = "dd/MM/yyyy HH:mm:ss"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_API_VERSION = "rundeck.monitor.api.version"; //$NON-NLS-1$
	private static final int RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE = 10;
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION = "rundeck.monitor.failed.job.redirection"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION_DEFAULT_VALUE = JobTabRedirection.SUMMARY.name();
	private static final String RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER = "rundeck.monitor.disable.version.checker"; //$NON-NLS-1$
	private static final boolean RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER_DEFAULT_VALUE = false;
	private static final String RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE = "rundeck.monitor.interface.type"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE_DEFAULT_VALUE = InterfaceType.SWING.name();

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

	private int rundeckAPIversion;

	private String jobTabRedirection;

	private boolean versionCheckerDisabled;

	private String interfaceType;


	public RundeckMonitorConfiguration() {
		//Nothing to initialize
	}

	/**
	 * Copy constructor
	 * @param rundeckMonitorConfiguration
	 */
	public RundeckMonitorConfiguration( final RundeckMonitorConfiguration rundeckMonitorConfiguration ) {

		rundeckUrl = rundeckMonitorConfiguration.rundeckUrl;
		rundeckAPIKey = rundeckMonitorConfiguration.rundeckAPIKey;
		rundeckLogin = rundeckMonitorConfiguration.rundeckLogin;
		rundeckPassword = rundeckMonitorConfiguration.rundeckPassword;

		rundeckProject = rundeckMonitorConfiguration.rundeckProject;
		rundeckMonitorName = rundeckMonitorConfiguration.rundeckMonitorName;
		refreshDelay = rundeckMonitorConfiguration.refreshDelay;
		lateThreshold = rundeckMonitorConfiguration.lateThreshold;
		failedJobNumber = rundeckMonitorConfiguration.failedJobNumber;
		dateFormat = rundeckMonitorConfiguration.dateFormat;
		rundeckAPIversion = rundeckMonitorConfiguration.rundeckAPIversion;
		jobTabRedirection = rundeckMonitorConfiguration.jobTabRedirection;
		versionCheckerDisabled = rundeckMonitorConfiguration.versionCheckerDisabled;
	}

	/**
	 * Load configuration
	 *
	 * @throws IOException in case of loading configuration error
	 */
	public void loadConfigurationPropertieFile() throws IOException {

		//Configuration loading
		final Path propertyFile = Paths.get( RUNDECK_MONITOR_PROPERTIES_FILE );
		if( ! Files.exists( propertyFile ) ){

			throw new FileNotFoundException( RUNDECK_MONITOR_PROPERTIES_FILE );
		}

		//Load the configuration file and extract properties
		final Properties properties = new Properties();
		try( final Reader propertyFileReader = Files.newBufferedReader( propertyFile, StandardCharsets.UTF_8 ) ) {
			properties.load( propertyFileReader );
		}

		rundeckUrl = properties.getProperty( RUNDECK_MONITOR_PROPERTY_URL );
		rundeckProject = properties.getProperty( RUNDECK_MONITOR_PROPERTY_PROJECT );

		rundeckAPIKey = properties.getProperty( RUNDECK_MONITOR_PROPERTY_API_KEY );

		rundeckLogin = properties.getProperty( RUNDECK_MONITOR_PROPERTY_LOGIN );
		rundeckPassword = properties.getProperty( RUNDECK_MONITOR_PROPERTY_PASSWORD );

		rundeckMonitorName = properties.getProperty( RUNDECK_MONITOR_PROPERTY_NAME, RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE );
		refreshDelay = getIntegerProperty( properties, RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY, RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE );
		lateThreshold = getIntegerProperty( properties, RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD, RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE );
		failedJobNumber = getIntegerProperty( properties, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE );
		dateFormat = properties.getProperty( RUNDECK_MONITOR_PROPERTY_DATE_FORMAT, RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE );
		rundeckAPIversion = getIntegerProperty( properties, RUNDECK_MONITOR_PROPERTY_API_VERSION, RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE );
		jobTabRedirection = properties.getProperty( RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION_DEFAULT_VALUE );
		versionCheckerDisabled = getBooleanProperty( properties, RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER, RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER_DEFAULT_VALUE );
		interfaceType = properties.getProperty( RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE, RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE_DEFAULT_VALUE );
	}

	/**
	 * Check configuration
	 *
	 * @throws InvalidPropertyException
	 * @throws MissingPropertyException
	 */
	public void verifyConfiguration() throws MissingPropertyException, InvalidPropertyException  {

		//Configuration checking

		checkMandatoryStringProperty( rundeckUrl, RUNDECK_MONITOR_PROPERTY_URL );
		checkMandatoryStringProperty( rundeckProject, RUNDECK_MONITOR_PROPERTY_PROJECT );

		boolean missingAPIKey = false;
		try {
			checkMandatoryStringProperty( rundeckAPIKey, RUNDECK_MONITOR_PROPERTY_API_KEY );
		}
		catch( final MissingPropertyException | InvalidPropertyException e ) {
			missingAPIKey = false;
		}

		if( missingAPIKey ) {
			checkMandatoryStringProperty( rundeckLogin, RUNDECK_MONITOR_PROPERTY_LOGIN );
			checkMandatoryStringProperty( rundeckPassword, RUNDECK_MONITOR_PROPERTY_PASSWORD );
		}

		//Test the configured date format
		try {
			new SimpleDateFormat( dateFormat );
		}
		catch( final IllegalArgumentException e ) {
			dateFormat = RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE;
		}

		//Test the configured tab redirection
		try {
			JobTabRedirection.valueOf( jobTabRedirection );
		}
		catch( final IllegalArgumentException e ) {
			jobTabRedirection = RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION_DEFAULT_VALUE;
		}

	}

	private static void checkMandatoryStringProperty( final String property, final String propertyName ) throws MissingPropertyException, InvalidPropertyException {

		if( null == property ) {
			throw new MissingPropertyException( propertyName );
		}
		else if( property.isEmpty() ) {
			throw new InvalidPropertyException( propertyName, property );
		}
	}

	private static int getIntegerProperty( final Properties properties, final String propertyName, final int defaultValue ) {

		final String propertyValue = properties.getProperty( propertyName, String.valueOf( defaultValue ) );

		if( propertyValue.isEmpty() ) {
			return defaultValue;
		}

		try {
			return Integer.parseInt( propertyValue );
		}
		catch( final NumberFormatException e ) {
			return defaultValue;
		}
	}

	private static boolean getBooleanProperty( final Properties properties, final String propertyName, final boolean defaultValue ) {

		return Boolean.parseBoolean( properties.getProperty( propertyName, String.valueOf( defaultValue ) ) );
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
		properties.put( RUNDECK_MONITOR_PROPERTY_API_VERSION, String.valueOf( rundeckAPIversion ) );
		properties.put( RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION, jobTabRedirection );
		properties.put( RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER, String.valueOf( versionCheckerDisabled ) );
		properties.put( RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE, interfaceType );

		//Comment header
		final StringBuilder commentStringBuilder = new StringBuilder();
		commentStringBuilder.append( "Generated by RundeckMonitor wizard at: " ); //$NON-NLS-1$
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

	public void disableVersionChecker() throws IOException {

		versionCheckerDisabled = true;
	}

	public static boolean propertiesFileExists() {

		final Path propertyFile = Paths.get( RUNDECK_MONITOR_PROPERTIES_FILE );
		return Files.exists( propertyFile );
	}

	public static boolean propertiesFileUpdated( final Date date ) throws IOException {

		final Path propertyFile = Paths.get( RUNDECK_MONITOR_PROPERTIES_FILE );
		if( Files.exists( propertyFile ) ) {

			final Date fileTime = new Date( Files.getLastModifiedTime( propertyFile ).toMillis() );

			return fileTime.after( date );
		}

		return false;
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

	public int getRundeckAPIversion() {
		return rundeckAPIversion;
	}

	public String getJobTabRedirection() {
		return jobTabRedirection;
	}

	public String getInterfaceType() {
		return interfaceType;
	}

	public void setRundeckUrl( final String rundeckUrl ) {
		this.rundeckUrl = rundeckUrl;
	}

	public void setRundeckAPIKey( final String rundeckPIKey ) {
		this.rundeckAPIKey = rundeckPIKey;
	}

	public void setRundeckLogin( final String rundeckLogin ) {
		this.rundeckLogin = rundeckLogin;
	}

	public void setRundeckPassword( final String rundeckPassword ) {
		this.rundeckPassword = rundeckPassword;
	}

	public void setRundeckProject( final String rundeckProject ) {
		this.rundeckProject = rundeckProject;
	}

	public void setRundeckMonitorName( final String rundeckMonitorName ) {
		this.rundeckMonitorName = rundeckMonitorName;
	}

	public void setRefreshDelay( final int refreshDelay ) {
		this.refreshDelay = refreshDelay;
	}

	public void setLateThreshold( final int lateThreshold ) {
		this.lateThreshold = lateThreshold;
	}

	public void setFailedJobNumber( final int failedJobNumber ) {
		this.failedJobNumber = failedJobNumber;
	}

	public void setDateFormat( final String dateFormat ) {
		this.dateFormat = dateFormat;
	}

	public void setRundeckAPIversion( final int rundeckAPIversion ) {
		this.rundeckAPIversion = rundeckAPIversion;
	}

	public void setJobTabRedirection( final String jobTabRedirection ) {
		this.jobTabRedirection = jobTabRedirection;
	}

	public void setInterfaceType( final String interfaceType ) {
		this.interfaceType = interfaceType;
	}

	public boolean isVersionCheckerEnabled() {
		return ! versionCheckerDisabled;
	}
}