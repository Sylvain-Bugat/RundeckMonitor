package com.github.sbugat.rundeckmonitor;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

/**
 * Tray icon management class
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitorTrayIcon {

	/** URL to access job execution details */
	private static final String RUNDECK_JOB_EXECUTION_URL = "/execution/show/"; //$NON-NLS-1$

	/** GitHub Project URL */
	private static final String RUNDECK_MONITOR_PROJECT_URL = "https://github.com/Sylvain-Bugat/RundeckMonitor"; //$NON-NLS-1$

	/** Marker on the job when it is too long*/
	private static final String LONG_EXECUTION_MARKER = " - LONG EXECUTION"; //$NON-NLS-1$

	/** OK image*/
	private final Image IMAGE_OK = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "OK.png" ) ); //$NON-NLS-1$
	/** WARNING image when a job seems to be blocked*/
	private final Image IMAGE_LATE = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "LATE.png" ) ); //$NON-NLS-1$
	/** KO image when a job has failed*/
	private final Image IMAGE_KO = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO.png" ) ); //$NON-NLS-1$
	/** KO image when a job has failed and a job seems to be blocked*/
	private final Image IMAGE_KO_LATE = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO_LATE.png" ) ); //$NON-NLS-1$
	/** Disconnected from rundeck image */
	private final Image IMAGE_DISCONNECTED = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "DISCONNECTED.png" ) ); //$NON-NLS-1$

	/** Task bar tray icon*/
	private final TrayIcon trayIcon;
	/** System tray */
	private final SystemTray tray = SystemTray.getSystemTray();
	/** System default browser to open execution details*/
	private final Desktop desktop = Desktop.getDesktop();

	/**Date format to use for printing the Job start date*/
	private final String dateFormat;

	/**Current state of the trayIcon */
	private RundeckMonitorState rundeckMonitorState;

	/**MenuItem for lasts late/failed jobs*/
	private final Map<MenuItem, Long> failedMenuItems = new LinkedHashMap<MenuItem, Long>();

	/**
	 * Initialize the tray icon for the rundeckMonitor if the OS is compatible with it
	 *
	 * @param rundeckUrl
	 * @param rundeckMonitorName name of the application
	 * @param failedJobNumber size of the failed jobs list
	 * @param dateFormatArg date format to print jobs start date
	 * @param rundeckMonitorStateArg state of the rundeck monitor
	 */
	public RundeckMonitorTrayIcon( final String rundeckUrl, final String rundeckMonitorName, final int failedJobNumber, final String dateFormatArg, final RundeckMonitorState rundeckMonitorStateArg ) {

		dateFormat = dateFormatArg;
		rundeckMonitorState = rundeckMonitorStateArg;

		if( SystemTray.isSupported() ) {

			//Action listener to get job execution detail on the rundeck URL
			final ActionListener menuListener = new ActionListener() {
				@SuppressWarnings("synthetic-access")
				public void actionPerformed( final ActionEvent e) {

					if( MenuItem.class.isInstance( e.getSource() ) ){

						final Long executionId = failedMenuItems.get( e.getSource() );

						try {
							final URI executionURI = new URI( rundeckUrl + RUNDECK_JOB_EXECUTION_URL + executionId );
							desktop.browse( executionURI );
						}
						catch ( final URISyntaxException | IOException exception) {

							final StringWriter stringWriter = new StringWriter();
							exception.printStackTrace( new PrintWriter( stringWriter ) );
							JOptionPane.showMessageDialog( null, exception.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor redirection error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			};
			//Alert reset of the failed jobs state
			final ActionListener reinitListener = new ActionListener() {
				@SuppressWarnings("synthetic-access")
				public void actionPerformed( final ActionEvent e) {
					rundeckMonitorState.setFailedJobs( false );

					updateTrayIcon();
				}
			};
			//Rundeck monitor about
			final ActionListener aboutListener = new ActionListener() {
				@SuppressWarnings("synthetic-access")
				public void actionPerformed( final ActionEvent e) {

					try {
						final URI executionURI = new URI( RUNDECK_MONITOR_PROJECT_URL );
						desktop.browse( executionURI );
					}
					catch ( final URISyntaxException | IOException exception) {

						final StringWriter stringWriter = new StringWriter();
						exception.printStackTrace( new PrintWriter( stringWriter ) );
						JOptionPane.showMessageDialog( null, exception.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor redirection error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			};
			//Rundeck monitor exit
			final ActionListener exitListener = new ActionListener() {
				@SuppressWarnings("synthetic-access")
				public void actionPerformed( final ActionEvent e) {
					tray.remove( trayIcon );
					System.exit( 0 );
				}
			};

			//Popup menu
			final PopupMenu popupMenu = new PopupMenu();

			for( int i = 0 ; i < failedJobNumber ; i++ ){

				final MenuItem failedItem = new MenuItem();
				failedMenuItems.put( failedItem, null );
				popupMenu.add( failedItem );
				failedItem.addActionListener( menuListener );
			}

			popupMenu.addSeparator();
			final MenuItem reinitItem = new MenuItem( "Reset alert" ); //$NON-NLS-1$
			popupMenu.add( reinitItem );
			reinitItem.addActionListener( reinitListener );
			final MenuItem aboutItem = new MenuItem( "About RundeckMonitor" ); //$NON-NLS-1$
			popupMenu.add( aboutItem );
			aboutItem.addActionListener( aboutListener );
			final MenuItem exitItem = new MenuItem( "Quit" ); //$NON-NLS-1$
			popupMenu.add( exitItem );
			exitItem.addActionListener( exitListener );

			//Add the icon  to the system tray
			trayIcon = new TrayIcon( IMAGE_OK, rundeckMonitorName, popupMenu ); //$NON-NLS-1$
			trayIcon.setImageAutoSize( true );

			try {
				tray.add( trayIcon );
			}
			catch ( final AWTException e ) {

				final StringWriter stringWriter = new StringWriter();
				e.printStackTrace( new PrintWriter( stringWriter ) );
				JOptionPane.showMessageDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$

				System.exit( 1 );
			}
		}
		else {
			//if the System is not compatible with SystemTray
			trayIcon = null;

			JOptionPane.showMessageDialog( null, "SystemTray cannot be initialized", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$

			System.exit( 2 );
		}
	}

	/**
	 * Update the list of failed/late jobs
	 *
	 * @param listJobExecutionInfo list of failed and late jobs informations
	 */
	public void updateExecutionIdsList( final List<JobExecutionInfo> listJobExecutionInfo ) {

		int i=0;

		for( final Entry<MenuItem,Long> entry: failedMenuItems.entrySet() ) {

			if( i >= listJobExecutionInfo.size() ) {
				break;
			}

			final JobExecutionInfo jobExecutionInfo = listJobExecutionInfo.get( i );

			entry.setValue( jobExecutionInfo.getExecutionId() );
			final SimpleDateFormat formatter = new SimpleDateFormat( dateFormat );
			final String longExecution = jobExecutionInfo.isLongExecution() ? LONG_EXECUTION_MARKER : ""; //$NON-NLS-1$
			entry.getKey().setLabel( formatter.format( jobExecutionInfo.getStartedAt() ) + ": " +jobExecutionInfo.getDescription() + longExecution ); //$NON-NLS-1$
			i++;
		}
	}

	/**
	 * Update the image of the tray icon
	 */
	public void updateTrayIcon() {

		if( rundeckMonitorState.isDisconnected() ) {

			trayIcon.setImage( IMAGE_DISCONNECTED );
		}
		else if( rundeckMonitorState.isFailedJobs() ) {

			if(  rundeckMonitorState.isLateJobs() ) {
				trayIcon.setImage( IMAGE_KO_LATE );
			}
			else {
				trayIcon.setImage( IMAGE_KO );
			}
		}
		else if( rundeckMonitorState.isLateJobs() ) {
			trayIcon.setImage( IMAGE_LATE );
		}
		else {
			trayIcon.setImage( IMAGE_OK );
		}
	}
}
