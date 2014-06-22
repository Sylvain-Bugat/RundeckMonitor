package com.github.sbugat.rundeckmonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

import org.rundeck.api.RundeckClient;
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
	private static final String RUNDECK_PROPERTY_LOGIN = "rundeck.monitor.login"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_PASSWORD = "rundeck.monitor.password"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_PROJECT = "rundeck.monitor.project"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_NAME = "rundeck.monitor.name"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY = "rundeck.monitor.refresh.delay"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD = "rundeck.monitor.execution.late.threshold"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER = "rundeck.monitor.failed.job.number"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT = "rundeck.monitor.date.format"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_API_VERSION = "rundeck.monitor.api.version"; //$NON-NLS-1$

	private final String rundeckProject;

	private final int refreshDelay;
	private final int lateThreshold;
	private final int failedJobNumber;
	private final String dateFormat;

	private final RundeckClient rundeckClient;

	private final RundeckMonitorTrayIcon rundeckMonitorTrayIcon;

	private final RundeckMonitorState rundeckMonitorState= new RundeckMonitorState();

	private Set<Long> knownLateExecutionIds = new LinkedHashSet<>();

	private Set<Long> knownFailedExecutionIds = new LinkedHashSet<>();

	public RundeckMonitor() throws FileNotFoundException, IOException {

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
		final String rundeckLogin = prop.getProperty( RUNDECK_PROPERTY_LOGIN );
		final String rundeckPassword = prop.getProperty( RUNDECK_PROPERTY_PASSWORD );
		rundeckProject = prop.getProperty( RUNDECK_PROPERTY_PROJECT );

		final String rundeckMonitorName = prop.getProperty( RUNDECK_MONITOR_PROPERTY_NAME );
		refreshDelay = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY ) );
		lateThreshold = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_EXECUTION_LATE_THRESHOLD ) );
		failedJobNumber = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER ) );
		dateFormat = prop.getProperty( RUNDECK_MONITOR_PROPERTY_DATE_FORMAT );
		final String version = prop.getProperty( RUNDECK_MONITOR_PROPERTY_API_VERSION );

		//Initialize the rundeck connection with or without version
		if( null != version && ! version.isEmpty() ) {
			 Integer.parseInt( version );
			rundeckClient = RundeckClient.builder().url( rundeckUrl ).login( rundeckLogin, rundeckPassword ).version( Integer.parseInt( version ) ).build();
		}
		else {
			rundeckClient = RundeckClient.builder().url( rundeckUrl ).login( rundeckLogin, rundeckPassword ).build();
		}

		//Init the tray icon
		rundeckMonitorTrayIcon = new RundeckMonitorTrayIcon( rundeckUrl, rundeckMonitorName, failedJobNumber, dateFormat, rundeckMonitorState );

		//Initialize and update the rundeck monitor failed/late jobs
		updateRundeckHistory( true );
	}

	/**
	 * RundeckMonitor background process method executing the main loop
	 */
	public void run() {

		while( true ){
			try {

				//
				updateRundeckHistory( false );

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
		final List<RundeckExecution> currentExecutions = rundeckClient.getRunningExecutions( rundeckProject );

		//Rundeck calls are OK
		rundeckMonitorState.setDisconnected( false );

		final Date currentTime = new Date();

		final List<JobExecutionInfo> listJobExecutionInfo = new ArrayList<>();

		boolean lateExecutionFound = false;

		//Scan runnings jobs to detect if they are late
		for( final RundeckExecution rundeckExecution : currentExecutions ) {

			if( currentTime.getTime() - rundeckExecution.getStartedAt().getTime() > lateThreshold * 1000 ) {

				final boolean newLongExecution = ! knownLateExecutionIds.contains( rundeckExecution.getId() );
				if( newLongExecution ) {
					lateExecutionFound = true;
				}

				listJobExecutionInfo.add( new JobExecutionInfo( rundeckExecution.getId(), rundeckExecution.getStartedAt(), rundeckExecution.getJob().getDescription(), true, newLongExecution ) );
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
	 */
	public static void main( final String args[] ){

		try {
			new Thread( new RundeckMonitor() ).start();
		}
		catch ( final Exception e) {

			final StringWriter stringWriter = new StringWriter();
			e.printStackTrace( new PrintWriter( stringWriter ) );
			JOptionPane.showMessageDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$

			System.exit( 1 );
		}
	}
}