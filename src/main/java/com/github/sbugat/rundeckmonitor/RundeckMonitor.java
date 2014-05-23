package org.rundeck.monitor;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.rundeck.api.RundeckMonitorClient;
import org.rundeck.api.domain.RundeckEvent;
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

	/** Task bar tray icon*/
	private final TrayIcon trayIcon;
	/** System tray */
	private final SystemTray tray = SystemTray.getSystemTray();
	/** System default browser to open execution details*/
	private final Desktop desktop = Desktop.getDesktop();

	boolean monitorOK = true;

	/** OK image*/
	private final Image imageOK = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "OK.png" ) ); //$NON-NLS-1$
	/** KO image when a jos has terminted with failed status*/
	private final Image imageKO = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO.png" ) ); //$NON-NLS-1$
	/** Disconnected from rundeck image */
	private final Image imageDisconnect = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "DISCONNECTED.png" ) ); //$NON-NLS-1$

	private final RundeckMonitorClient rundeckClient;

	private Map<Long, RundeckEvent> failedMap = new LinkedHashMap<Long, RundeckEvent>();

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
						catch ( final URISyntaxException exception) {
							exception.printStackTrace();
						}
						catch ( final IOException exception ) {
							exception.printStackTrace();
						}
					}
				}
			};
			//Alert reset
			final ActionListener reinitListener = new ActionListener() {
				@SuppressWarnings("synthetic-access")
				public void actionPerformed( final ActionEvent e) {
					trayIcon.setImage( imageOK );
					monitorOK = true;

					for( final MenuItem menuItem : failedMenuItems.keySet() ){
						menuItem.setFont(  Font.decode( null ) );
					}
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
					catch ( final URISyntaxException exception) {
						exception.printStackTrace();
					}
					catch ( final IOException exception ) {
						exception.printStackTrace();
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


			//Init the menu with last failed jobs
			final RundeckHistory histo = rundeckClient.getHistory( rundeckProject, RUNDECK_FAILED_JOB, Long.valueOf( failedJobNumber ), Long.valueOf(0) );
			if( histo.getEvents().size() > 0 ) {

				updateRundeckHistory( histo.getEvents(), false );
			}

			//Add the icon  to the system tray
			trayIcon = new TrayIcon( imageOK, rundeckMonitorName, popupMenu ); //$NON-NLS-1$
			trayIcon.setImageAutoSize( true );

			try {
				tray.add(trayIcon);
			} catch ( final AWTException e ) {

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

	public void run() {

		while( true ){
			try {

				final RundeckHistory histo = rundeckClient.getHistory( rundeckProject, RUNDECK_FAILED_JOB, Long.valueOf( failedJobNumber ), Long.valueOf(0) );

				if( histo.getEvents().size() > 0 ) {

					final RundeckEvent rundeckEvent = histo.getEvents().get(0);
					if( ! failedMap.containsKey( rundeckEvent.getExecutionId() ) ){

						updateRundeckHistory( histo.getEvents(), true );
						monitorOK = false;
						trayIcon.setImage( imageKO );
						final SimpleDateFormat formatter = new SimpleDateFormat( dateFormat );
						trayIcon.displayMessage( "New failed job", formatter.format( rundeckEvent.getStartedAt() ) + ": " + rundeckEvent.getTitle(), TrayIcon.MessageType.ERROR ); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else if( monitorOK ){
						trayIcon.setImage( imageOK );
					}
					else {
						trayIcon.setImage( imageKO );
					}
				}

				try {
					Thread.sleep( refreshDelay * 1000 );
				}
				catch ( final Exception e ) {

					e.printStackTrace();
				}
			}
			catch ( final Exception e ) {

				trayIcon.setImage( imageDisconnect );
				try {
					Thread.sleep( 5000 );
				}
				catch (InterruptedException e1) {
					tray.remove( trayIcon );

					final StringWriter stringWriter = new StringWriter();
					e.printStackTrace( new PrintWriter( stringWriter ) );
					JOptionPane.showMessageDialog( null, e1.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor running error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$

					System.exit( 1 );
				}
			}
		}
	}

	private void updateRundeckHistory( final List<RundeckEvent> rundeckEvents, final boolean newJobFailed ){

		failedMap.clear();

		final MenuItem[] menuItems = new MenuItem[ failedJobNumber ];
		failedMenuItems.keySet().toArray( menuItems );
		failedMenuItems.clear();

		int i=0;

		for( final RundeckEvent rundeckEvent : rundeckEvents ) {

			failedMap.put( rundeckEvent.getExecutionId(), rundeckEvent );

			final SimpleDateFormat formatter = new SimpleDateFormat( dateFormat );
			menuItems [ i ].setLabel( formatter.format( rundeckEvent.getStartedAt() ) + ": " + rundeckEvent.getTitle() ); //$NON-NLS-1$
			failedMenuItems.put( menuItems [ i ], rundeckEvent.getExecutionId() );

			i++;
		}

		if( newJobFailed ){
			menuItems [ 0 ].setFont( Font.decode( null ).deriveFont( Font.BOLD ) );
		}
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