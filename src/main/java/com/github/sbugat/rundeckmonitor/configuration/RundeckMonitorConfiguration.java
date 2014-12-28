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
 * Configurationloading class of the RunDeck Monitor.
 * 
 * @author Sylvain Bugat
 * 
 */
public final class RundeckMonitorConfiguration {

	/** Configuration file name. */
	public static final String RUNDECK_MONITOR_PROPERTIES_FILE = "rundeckMonitor.properties"; //$NON-NLS-1$

	/** RunDeck URL property name. */
	public static final String RUNDECK_MONITOR_PROPERTY_URL = "rundeck.monitor.url"; //$NON-NLS-1$
	/** RunDeck API key property name. */
	public static final String RUNDECK_MONITOR_PROPERTY_API_KEY = "rundeck.monitor.api.key"; //$NON-NLS-1$
	/** RunDeck login property name. */
	public static final String RUNDECK_MONITOR_PROPERTY_LOGIN = "rundeck.monitor.login"; //$NON-NLS-1$
	/** RunDeck password property name. */
	public static final String RUNDECK_MONITOR_PROPERTY_PASSWORD = "rundeck.monitor.password"; //$NON-NLS-1$
	/** RunDeck project property name. */
	public static final String RUNDECK_MONITOR_PROPERTY_PROJECT = "rundeck.monitor.project"; //$NON-NLS-1$
	/** RunDeck monitor name property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_NAME = "rundeck.monitor.name"; //$NON-NLS-1$
	/** RunDeck monitor name default value. */
	public static final String RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE = "RundeckMonitor"; //$NON-NLS-1$
	/** RunDeck monitor refresh delay property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY = "rundeck.monitor.refresh.delay"; //$NON-NLS-1$
	/** RunDeck monitor refresh delay default value. */
	private static final int RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE = 60;
	/** RunDeck monitor late threshold property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD = "rundeck.monitor.execution.late.threshold"; //$NON-NLS-1$
	/** RunDeck monitor late threshold default value. */
	private static final int RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE = 1800;
	/** RunDeck monitor failed job number property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER = "rundeck.monitor.failed.job.number"; //$NON-NLS-1$
	/** RunDeck monitor failed job number default value. */
	private static final int RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE = 10;
	/** RunDeck monitor date format property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT = "rundeck.monitor.date.format"; //$NON-NLS-1$
	/** RunDeck monitor date format default value. */
	public static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE = "dd/MM/yyyy HH:mm:ss"; //$NON-NLS-1$
	/** RunDeck monitor API version property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_API_VERSION = "rundeck.monitor.api.version"; //$NON-NLS-1$
	/** RunDeck monitor API version default value. */
	private static final int RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE = 5;
	/** RunDeck monitor job redirection property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION = "rundeck.monitor.failed.job.redirection"; //$NON-NLS-1$
	/** RunDeck monitor job redirection default value. */
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION_DEFAULT_VALUE = JobTabRedirection.SUMMARY.name();
	/** RunDeck monitor version checker property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER = "rundeck.monitor.disable.version.checker"; //$NON-NLS-1$
	/** RunDeck monitor version checker default value. */
	private static final boolean RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER_DEFAULT_VALUE = false;
	/** RunDeck monitor GUI type property name. */
	private static final String RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE = "rundeck.monitor.interface.type"; //$NON-NLS-1$
	/** RunDeck monitor GUI type default value. */
	private static final String RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE_DEFAULT_VALUE = InterfaceType.SWING.name();

	/** RunDeck URL. */
	private String rundeckUrl;

	/** Rundeck API key. */
	private String rundeckAPIKey;

	/** RunDeck login. */
	private String rundeckLogin;

	/** RunDeck password. */
	private String rundeckPassword;

	/** Name of the rundeck project to access. */
	private String rundeckProject;

	/** RunDeck monitor name. */
	private String rundeckMonitorName;

