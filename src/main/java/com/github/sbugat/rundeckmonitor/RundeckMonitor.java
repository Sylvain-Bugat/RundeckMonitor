package com.github.sbugat.rundeckmonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.rundeck.api.domain.RundeckProject;
import org.rundeck.api.query.ExecutionQuery;
import org.rundeck.api.util.PagedResults;

import com.github.sbugat.rundeckmonitor.configuration.InvalidPropertyException;
import com.github.sbugat.rundeckmonitor.configuration.MissingPropertyException;
import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.wizard.RundeckMonitorConfigurationWizard;

/**
 * Primary and main class of the Rundeck Monitor
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitor implements Runnable {

	private final VersionChecker versionChecker;

	/**Configuration of the rundeck monitor with default values if some properties are missing or are empty*/
	private final RundeckMonitorConfiguration rundeckMonitorConfiguration;

	/**Time zone difference between local machine and rundeck server to correctly detect late execution*/
	private long dateDelta;

	/**Rundeck client API used to interact with rundeck rest API*/
	private RundeckClient rundeckClient;

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
	 * @throws InvalidPropertyException
	 * @throws MissingPropertyException
	 */
	public RundeckMonitor( final RundeckMonitorConfiguration rundeckMonitorConfigurationArg, final VersionChecker versionCheckerArg ) throws IOException, MissingPropertyException, InvalidPropertyException {

		versionChecker = versionCheckerArg;
		rundeckMonitorConfiguration = rundeckMonitorConfigurationArg;

		//Configuration checking
		rundeckMonitorConfiguration.verifyConfiguration();

		//Initialize the client builder with token  or login/password authentication
		final RundeckClientBuilder rundeckClientBuilder;
		final String rundeckAPIKey = rundeckMonitorConfiguration.getRundeckAPIKey();
		final String rundeckUrl = rundeckMonitorConfiguration.getRundeckUrl();
		if( null != rundeckAPIKey && ! rundeckAPIKey.isEmpty() ) {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckAPIKey );
		}
		else {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckMonitorConfiguration.getRundeckLogin(), rundeckMonitorConfiguration.getRundeckPassword() );
		}

		//Initialize the rundeck client with or without version
		final String rundeckAPIVersion= rundeckMonitorConfiguration.getRundeckAPIversion();
		if( null != rundeckAPIVersion && ! rundeckAPIVersion.isEmpty() ) {
			rundeckClient = rundeckClientBuilder.version( Integer.parseInt( rundeckAPIVersion ) ).build();
		}
		else {
			rundeckClient = rundeckClientBuilder.build();
		}

		//Test authentication credentials
		rundeckClient.testAuth();

		//Check if the configured project exists
		boolean existingProject = false;
		for( final RundeckProject rundeckProject: rundeckClient.getProjects() ) {

			if( rundeckMonitorConfiguration.getRundeckProject().equals( rundeckProject.getName() ) ) {
				existingProject = true;
				break;
			}
		}

		if( ! existingProject ) {
			JOptionPane.showMessageDialog( null, "Invalid rundeck project," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_PROJECT + '=' + rundeckMonitorConfiguration.getRundeckProject() + "\".", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			System.exit( 1 );
		}

		//Time-zone delta between srundeck server and the computer where rundeck monitor is running
		dateDelta = rundeckClient.getSystemInfo().getDate().getTime() - new Date().getTime();

		//Initialize the tray icon
		rundeckMonitorTrayIcon = new RundeckMonitorTrayIcon( rundeckMonitorConfiguration, rundeckMonitorState );

		//Initialize and update the rundeck monitor failed/late jobs
		updateRundeckHistory( true );

		//Clean any temporary downloaded jar
		versionChecker.cleanOldAndTemporaryJar();
	}

	public void reloadConfiguration() throws IOException, MissingPropertyException, InvalidPropertyException {

		//Configuration checking
		rundeckMonitorConfiguration.loadConfigurationPropertieFile();
		rundeckMonitorConfiguration.verifyConfiguration();

		//Initialize the client builder with token  or login/password authentication
		final RundeckClientBuilder rundeckClientBuilder;
		final String rundeckAPIKey = rundeckMonitorConfiguration.getRundeckAPIKey();
		final String rundeckUrl = rundeckMonitorConfiguration.getRundeckUrl();
		if( null != rundeckAPIKey && ! rundeckAPIKey.isEmpty() ) {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckAPIKey );
		}
		else {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckMonitorConfiguration.getRundeckLogin(), rundeckMonitorConfiguration.getRundeckPassword() );
		}

		//Initialize the rundeck client with or without version
		final String rundeckAPIVersion= rundeckMonitorConfiguration.getRundeckAPIversion();
		if( null != rundeckAPIVersion && ! rundeckAPIVersion.isEmpty() ) {
			rundeckClient = rundeckClientBuilder.version( Integer.parseInt( rundeckAPIVersion ) ).build();
		}
		else {
			rundeckClient = rundeckClientBuilder.build();
		}

		//Test authentication credentials
		rundeckClient.testAuth();

		//Check if the configured project exists
		boolean existingProject = false;
		for( final RundeckProject rundeckProject: rundeckClient.getProjects() ) {

			if( rundeckMonitorConfiguration.getRundeckProject().equals( rundeckProject.getName() ) ) {
				existingProject = true;
				break;
			}
		}

		if( ! existingProject ) {
			JOptionPane.showMessageDialog( null, "Invalid rundeck project," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_PROJECT + '=' + rundeckMonitorConfiguration.getRundeckProject() + "\".", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			System.exit( 1 );
		}

		//Time-zone delta between srundeck server and the computer where rundeck monitor is running
		dateDelta = rundeckClient.getSystemInfo().getDate().getTime() - new Date().getTime();

		//Reinit monitor state
		rundeckMonitorState.setFailedJobs( false );
		rundeckMonitorState.setLateJobs( false );
		rundeckMonitorState.setDisconnected( false );

		//Initialize and update the rundeck monitor failed/late jobs
		updateRundeckHistory( true );
	}


	private boolean checkNewConfiguration( final Date lastConfigurationUpdateDate ) {

		try {
			if( RundeckMonitorConfiguration.propertiesFileUpdated( lastConfigurationUpdateDate ) ) {

				//reload the configuration
				try {
					reloadConfiguration();
					rundeckMonitorTrayIcon.reloadConfiguration();
					return true;
				}
				catch( final MissingPropertyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch( final InvalidPropertyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch( final IOException e) {
			//Ignore configuration checking and loading error
			return true;
		}

		return false;
	}

	/**
	 * RundeckMonitor background process method executing the main loop
	 */
	public void run() {

		Date lastConfigurationUpdateDate = new Date();

		while( true ){
			try {

				if( checkNewConfiguration( lastConfigurationUpdateDate ) ) {
					lastConfigurationUpdateDate = new Date();
				}

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
					Thread.sleep( rundeckMonitorConfiguration.getRefreshDelay() * 1000 );
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

					Thread.sleep( rundeckMonitorConfiguration.getRefreshDelay() * 1000 );
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
		final ExecutionQuery executionQuery = ExecutionQuery.builder().project( rundeckMonitorConfiguration.getRundeckProject() ).status( ExecutionStatus.FAILED ).build();
		final PagedResults<RundeckExecution> lastFailedJobs = rundeckClient.getExecutions( executionQuery, Long.valueOf( rundeckMonitorConfiguration.getFailedJobNumber() ), null );

		final List<RundeckExecution> currentExecutions = rundeckClient.getRunningExecutions( rundeckMonitorConfiguration.getRundeckProject() );

		//Rundeck calls are OK
		rundeckMonitorState.setDisconnected( false );

		final Date currentTime = new Date();

		final List<JobExecutionInfo> listJobExecutionInfo = new ArrayList<>();

		boolean lateExecutionFound = false;

		//Scan runnings jobs to detect if they are late
		for( final RundeckExecution rundeckExecution : currentExecutions ) {

			if( currentTime.getTime() - rundeckExecution.getStartedAt().getTime() + dateDelta > rundeckMonitorConfiguration.getLateThreshold() * 1000 ) {

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
	public static void main( final String args[] ) /*throws InterruptedException*/{

		//Launch the configuration wizard if there is no configuration file
		if( ! RundeckMonitorConfiguration.propertiesFileExists() ) {
			new RundeckMonitorConfigurationWizard( new RundeckMonitorConfiguration() );
		}

		//Wait until the configuration file is created
		while( ! RundeckMonitorConfiguration.propertiesFileExists() ) {

			try {
				Thread.sleep( 1000 );
			}
			catch( final InterruptedException e ) {
				//Ignore this error
			}
		}

		//Initialization of the version checker
		final VersionChecker versionChecker = new VersionChecker( "Sylvain-Bugat", "RundeckMonitor", "rundeck-monitor", "target", "-jar-with-dependencies" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		//Clean any temporary downloaded jar
		versionChecker.cleanOldAndTemporaryJar();

		final Object[] options = { "Exit", "Edit configuration" }; //$NON-NLS-1$ //$NON-NLS-2$

		while( true ) {

			int errorUserReturn = JOptionPane.YES_OPTION;

			final RundeckMonitorConfiguration rundeckMonitorConfiguration = new RundeckMonitorConfiguration();

			try {

				//Configuration loading
				rundeckMonitorConfiguration.loadConfigurationPropertieFile();

				//Start the main thread
				new Thread( new RundeckMonitor( rundeckMonitorConfiguration, versionChecker ) ).start();

				//Start the version checker thread
				new Thread( versionChecker ).start();

				//Monitor and Version started without exception, end the launch thread
				return;
			}

			//Loading properties exceptions
			catch( final MissingPropertyException e ) {
				errorUserReturn = JOptionPane.showOptionDialog( null, "Missing mandatory property," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + e.getProperty() + "\".", "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] );  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
			}
			catch( final InvalidPropertyException e ) {
				errorUserReturn = JOptionPane.showOptionDialog( null, "Invalid property value:" + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + e.getProperty() + '=' + e.getPropertyValue() + "\".", "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] );  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
			}
			//Loading configuration file I/O exception
			catch( final IOException e ) {
				errorUserReturn = JOptionPane.showOptionDialog( null, "Error loading property file:" + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTIES_FILE + "check access rights of this file." , "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			//Authentication exceptions
			catch ( final RundeckApiTokenException e ) {
				errorUserReturn = JOptionPane.showOptionDialog( null, "Invalid authentication token," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_API_KEY + "\".", "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			catch ( final RundeckApiLoginException e ) {
				errorUserReturn = JOptionPane.showOptionDialog( null, "Invalid login/password," + System.lineSeparator() + "check and change these parameters values:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_LOGIN + '"' + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_PASSWORD + "\".", "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] );  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
			}
			catch ( final RundeckApiHttpStatusException e ) {

				if( 500 == e.getStatusCode() ) {
					errorUserReturn = JOptionPane.showOptionDialog( null, "Invalid project settings," + System.lineSeparator() + "check and change these parameters values:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_API_KEY + '"' + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_PROJECT + "\".", "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				else {
					final StringWriter stringWriter = new StringWriter();
					e.printStackTrace( new PrintWriter( stringWriter ) );
					errorUserReturn = JOptionPane.showOptionDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] ); //$NON-NLS-1$
				}
			}
			catch ( final RundeckApiException e ) {

				//Connection error
				if( ConnectException.class.isInstance( e.getCause() ) ){
					JOptionPane.showOptionDialog( null, "Unable to connect to the project URL," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_URL + "\".", "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				else {
					final StringWriter stringWriter = new StringWriter();
					e.printStackTrace( new PrintWriter( stringWriter ) );
					errorUserReturn = JOptionPane.showOptionDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] ); //$NON-NLS-1$
				}
			}

			catch ( final Exception e ) {

				final StringWriter stringWriter = new StringWriter();
				e.printStackTrace( new PrintWriter( stringWriter ) );
				errorUserReturn = JOptionPane.showOptionDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] ); //$NON-NLS-1$
			}

			//
			if( JOptionPane.YES_OPTION == errorUserReturn ) {

				System.exit( 1 );
			}

			//Date the current time and launch the configuration wizard
			final Date systemDate = new Date();

			new RundeckMonitorConfigurationWizard( rundeckMonitorConfiguration );

			//Wait until the configuration file is updated
			boolean configurationFileUpdated = false;
			while( ! configurationFileUpdated ) {

				try {
					Thread.sleep( 1000 );
					configurationFileUpdated = RundeckMonitorConfiguration.propertiesFileUpdated( systemDate );
				}
				catch( final IOException | InterruptedException e ) {
					//Ignore these error
				}
			}
		}
	}
}