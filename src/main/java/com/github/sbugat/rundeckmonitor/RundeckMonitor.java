package com.github.sbugat.rundeckmonitor;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

import org.rundeck.api.RundeckMonitorClient;
import org.rundeck.api.domain.RundeckEvent;
import org.rundeck.api.domain.RundeckExecution;
import org.rundeck.api.domain.RundeckHistory;

/**
 * Primary and main class of the Rundeck Monitor
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitor implements Runnable {

	private static final String PROPERTIES_FILE = "rundeckMonitor.properties"; //$NON-NLS-1$
	private static final String RUNDECK_JOB_EXECUTION_URL = "/execution/show/"; //$NON-NLS-1$
	private static final String RUNDECK_FAILED_JOB = "fail"; //$NON-NLS-1$

	private static final String RUNDECK_PROPERTY_URL = "rundeck.monitor.url"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_LOGIN = "rundeck.monitor.login"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_PASSWORD = "rundeck.monitor.password"; //$NON-NLS-1$
	private static final String RUNDECK_PROPERTY_PROJECT = "rundeck.monitor.project"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_NAME = "rundeck.monitor.name"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY = "rundeck.monitor.refresh.delay"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER = "rundeck.monitor.failed.job.number"; //$NON-NLS-1$
	private static final String RUNDECK_MONITOR_PROPERTY_DATE_FORMAT = "rundeck.monitor.date.format"; //$NON-NLS-1$

	private static final String RUNDECK_MONITOR_PROJECT_URL = "https://github.com/Sylvain-Bugat/RundeckMonitor"; //$NON-NLS-1$

	private final String rundeckUrl;
	private final String rundeckLogin;
	private final String rundeckPassword;
	private final String rundeckProject;

	private final String rundeckMonitorName;
	private final int refreshDelay;
	private final int failedJobNumber;
	private final String dateFormat;

	boolean monitorOK = true;

	/** OK image*/
	private final Image IMAGE_OK = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "OK.png" ) ); //$NON-NLS-1$
	/** WARNING image when a job seems to be blocked*/
	private final Image IMAGE_WARNING = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO.png" ) ); //$NON-NLS-1$
	/** KO image when a job has failed*/
	private final Image IMAGE_KO = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO.png" ) ); //$NON-NLS-1$
	/** Disconnected from rundeck image */
	private final Image imageDisconnect = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "DISCONNECTED.png" ) ); //$NON-NLS-1$

	private final RundeckMonitorClient rundeckClient;

	private final RundeckMonitorTrayIcon rundeckMonitorTrayIcon;

	private final RundeckMonitorState rundeckMonitorState= new RundeckMonitorState();

	private Map<Long, RundeckEvent> failedMap = new LinkedHashMap<Long, RundeckEvent>();

	private Set<Long> knownExecutionIds = new LinkedHashSet<>();

	private Map<MenuItem, Long> failedMenuItems = new LinkedHashMap<MenuItem, Long>();

	public RundeckMonitor() throws FileNotFoundException, IOException {

		//Configuration loading
		final Properties prop = new Properties();
		final File propertyFile = new File( PROPERTIES_FILE );
		if( ! propertyFile.exists() ){

			JOptionPane.showMessageDialog( null, "Copy and configure " + PROPERTIES_FILE + " file", PROPERTIES_FILE + " file is missing", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$

			System.exit( 1 );
		}

		prop.load( new FileInputStream( propertyFile ) );

		rundeckUrl = prop.getProperty( RUNDECK_PROPERTY_URL );
		rundeckLogin = prop.getProperty( RUNDECK_PROPERTY_LOGIN );
		rundeckPassword = prop.getProperty( RUNDECK_PROPERTY_PASSWORD );
		rundeckProject = prop.getProperty( RUNDECK_PROPERTY_PROJECT );

		rundeckMonitorName = prop.getProperty( RUNDECK_MONITOR_PROPERTY_NAME );
		refreshDelay = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_REFRESH_DELAY ) );
		failedJobNumber = Integer.parseInt( prop.getProperty( RUNDECK_MONITOR_PROPERTY_FAILED_JOB_NUMBER ) );
		dateFormat = prop.getProperty( RUNDECK_MONITOR_PROPERTY_DATE_FORMAT );

		//Rundeck connection
		rundeckClient = new RundeckMonitorClient( rundeckUrl, rundeckLogin, rundeckPassword );

		//Init the tray icon
		rundeckMonitorTrayIcon = new RundeckMonitorTrayIcon( rundeckUrl, rundeckMonitorName, failedJobNumber, dateFormat, rundeckMonitorState );

		updateRundeckHistory( true );
	}

	/**
	 * RundeckMonitor main loop method
	 */
	public void run() {

		while( true ){
			try {

				updateRundeckHistory( false );

				try {
					Thread.sleep( refreshDelay * 1000 );
				}
				catch ( final Exception e ) {

					//Nothing to do
				}
			}
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

	private void updateRundeckHistory( final boolean init ) {

		final RundeckHistory lastFailedJobs = rundeckClient.getHistory( rundeckProject, RUNDECK_FAILED_JOB, Long.valueOf( failedJobNumber ), Long.valueOf(0) );
		final List<RundeckExecution> currentExecutions= rundeckClient.getRunningExecutions( rundeckProject );

		rundeckMonitorState.setDisconnected( false );

		final Date currentTime = new Date();

		final List<JobExecutionInfo> listJobExecutionInfo = new ArrayList<>();

		for( final RundeckExecution rundeckExecution : currentExecutions ) {

			if( currentTime.getTime() - rundeckExecution.getStartedAt().getTime() > 0 ) {
				listJobExecutionInfo.add( new JobExecutionInfo( rundeckExecution.getId(), rundeckExecution.getStartedAt(), rundeckExecution.getDescription() ) );

				if( ! knownExecutionIds.contains( rundeckExecution.getId() ) ) {

					rundeckMonitorState.setLateJobs( true );
				}
			}
		}

		for( final RundeckEvent rundeckEvent : lastFailedJobs.getEvents() ) {

			listJobExecutionInfo.add( new JobExecutionInfo( Long.valueOf( rundeckEvent.getExecutionId() ), rundeckEvent.getStartedAt(), rundeckEvent.getTitle() ) );

			if( ! knownExecutionIds.contains( rundeckEvent.getExecutionId() ) ) {

				rundeckMonitorState.setFailedJobs( true );
				knownExecutionIds.add( rundeckEvent.getExecutionId() );
			}
		}

		rundeckMonitorTrayIcon.updateExecutionIdsList( listJobExecutionInfo );

		if( init ) {

			rundeckMonitorState.setFailedJobs( false );
		}

		rundeckMonitorTrayIcon.updateTrayIcon();
	}

	public static void main( final String args[] ){

		try {
			new Thread( new RundeckMonitor() ).start();
		} catch ( final Exception e) {

			final StringWriter stringWriter = new StringWriter();
			e.printStackTrace( new PrintWriter( stringWriter ) );
			JOptionPane.showMessageDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$

			System.exit( 1 );
		}
	}
}