	/** Delay between 2 refresh of rundeck's data. */
	private int refreshDelay;

	/** Threshold for detecting long execution. */
	private int lateThreshold;

	/** Number of displayed failed jobs. */
	private int failedJobNumber;

	/** Displayed date format. */
	private String dateFormat;

	/** RunDeck API version. */
	private int rundeckAPIversion;

	/** Job tab redirection. */
	private String jobTabRedirection;

	/** Version checker flag. */
	private boolean versionCheckerDisabled;

	/** Type of GUI interface used. */
	private String interfaceType;

	/**
	 * Default constructor.
	 */
	public RundeckMonitorConfiguration() {
		// Nothing to initialize
	}

	/**
	 * Copy constructor.
	 * 
	 * @param rundeckMonitorConfiguration configuration to copy
	 */
	public RundeckMonitorConfiguration(final RundeckMonitorConfiguration rundeckMonitorConfiguration) {

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
	 * Load configuration.
	 * 
	 * @throws IOException in case of loading configuration error
	 */
	public void loadConfigurationPropertieFile() throws IOException {

		// Configuration loading
		final Path propertyFile = Paths.get(RUNDECK_MONITOR_PROPERTIES_FILE);
		if (!Files.exists(propertyFile)) {

			throw new FileNotFoundException(RUNDECK_MONITOR_PROPERTIES_FILE);
		}

		// Load the configuration file and extract properties
		final Properties properties = new Properties();
		try (final Reader propertyFileReader = Files.newBufferedReader(propertyFile, StandardCharsets.UTF_8)) {
			properties.load(propertyFileReader);
		}

		rundeckUrl = properties.getProperty(RUNDECK_MONITOR_PROPERTY_URL);
		rundeckProject = properties.getProperty(RUNDECK_MONITOR_PROPERTY_PROJECT);

		rundeckAPIKey = properties.getProperty(RUNDECK_MONITOR_PROPERTY_API_KEY);

		rundeckLogin = properties.getProperty(RUNDECK_MONITOR_PROPERTY_LOGIN);
		rundeckPassword = properties.getProperty(RUNDECK_MONITOR_PROPERTY_PASSWORD);

		rundeckMonitorName = properties.getProperty(RUNDECK_MONITOR_PROPERTY_NAME, RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE);
		refreshDelay = getIntegerProperty(properties, RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY, RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE);
		lateThreshold = getIntegerProperty(properties, RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD, RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE);
		failedJobNumber = getIntegerProperty(properties, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE);
		dateFormat = properties.getProperty(RUNDECK_MONITOR_PROPERTY_DATE_FORMAT, RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE);
		rundeckAPIversion = getIntegerProperty(properties, RUNDECK_MONITOR_PROPERTY_API_VERSION, RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE);
		jobTabRedirection = properties.getProperty(RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION_DEFAULT_VALUE);
		versionCheckerDisabled = getBooleanProperty(properties, RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER, RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER_DEFAULT_VALUE);
		interfaceType = properties.getProperty(RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE, RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE_DEFAULT_VALUE);
	}

	/**
	 * Check loaded configuration.
	 * 
	 * @throws InvalidPropertyException in case of loading configuration property error
	 * @throws MissingPropertyException in case of loading configuration property error
	 */
	public void verifyConfiguration() throws MissingPropertyException, InvalidPropertyException {

		// Configuration checking

		checkMandatoryStringProperty(rundeckUrl, RUNDECK_MONITOR_PROPERTY_URL);
		checkMandatoryStringProperty(rundeckProject, RUNDECK_MONITOR_PROPERTY_PROJECT);

		boolean missingAPIKey = false;
		try {
			checkMandatoryStringProperty(rundeckAPIKey, RUNDECK_MONITOR_PROPERTY_API_KEY);
		}
		catch (final MissingPropertyException | InvalidPropertyException e) {
			missingAPIKey = false;
		}

		if (missingAPIKey) {
			checkMandatoryStringProperty(rundeckLogin, RUNDECK_MONITOR_PROPERTY_LOGIN);
			checkMandatoryStringProperty(rundeckPassword, RUNDECK_MONITOR_PROPERTY_PASSWORD);
		}

		// Test the configured date format
		try {
			new SimpleDateFormat(dateFormat);
		}
		catch (final IllegalArgumentException e) {
			dateFormat = RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE;
		}

		// Test the configured tab redirection
		try {
			JobTabRedirection.valueOf(jobTabRedirection);
		}
		catch (final IllegalArgumentException e) {
			jobTabRedirection = RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION_DEFAULT_VALUE;
		}

	}

	/**
	 * Check a property value.
	 * 
	 * @param property property loaded (can be null)
	 * @param propertyName name of the loaded property
	 * @throws MissingPropertyException if the property is null
	 * @throws InvalidPropertyException if the property is empty
	 */
	private static void checkMandatoryStringProperty(final String property, final String propertyName) throws MissingPropertyException, InvalidPropertyException {

		if (null == property) {
			throw new MissingPropertyException(propertyName);
		}
		else if (property.isEmpty()) {
			throw new InvalidPropertyException(propertyName, property);
		}
	}

	/**
	 * Get an optional integer property.
	 * 
	 * @param properties loaded properties
	 * @param propertyName property name to get
	 * @param defaultValue value to set if the property is missing or is invalid
	 * @return property value
	 */
	private static int getIntegerProperty(final Properties properties, final String propertyName, final int defaultValue) {

		final String propertyValue = properties.getProperty(propertyName, String.valueOf(defaultValue));

		if (propertyValue.isEmpty()) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(propertyValue);
		}
		catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Get an optional boolean property.
	 * 
	 * @param properties loaded properties
	 * @param propertyName property name to get
	 * @param defaultValue value to set if the property is missing or is invalid
	 * @return property value
	 */
	private static boolean getBooleanProperty(final Properties properties, final String propertyName, final boolean defaultValue) {

		return Boolean.parseBoolean(properties.getProperty(propertyName, String.valueOf(defaultValue)));
	}

	/**
	 * Save the current loaded configuration to the configuration file.
	 * 
	 * @throws IOException in case of writing error
	 */
	public void saveMonitorConfigurationPropertieFile() throws IOException {

		// Add all properties
		final Properties properties = new Properties();

		properties.put(RUNDECK_MONITOR_PROPERTY_URL, rundeckUrl);
		properties.put(RUNDECK_MONITOR_PROPERTY_API_KEY, rundeckAPIKey);
		properties.put(RUNDECK_MONITOR_PROPERTY_LOGIN, rundeckLogin);
		properties.put(RUNDECK_MONITOR_PROPERTY_PASSWORD, rundeckPassword);
		properties.put(RUNDECK_MONITOR_PROPERTY_PROJECT, rundeckProject);
		properties.put(RUNDECK_MONITOR_PROPERTY_NAME, rundeckMonitorName);
		properties.put(RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY, String.valueOf(refreshDelay));
		properties.put(RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD, String.valueOf(lateThreshold));
		properties.put(RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER, String.valueOf(failedJobNumber));
		properties.put(RUNDECK_MONITOR_PROPERTY_DATE_FORMAT, dateFormat);
		properties.put(RUNDECK_MONITOR_PROPERTY_API_VERSION, String.valueOf(rundeckAPIversion));
		properties.put(RUNDECK_MONITOR_PROPERTY_FAILED_JOB_REDIRECTION, jobTabRedirection);
		properties.put(RUNDECK_MONITOR_PROPERTY_DISABLE_VERSION_CHECKER, String.valueOf(versionCheckerDisabled));
		properties.put(RUNDECK_MONITOR_PROPERTY_INTERFACE_TYPE, interfaceType);

		// Comment header
		final StringBuilder commentStringBuilder = new StringBuilder();
		commentStringBuilder.append("Generated by RundeckMonitor wizard at: "); //$NON-NLS-1$
		try {
			final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			commentStringBuilder.append(formatter.format(new Date()));
		}
		catch (final IllegalArgumentException e) {
			commentStringBuilder.append(new Date());
		}
		commentStringBuilder.append(System.lineSeparator());
		commentStringBuilder.append("-------------------------------------------------------"); //$NON-NLS-1$

		// Properties file writing
		final Path propertyFile = Paths.get(RUNDECK_MONITOR_PROPERTIES_FILE);
		try (final Writer propertyFilewriter = Files.newBufferedWriter(propertyFile, StandardCharsets.UTF_8)) {
			properties.store(propertyFilewriter, commentStringBuilder.toString());
		}
	}

	/**
	 * Disable the version checker.
	 */
	public void disableVersionChecker() {

		versionCheckerDisabled = true;
	}

	/**
	 * Check if the configuration file exists and is readable.
	 * 
	 * @return true if the configuration file exists and can be read
	 */
	public static boolean propertiesFileExists() {

		final Path propertyFile = Paths.get(RUNDECK_MONITOR_PROPERTIES_FILE);
		return Files.exists(propertyFile) && Files.isReadable(propertyFile);
	}

	/**
	 * Check if the configuration file has been updated.
	 * 
	 * @param date date of the last configuration loading
	 * @return true if the configuration file is newer than the date
	 */
	public static boolean propertiesFileUpdated(final Date date) {

		final Path propertyFile = Paths.get(RUNDECK_MONITOR_PROPERTIES_FILE);
		if (Files.exists(propertyFile)) {

			final Date fileTime;
			try {
				fileTime = new Date(Files.getLastModifiedTime(propertyFile).toMillis());
			}
			catch (final IOException e) {
				return false;
			}

			return fileTime.after(date);
		}

		return false;
	}

	/**
	 * Return the RunDeck URL.
	 * 
	 * @return RunDeck URL
	 */
	public String getRundeckUrl() {
		return rundeckUrl;
	}

	/**
	 * Return the RunDeck API key.
	 * 
	 * @return RunDeck API key
	 */
	public String getRundeckAPIKey() {
		return rundeckAPIKey;
	}

	/**
	 * Return the RunDeck login.
	 * 
	 * @return RunDeck login
	 */
	public String getRundeckLogin() {
		return rundeckLogin;
	}

	/**
	 * Return the RunDeck paswword.
	 * 
	 * @return RunDeck paswword
	 */
	public String getRundeckPassword() {
		return rundeckPassword;
	}

	/**
	 * Return the RunDeck project.
	 * 
	 * @return RunDeck project
	 */
	public String getRundeckProject() {
		return rundeckProject;
	}

	/**
	 * Return the RunDeck monitor name.
	 * 
	 * @return RunDeck monitor name
	 */
	public String getRundeckMonitorName() {
		return rundeckMonitorName;
	}

	/**
	 * Return the RunDeck monitor refresh delay.
	 * 
	 * @return RunDeck monitor refresh delay
	 */
	public int getRefreshDelay() {
		return refreshDelay;
	}

	/**
	 * Return the RunDeck monitor late threshold.
	 * 
	 * @return RunDeck monitor late threshold
	 */
	public int getLateThreshold() {
		return lateThreshold;
	}

	/**
	 * Return the RunDeck monitor failed job number.
	 * 
	 * @return RunDeck monitor failed job number
	 */
	public int getFailedJobNumber() {
		return failedJobNumber;
	}

	/**
	 * Return the RunDeck monitor date format.
	 * 
	 * @return RunDeck monitor date format
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * Return the RunDeck monitor API version.
	 * 
	 * @return RunDeck monitor API version
	 */
	public int getRundeckAPIversion() {
		return rundeckAPIversion;
	}

	/**
	 * Return the RunDeck monitor job tab redirection.
	 * 
	 * @return RunDeck monitor job tab redirection
	 */
	public String getJobTabRedirection() {
		return jobTabRedirection;
	}

	/**
	 * Return the RunDeck monitor GUI type.
	 * 
	 * @return RunDeck monitor GUI type
	 */
	public String getInterfaceType() {
		return interfaceType;
	}

	/**
	 * Return the RunDeck monitor version checker flag.
	 * 
	 * @return true if the version checker is enabled
	 */
	public boolean isVersionCheckerEnabled() {
		return !versionCheckerDisabled;
	}

	/**
	 * Set the RunDeck URL.
	 * 
	 * @param rundeckUrlArg RunDeck URL to set
	 */
	public void setRundeckUrl(final String rundeckUrlArg) {
		rundeckUrl = rundeckUrlArg;
	}

	/**
	 * Set the RunDeck API key.
	 * 
	 * @param rundeckAPIKeyArg API key to set
	 */
	public void setRundeckAPIKey(final String rundeckAPIKeyArg) {
		this.rundeckAPIKey = rundeckAPIKeyArg;
	}

	/**
	 * Set the RunDeck login.
	 * 
	 * @param rundeckLoginArg login to set
	 */
	public void setRundeckLogin(final String rundeckLoginArg) {
		this.rundeckLogin = rundeckLoginArg;
	}

	/**
	 * Set the RunDeck password.
	 * 
	 * @param rundeckPasswordArg password to set
	 */
	public void setRundeckPassword(final String rundeckPasswordArg) {
		this.rundeckPassword = rundeckPasswordArg;
	}

	/**
	 * Set the RunDeck project.
	 * 
	 * @param rundeckProjectArg project to set
	 */
	public void setRundeckProject(final String rundeckProjectArg) {
		this.rundeckProject = rundeckProjectArg;
	}

	/**
	 * Set the RunDeck monitor name.
	 * 
	 * @param rundeckMonitorNameArg monitor name to set
	 */
	public void setRundeckMonitorName(final String rundeckMonitorNameArg) {
		this.rundeckMonitorName = rundeckMonitorNameArg;
	}

	/**
	 * Set the RunDeck monitor refresh delay.
	 * 
	 * @param refreshDelayArg refresh delay to set
	 */
	public void setRefreshDelay(final int refreshDelayArg) {
		this.refreshDelay = refreshDelayArg;
	}

	/**
	 * Set the RunDeck monitor late threshold.
	 * 
	 * @param lateThresholdArg late threashold to set
	 */
	public void setLateThreshold(final int lateThresholdArg) {
		this.lateThreshold = lateThresholdArg;
	}

	/**
	 * Set the RunDeck monitor failed job number.
	 * 
	 * @param failedJobNumberArg failed job number to set
	 */
	public void setFailedJobNumber(final int failedJobNumberArg) {
		this.failedJobNumber = failedJobNumberArg;
	}

	/**
	 * Set the RunDeck monitor date format.
	 * 
	 * @param dateFormatArg date format to set
	 */
	public void setDateFormat(final String dateFormatArg) {
		this.dateFormat = dateFormatArg;
	}

	/**
	 * Set the RunDeck monitor API version.
	 * 
	 * @param rundeckAPIversionArg API version to set
	 */
	public void setRundeckAPIversion(final int rundeckAPIversionArg) {
		this.rundeckAPIversion = rundeckAPIversionArg;
	}

	/**
	 * Set the RunDeck monitor job tab redirection.
	 * 
	 * @param jobTabRedirectionArg job tab redirection to set
	 */
	public void setJobTabRedirection(final String jobTabRedirectionArg) {
		this.jobTabRedirection = jobTabRedirectionArg;
	}

	/**
	 * Set the RunDeck monitor GUI type.
	 * 
	 * @param interfaceTypeArg GUI type to set
	 */
	public void setInterfaceType(final String interfaceTypeArg) {
		this.interfaceType = interfaceTypeArg;
	}

}
