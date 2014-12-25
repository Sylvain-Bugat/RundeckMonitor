package com.github.sbugat.rundeckmonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.rundeck.api.RundeckApiException.RundeckApiLoginException;
import org.rundeck.api.RundeckApiException.RundeckApiTokenException;
import org.rundeck.api.RundeckClient;
import org.rundeck.api.domain.RundeckExecution;
import org.rundeck.api.domain.RundeckExecution.ExecutionStatus;
import org.rundeck.api.domain.RundeckProject;
import org.rundeck.api.query.ExecutionQuery;
import org.rundeck.api.util.PagedResults;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.github.sbugat.rundeckmonitor.configuration.InvalidPropertyException;
import com.github.sbugat.rundeckmonitor.configuration.MissingPropertyException;
import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.configuration.UnknownProjectException;
import com.github.sbugat.rundeckmonitor.tools.EnvironmentTools;
import com.github.sbugat.rundeckmonitor.tools.RundeckClientTools;
import com.github.sbugat.rundeckmonitor.tools.SystemTools;
import com.github.sbugat.rundeckmonitor.wizard.InterfaceType;
import com.github.sbugat.rundeckmonitor.wizard.RundeckMonitorConfigurationWizard;

/**
 * Primary and main class of the Rundeck Monitor.
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitor implements Runnable {

	/**SLF4J XLogger.*/
	private static final XLogger LOG = XLoggerFactory.getXLogger( RundeckMonitor.class );

	private final VersionChecker versionChecker;

	/**Configuration of the rundeck monitor with default values if some properties are missing or are empty.*/
	private final RundeckMonitorConfiguration rundeckMonitorConfiguration;

	/**Time zone difference between local machine and rundeck server to correctly detect late execution.*/
	private long dateDelta;

	/**Rundeck client API used to interact with rundeck rest API.*/
	private RundeckClient rundeckClient;

	/**Tray icon and his menu for updating jobs and state displayed.*/
	private final RundeckMonitorTrayIcon rundeckMonitorTrayIcon;

	/**Current state (failed job/long process/disconnected) of the rundeck monitor.*/
	private final RundeckMonitorState rundeckMonitorState = new RundeckMonitorState();

	/**Set for all known late execution identifiers.*/
	private Set<Long> knownLateExecutionIds = new LinkedHashSet<>();
	/**Set for all known failed execution identifiers.*/
	private Set<Long> knownFailedExecutionIds = new LinkedHashSet<>();

	/**
	 * Initialize the rundeck monitor, load configuration and try to connect to the configured rundeck.
	 *
	 * @throws IOException in case of loading configuration error
	 * @throws InvalidPropertyException in case of loading configuration property error
	 * @throws MissingPropertyException in case of loading configuration property error
	 * @throws UnknownProjectException in case of unknown RunDeck project
	 */
	public RundeckMonitor( final RundeckMonitorConfiguration rundeckMonitorConfigurationArg, final VersionChecker versionCheckerArg ) throws IOException, MissingPropertyException, InvalidPropertyException, UnknownProjectException {

		LOG.entry();

		versionChecker = versionCheckerArg;
		rundeckMonitorConfiguration = rundeckMonitorConfigurationArg;

		//Configuration checking and initialize a new Rundeck client
		initRundeckClient();

		//Initialize the tray icon
		if( EnvironmentTools.isWindows() && InterfaceType.SWING.name().equals( rundeckMonitorConfiguration.getInterfaceType() ) ) {
			rundeckMonitorTrayIcon = new RundeckMonitorSwingTrayIcon( rundeckMonitorConfiguration, rundeckMonitorState );
		}
		else {
			rundeckMonitorTrayIcon = new RundeckMonitorAWTTrayIcon( rundeckMonitorConfiguration, rundeckMonitorState );
		}

		try {
			//Initialize and update the rundeck monitor failed/late jobs
			updateRundeckHistory( true );

			//Clean any temporary downloaded jar
			versionChecker.cleanOldAndTemporaryJar();
		}
		catch(final Exception e) {
			rundeckMonitorTrayIcon.disposeTrayIcon();
			LOG.exit( e );
			throw e;
		}

		LOG.exit();
	}

	public void reloadConfiguration() throws IOException, MissingPropertyException, InvalidPropertyException, UnknownProjectException {

		//Configuration checking
		rundeckMonitorConfiguration.loadConfigurationPropertieFile();

		//Configuration checking and initialize a new Rundeck client
		initRundeckClient();

		//Time-zone delta between srundeck server and the computer where rundeck monitor is running
		dateDelta = rundeckClient.getSystemInfo().getDate().getTime() - new Date().getTime();

		//Reinit monitor state
		rundeckMonitorState.setFailedJobs( false );
		rundeckMonitorState.setLateJobs( false );
		rundeckMonitorState.setDisconnected( false );

		//Initialize and update the rundeck monitor failed/late jobs
		updateRundeckHistory( true );

		LOG.exit();
	}

	/**
	 * Check the configuration and initialize a new rundeck client.
	 *
	 * @throws MissingPropertyException when check configuration
	 * @throws InvalidPropertyException when check configuration
	 * @throws UnknownProjectException if the configured project is unknown
	 */
	private void initRundeckClient() throws MissingPropertyException, InvalidPropertyException, UnknownProjectException {

		//Configuration checking
		rundeckMonitorConfiguration.verifyConfiguration();

		//Initialize the rundeck client with the API version
		rundeckClient = RundeckClientTools.buildRundeckClient( rundeckMonitorConfiguration, false );

		//Check if the configured project exists
		boolean existingProject = false;
		for( final RundeckProject rundeckProject: rundeckClient.getProjects() ) {

			if( rundeckMonitorConfiguration.getRundeckProject().equals( rundeckProject.getName() ) ) {
				existingProject = true;
				break;
			}
		}

		if( ! existingProject ) {

			final UnknownProjectException exception = new UnknownProjectException( rundeckMonitorConfiguration.getRundeckProject() );
			LOG.error( "Error unknown project: {}", rundeckMonitorConfiguration.getRundeckProject() ); //$NON-NLS-1$
			LOG.exit( exception );
			throw exception;
		}

		//Time-zone delta between srundeck server and the computer where rundeck monitor is running
		dateDelta = rundeckClient.getSystemInfo().getDate().getTime() - new Date().getTime();
	}

	private boolean checkNewConfiguration( final Date lastConfigurationUpdateDate ) {

		LOG.entry( lastConfigurationUpdateDate );

		Date lastConfigurationDate = lastConfigurationUpdateDate;

		if( RundeckMonitorConfiguration.propertiesFileUpdated( lastConfigurationDate ) ) {

			//Wait until configuration is reloaded or exit
			while( true ) {

				if( RundeckMonitorConfiguration.propertiesFileUpdated( lastConfigurationDate ) ) {
					//reload the configuration
					try {
						reloadConfiguration();
						rundeckMonitorTrayIcon.reloadConfiguration();

						//Set the tray icon as reconnected
						rundeckMonitorState.setDisconnected( false );
						rundeckMonitorTrayIcon.updateTrayIcon();
						LOG.exit( true );
						return true;
					}
					catch( final IOException | MissingPropertyException | InvalidPropertyException | UnknownProjectException | RuntimeException e) {

						//Set the tray icon as disconnected
						rundeckMonitorState.setDisconnected( true );
						rundeckMonitorTrayIcon.updateTrayIcon();

						if( handleStartupException( e, false ) ) {

							new RundeckMonitorConfigurationWizard( rundeckMonitorConfiguration, true );
							lastConfigurationDate = new Date();
						}
						//Dispose tray icon and exit
						else {
							rundeckMonitorTrayIcon.disposeTrayIcon();
							SystemTools.exit( SystemTools.EXIT_CODE_ERROR );
						}
					}
				}

				//Wait 1s
				try {

					Thread.sleep( 1000 );
				}
				catch ( final InterruptedException e) {

					//Nothing to do
					LOG.error( "Waiting interrupted", e ); //$NON-NLS-1$
				}
			}
		}

		LOG.exit( false );
		return false;
	}

	/**
	 * RundeckMonitor background process method executing the main loop.
	 */
	public void run() {

		LOG.entry();

		Date lastConfigurationUpdateDate = new Date();

		while( true ){
			try {

				if( checkNewConfiguration( lastConfigurationUpdateDate ) ) {
					lastConfigurationUpdateDate = new Date();
				}

				//Update the tray icon menu
				updateRundeckHistory( false );

				if( versionChecker.isversionCheckerDisabled() ) {

					rundeckMonitorConfiguration.disableVersionChecker();
					rundeckMonitorConfiguration.saveMonitorConfigurationPropertieFile();

					versionChecker.resetVersionCheckerDisabled();
				}

				//If download finished
				if( versionChecker.isDownloadDone() && versionChecker.restart() ) {

					//Restart, remove the tray icon and exit
					rundeckMonitorTrayIcon.disposeTrayIcon();
					SystemTools.exit( SystemTools.EXIT_CODE_OK );
				}

				try {
					Thread.sleep( rundeckMonitorConfiguration.getRefreshDelay() * 1000l );
				}
				catch ( final Exception e ) {

					//Nothing to do
					LOG.error( "Waiting interrupted", e ); //$NON-NLS-1$
				}
			}
			//If an exception is catch, consider the monitor as disconnected
			catch ( final IOException | RuntimeException e ) {

				rundeckMonitorState.setDisconnected( true );
				rundeckMonitorTrayIcon.updateTrayIcon();

				try {

					Thread.sleep( rundeckMonitorConfiguration.getRefreshDelay() * 1000l );
				}
				catch ( final InterruptedException e1) {

					//Nothing to do
					LOG.error( "Waiting interrupted", e1 ); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Call Rundeck rest API and update the monitor state and displayed jobs if there are new failed/late jobs.
	 *
	 * @param init boolean to indicate if it's the first call to this method for the monitor initialization
	 */
	private void updateRundeckHistory( final boolean init ) {

		LOG.entry( init );

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

			if( currentTime.getTime() - rundeckExecution.getStartedAt().getTime() + dateDelta > rundeckMonitorConfiguration.getLateThreshold() * 1000l ) {

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
			listJobExecutionInfo.add( new JobExecutionInfo( rundeckExecution.getId(), rundeckExecution.getStartedAt(), jobName, false, newFailedJob && ! init ) );
		}

		//Display failed/late jobs on the trayIcon menu
		rundeckMonitorTrayIcon.updateExecutionIdsList( listJobExecutionInfo );

		if( init ) {

			rundeckMonitorState.setFailedJobs( false );
		}

		//Update the tray icon color
		rundeckMonitorTrayIcon.updateTrayIcon();

		LOG.exit();
	}

	/**
	 * Rundeck launcher exception handler, display an error message based on the argument exception.
	 *
	 * @param exception exception to analyze
	 * @param initialization indicate if the tray icon is not loaded yet
	 * @return true if the wizard needs to be launched
	 */
	private static boolean handleStartupException( final Exception exception, final boolean initialization ) {

		LOG.entry( exception, initialization );

		final String errorMessage;

		//Loading properties exceptions
		if( MissingPropertyException.class.isInstance( exception ) ) {

			errorMessage = "Missing mandatory property," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + (( MissingPropertyException ) exception).getProperty() + "\"."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else if( InvalidPropertyException.class.isInstance( exception ) ) {

			errorMessage = "Invalid property value:" + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + (( InvalidPropertyException ) exception).getProperty() + '=' + (( InvalidPropertyException ) exception).getPropertyValue() + "\"."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		//Unknown rundeck project exception
		else if( UnknownProjectException.class.isInstance( exception ) ) {

			errorMessage = "Unknown rundeck project:" + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_PROJECT + '=' + (( UnknownProjectException ) exception).getProjectName() + "\"."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		//Property file not found
		else if( FileNotFoundException.class.isInstance( exception ) ) {

			errorMessage = "Property file not found:" + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTIES_FILE + "check this file."; //$NON-NLS-1$ //$NON-NLS-2$
		}
		//Loading configuration file I/O exception
		else if( IOException.class.isInstance( exception ) ) {

			errorMessage = "Error loading property file:" + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTIES_FILE + "check access rights of this file."; //$NON-NLS-1$ //$NON-NLS-2$
		}
		//Authentication exceptions
		else if( RundeckApiTokenException.class.isInstance( exception ) ) {

			errorMessage = "Invalid authentication token," + System.lineSeparator() + "check and change this parameter value:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_API_KEY + "\"."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else if( RundeckApiLoginException.class.isInstance( exception ) ) {

			errorMessage = "Invalid login/password," + System.lineSeparator() + "check and change these parameters values:" + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_LOGIN + '"' + System.lineSeparator() + '"' + RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_PASSWORD + "\"."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else {

			final StringWriter stringWriter = new StringWriter();
			exception.printStackTrace( new PrintWriter( stringWriter ) );
			errorMessage = exception.getMessage() + System.lineSeparator() + stringWriter.toString();
		}

		//Show a dialog with edit configuration option
		final Object[] options = { "Exit", "Edit configuration" }; //$NON-NLS-1$ //$NON-NLS-2$

		final int errorUserReturn;
		if( initialization ) {
			errorUserReturn = JOptionPane.showOptionDialog( null, errorMessage, "RundeckMonitor initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] );  //$NON-NLS-1$
		}
		else {
			errorUserReturn = JOptionPane.showOptionDialog( null, errorMessage, "RundeckMonitor reload configuration error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[ 0 ] );  //$NON-NLS-1$
		}

		if( JOptionPane.NO_OPTION == errorUserReturn ) {

			LOG.exit( true );
			return true;
		}

		LOG.exit( false );
		return false;
	}

	/**
	 * RundeckMonitor main method.
	 *
	 * @param args program arguments: none is expected and used
	 */
	public static void main( final String[] args ) {

		LOG.entry( ( Object[] ) args );

		//Launch the configuration wizard if there is no configuration file
		if( ! RundeckMonitorConfiguration.propertiesFileExists() ) {
			LOG.info( "Launching configuration wizard" ); //$NON-NLS-1$
			new RundeckMonitorConfigurationWizard( new RundeckMonitorConfiguration(), true );
		}

		//Wait until the configuration file is created
		while( ! RundeckMonitorConfiguration.propertiesFileExists() ) {

			try {
				Thread.sleep( 1000 );
			}
			catch( final InterruptedException e ) {
				LOG.error( "Waiting interrupted", e ); //$NON-NLS-1$
			}
		}

		//Initialization of the version checker
		final VersionChecker versionChecker = new VersionChecker( "Sylvain-Bugat", "RundeckMonitor", "rundeck-monitor", "-jar-with-dependencies" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		//Clean any temporary downloaded jar
		versionChecker.cleanOldAndTemporaryJar();

		while( true ) {

			final RundeckMonitorConfiguration rundeckMonitorConfiguration = new RundeckMonitorConfiguration();

			try {

				//Configuration loading
				rundeckMonitorConfiguration.loadConfigurationPropertieFile();

				//Start the main thread
				new Thread( new RundeckMonitor( rundeckMonitorConfiguration, versionChecker ) ).start();

				if( rundeckMonitorConfiguration.isVersionCheckerEnabled() ) {
					//Start the version checker thread
					LOG.info( "Launching version checker" ); //$NON-NLS-1$
					new Thread( versionChecker ).start();
				}

				//Monitor and Version started without exception, end the launch thread
				return;
			}
			catch ( final IOException | MissingPropertyException | InvalidPropertyException | UnknownProjectException | RuntimeException e ) {

				if( ! handleStartupException( e, true ) ) {
					SystemTools.exit( SystemTools.EXIT_CODE_ERROR );
				}
			}

			//Date the current time and launch the configuration wizard
			final Date systemDate = new Date();

			new RundeckMonitorConfigurationWizard( rundeckMonitorConfiguration, true );

			//Wait until the configuration file is updated
			boolean configurationFileUpdated = false;
			while( ! configurationFileUpdated ) {

				try {
					Thread.sleep( 1000 );
				}
				catch( final InterruptedException e ) {
					LOG.error( "Waiting interrupted", e ); //$NON-NLS-1$
				}
				configurationFileUpdated = RundeckMonitorConfiguration.propertiesFileUpdated( systemDate );
			}
		}
	}
}
