package com.github.sbugat.rundeckmonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

import org.rundeck.api.RundeckApiException;
import org.rundeck.api.RundeckApiException.RundeckApiHttpStatusException;
import org.rundeck.api.RundeckApiException.RundeckApiLoginException;
import org.rundeck.api.RundeckApiException.RundeckApiTokenException;
import org.rundeck.api.RundeckClient;
import org.rundeck.api.RundeckClientBuilder;
import org.rundeck.api.domain.RundeckExecution;
import org.rundeck.api.domain.RundeckExecution.ExecutionStatus;
import org.rundeck.api.query.ExecutionQuery;
import org.rundeck.api.util.PagedResults;

/**
 * Primary and main class of the Rundeck Monitor
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitor implements Runnable {

	/**Configuration file name*/
	private static final String RUNDECK_MONITOR_PROPERTIES_FILE = "rundeckMonitor.properties"; //$NON-NLS-1$

	private static final String RUNDECK_PROPERTY_URL = "rundeck.monitor.url"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_API_KEY = "rundeck.monitor.api.key"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_LOGIN = "rundeck.monitor.login"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_PASSWORD = "rundeck.monitor.password"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_PROJECT = "rundeck.monitor.project"; //$NON-NLS-1$
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

	private final VersionChecker versionChecker;

	/**Name of the rundeck project to access*/
	private final String rundeckProject;

	/**Delay between 2 refresh of rundeck's data*/
	private final int refreshDelay;

	/**Threshold for detecting long execution*/
	private final int lateThreshold;

	/**Time zone difference between local machine and rundeck server to correctly detect late execution*/
	private final long dateDelta;

	/**Rundeck client API used to interact with rundeck rest API*/
	private final RundeckClient rundeckClient;

	/**Tray icon and his menu for updating jobs and state displayed*/
	private final RundeckMonitorTrayIcon rundeckMonitorTrayIcon;

	/**Current state (failed job/long process/disconnected) of the rundeck monitor*/
	private final RundeckMonitorState rundeckMonitorState = new RundeckMonitorState();

	/**Set for all known late execution identifiers*/
	private Set<Long> knownLateExecutionIds = new LinkedHashSet<>();
	/**Set for all known failed execution identifiers*/
	private Set<Long> knownFailedExecutionIds = new LinkedHashSet<>();

	/**
	 * Initialize the rundeck monitor, load configuration and try to connect to the configured rundeck
	 *
	 * @throws IOException in case of loading configuration error
	 */
	public RundeckMonitor( final VersionChecker versionCheckerArg ) throws IOException {

		versionChecker = versionCheckerArg;

		//Configuration loading
		final File propertyFile = new File( RUNDECK_MONITOR_PROPERTIES_FILE );
		if( ! propertyFile.exists() ){

			JOptionPane.showMessageDialog( null, "Copy and configure " + RUNDECK_MONITOR_PROPERTIES_FILE + " file", RUNDECK_MONITOR_PROPERTIES_FILE + " file is missing", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit( 1 );
		}

		//Load the configuration file and extract properties
		final Properties prop = new Properties();
		prop.load( new FileInputStream( propertyFile ) );

		final String rundeckUrl = prop.getProperty( RUNDECK_PROPERTY_URL );
		final String rundeckApiKey = prop.getProperty( RUNDECK_PROPERTY_API_KEY );
		final String rundeckLogin = prop.getProperty( RUNDECK_PROPERTY_LOGIN );
		final String rundeckPassword = prop.getProperty( RUNDECK_PROPERTY_PASSWORD );
		rundeckProject = prop.getProperty( RUNDECK_PROPERTY_PROJECT );

		final String rundeckMonitorName = prop.getProperty( RUNDECK_MONITOR_PROPERTY_NAME, RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE );
		refreshDelay = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY, RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY_DEFAULT_VALUE ) );
		lateThreshold = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD, RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD_DEFAULT_VALUE ) );
		final int failedJobNumber = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER, RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER_DEFAULT_VALUE ) );
		final String dateFormat = prop.getProperty( RUNDECK_MONITOR_PROPERTY_DATE_FORMAT, RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE );
		final String version = prop.getProperty( RUNDECK_MONITOR_PROPERTY_API_VERSION, RUNDECK_MONITOR_PROPERTY_API_VERSION_DEFAULT_VALUE );

		//Initialize the client builder with token  or login/password authentication
		final RundeckClientBuilder rundeckClientBuilder;
		if( null != rundeckApiKey && ! rundeckApiKey.isEmpty() ) {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckApiKey );
		}
		else {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckLogin, rundeckPassword );
		}

		//Initialize the rundeck client with or without version
		if( null != version && ! version.isEmpty() ) {
			rundeckClient = rundeckClientBuilder.version( Integer.parseInt( version ) ).build();
		}
		else {
			rundeckClient = rundeckClientBuilder.build();
		}

		//Time-zone delta between srundeck server and the computer where rundeck monitor is running
		dateDelta = rundeckClient.getSystemInfo().getDate().getTime() - new Date().getTime();

		//Initialize the tray icon
		rundeckMonitorTrayIcon = new RundeckMonitorTrayIcon( rundeckUrl, rundeckMonitorName, failedJobNumber, dateFormat, rundeckMonitorState );

		//Initialize and update the rundeck monitor failed/late jobs
		updateRundeckHistory( true );

		//Clean any temporary downloaded jar
		versionChecker.cleanOldAndTemporaryJar();
	}

	/**
	 * RundeckMonitor background process method executing the main loop
	 */
	public void run() {

		while( true ){
			try {

				//
				updateRundeckHistory( false );

				if( versionChecker.isDownloadDone() ) {

					//Restart, remove the tray icon and exit
					if( versionChecker.restart() ) {
						rundeckMonitorTrayIcon.disposeTrayIcon();
						System.exit( 0 );
					}
				}

				try {
					Thread.sleep( refreshDelay * 1000 );
				}
				catch ( final Exception e ) {

					//Nothing to do
				}
			}
			//If an exception is catch, consider the monitor as disconnected
			catch ( final Exception e ) {

				rundeckMonitorState.setDisconnected( true );
				rundeckMonitorTrayIcon.updateTrayIcon();

				try {

					Thread.sleep( refreshDelay * 1000 );
				}
				catch ( final InterruptedException e1) {

					//Nothing to do
				}
			}
		}
	}

	/**
	 * Call Rundeck rest API and update the monitor state and displayed jobs if there are new failed/late jobs
	 *
	 * @param init boolean to indicate if it's the first call to this method for the monitor initialization
	 */
	private void updateRundeckHistory( final boolean init ) {

		//call Rundeck rest API
		final ExecutionQuery executionQuery = ExecutionQuery.builder().project( rundeckProject ).status( ExecutionStatus.FAILED ).build();
		final PagedResults<RundeckExecution> lastFailedJobs = rundeckClient.getExecutions( executionQuery, Long.valueOf( 10 ), null );
		final List<RundeckExecution> currentExecutions= rundeckClient.getRunningExecutions( rundeckProject );

		//Rundeck calls are OK
		rundeckMonitorState.setDisconnected( false );

		final Date currentTime = new Date();

		final List<JobExecutionInfo> listJobExecutionInfo = new ArrayList<>();

		boolean lateExecutionFound = false;

		//Scan runnings jobs to detect if they are late
		for( final RundeckExecution rundeckExecution : currentExecutions ) {

			if( currentTime.getTime() - rundeckExecution.getStartedAt().getTime() + dateDelta > lateThreshold * 1000 ) {

				lateExecutionFound = true;

				final boolean newLongExecution = ! knownLateExecutionIds.contains( rundeckExecution.getId() );
				if( newLongExecution ) {
					knownLateExecutionIds.add( rundeckExecution.getId() );
				}

				final String jobName;
				if( null != rundeckExecution.getJob() ) {
					jobName = rundeckExecution.getJob().getName();
				}
				else {
					jobName = rundeckExecution.getDescription();
				}
				listJobExecutionInfo.add( new JobExecutionInfo( rundeckExecution.getId(), rundeckExecution.getStartedAt(), jobName, true, newLongExecution ) );
			}
		}

		rundeckMonitorState.setLateJobs( lateExecutionFound );

		//Add all lasts failed jobs to the list
		for( final RundeckExecution rundeckExecution : lastFailedJobs.getResults() ) {

			final boolean newFailedJob = ! knownFailedExecutionIds.contains( rundeckExecution.getId() );
			if( newFailedJob ) {

				rundeckMonitorState.setFailedJobs( true );
				knownFailedExecutionIds.add( rundeckExecution.getId() );
			}

			final String jobName;
			if( null != rundeckExecution.getJob() ) {
				jobName = rundeckExecution.getJob().getName();
			}
			else {
				jobName = rundeckExecution.getDescription();
			}
			listJobExecutionInfo.add( new JobExecutionInfo( Long.valueOf( rundeckExecution.getId() ), rundeckExecution.getStartedAt(), jobName, false, newFailedJob && ! init ) );
		}

		//Display failed/late jobs on the trayIcon menu
		rundeckMonitorTrayIcon.updateExecutionIdsList( listJobExecutionInfo );

		if( init ) {

			rundeckMonitorState.setFailedJobs( false );
		}

		//Update the tray icon color
		rundeckMonitorTrayIcon.updateTrayIcon();
	}

	/**
	 * RundeckMonitor main method
	 *
	 * @param args program arguments: none is expected and used
	 * @throws InterruptedException
	 */
	public static void main( final String args[] ) throws InterruptedException{

		//"Sylvain-Bugat", "RundeckMonitor",
		final VersionChecker versionChecker = new VersionChecker( "Sylvain-Bugat", "RundeckMonitor", "rundeck-monitor", "target", "-jar-with-dependencies" );

		//Clean any temporary downloaded jar
		versionChecker.cleanOldAndTemporaryJar();

		try {
			//Start the main thread
			new Thread( new RundeckMonitor( versionChecker ) ).start();

			//Start the version checker thread
			new Thread( versionChecker ).start();
		}
		catch ( final RundeckApiTokenException e ) {
			JOptionPane.showMessageDialog( null, "Invalid authentication token," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RUNDECK_PROPERTY_API_KEY + "\".", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			System.exit( 1 );
		}
		catch ( final RundeckApiLoginException e ) {
			JOptionPane.showMessageDialog( null, "Invalid login/password," + System.lineSeparator() + "check and change these parameters values:" + System.lineSeparator() + '"' + RUNDECK_PROPERTY_LOGIN + '"' + System.lineSeparator() + '"' + RUNDECK_PROPERTY_PASSWORD + "\".", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			System.exit( 1 );
		}
		catch ( final RundeckApiHttpStatusException e ) {

			if( 500 == e.getStatusCode() ) {
				JOptionPane.showMessageDialog( null, "Invalid project settings," + System.lineSeparator() + "check and change these parameters values:" + System.lineSeparator() + '"' + RUNDECK_PROPERTY_API_KEY + '"' + System.lineSeparator() + '"' + RUNDECK_PROPERTY_PROJECT + "\".", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else {
				final StringWriter stringWriter = new StringWriter();
				e.printStackTrace( new PrintWriter( stringWriter ) );
				JOptionPane.showMessageDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.exit( 1 );
		}
		catch ( final RundeckApiException e ) {

			//Connection error
			if( ConnectException.class.isInstance( e.getCause() ) ){
				JOptionPane.showMessageDialog( null, "Unable to connect to the project URL," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RUNDECK_PROPERTY_URL + "\".", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			else {
				final StringWriter stringWriter = new StringWriter();
				e.printStackTrace( new PrintWriter( stringWriter ) );
				JOptionPane.showMessageDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "undeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.exit( 1 );
		}
		catch ( final Exception e ) {

			final StringWriter stringWriter = new StringWriter();
			e.printStackTrace( new PrintWriter( stringWriter ) );
			JOptionPane.showMessageDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$

			System.exit( 1 );
		}
	}
}