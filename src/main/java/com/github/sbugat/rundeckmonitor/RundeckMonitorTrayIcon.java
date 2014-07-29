package com.github.sbugat.rundeckmonitor;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
	protected static final String RUNDECK_MONITOR_PROJECT_URL = "https://github.com/Sylvain-Bugat/RundeckMonitor"; //$NON-NLS-1$

	/** Marker on the job when it is too long*/
	private static final String LONG_EXECUTION_MARKER = " - LONG EXECUTION"; //$NON-NLS-1$

	/** Alert message when a new failed job is detected*/
	private static final String NEW_FAILED_JOB_ALERT = "New failed job"; //$NON-NLS-1$

	/** Alert message when a new long execution is detected*/
	private static final String NEW_LONG_EXECUTION_ALERT = "New long execution"; //$NON-NLS-1$

	/** OK image*/
	private final Image IMAGE_OK = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "OK.png" ) ); //$NON-NLS-1$
	/** WARNING image when a job seems to be blocked*/
	private final Image IMAGE_LATE = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "LATE.png" ) ); //$NON-NLS-1$
	private final Icon ICON_LATE_SMALL = new ImageIcon( Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "LATE_SMALL.png" ) ) ); //$NON-NLS-1$
	/** KO image when a job has failed*/
	private final Image IMAGE_KO = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO.png" ) ); //$NON-NLS-1$
	private final Icon ICON_KO_SMALL = new ImageIcon( Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO_SMALL.png" ) ) ); //$NON-NLS-1$
	/** KO image when a job has failed and a job seems to be blocked*/
	private final Image IMAGE_KO_LATE = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "KO_LATE.png" ) ); //$NON-NLS-1$
	/** Disconnected from rundeck image */
	private final Image IMAGE_DISCONNECTED = Toolkit.getDefaultToolkit().getImage( getClass().getClassLoader().getResource( "DISCONNECTED.png" ) ); //$NON-NLS-1$

	/** System tray */
	private final SystemTray tray;

	/** Task bar tray icon*/
	private final TrayIcon trayIcon;

	/**Date format to use for printing the Job start date*/
	private final RundeckMonitorConfiguration rundeckMonitorConfiguration;

	/**Current state of the trayIcon */
	private RundeckMonitorState rundeckMonitorState;

	/**MenuItem for lasts late/failed jobs*/
	private final Map<JMenuItem, Long> failedMenuItems = new LinkedHashMap<>();

	private final Set<Long> newLateProcess = new HashSet<>();

	private final Set<Long> newFailedProcess = new HashSet<>();

	/**
	 * Initialize the tray icon for the rundeckMonitor if the OS is compatible with it
	 *
	 * @param rundeckMonitorConfiguration loaded configuration
	 * @param rundeckMonitorStateArg state of the rundeck monitor
	 */
	public RundeckMonitorTrayIcon( final RundeckMonitorConfiguration rundeckMonitorConfigurationArg, final RundeckMonitorState rundeckMonitorStateArg ) {

		rundeckMonitorConfiguration = rundeckMonitorConfigurationArg;
		rundeckMonitorState = rundeckMonitorStateArg;

		if( SystemTray.isSupported() ) {

			//Try to use the system Look&Feel
			try {
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
			catch( final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e ) {

				//If System Look&Feel is not supported, stay with the default one
			}

			// Get the system default browser to open execution details
			final Desktop desktop = Desktop.getDesktop();

			//Action listener to get job execution detail on the rundeck URL
			final ActionListener menuListener = new ActionListener() {
				@SuppressWarnings("synthetic-access")
				public void actionPerformed( final ActionEvent e) {

					if( JMenuItem.class.isInstance( e.getSource() ) ){

						final Long executionId = failedMenuItems.get( e.getSource() );

						try {
							final URI executionURI = new URI( rundeckMonitorConfiguration.getRundeckUrl() + RUNDECK_JOB_EXECUTION_URL + executionId );
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

					//Reset all failed icon
					for( final Entry<JMenuItem,Long> entry: failedMenuItems.entrySet() ) {

						final JMenuItem menuItem = entry.getKey();
						menuItem.setIcon( null );
						menuItem.setFont( menuItem.getFont().deriveFont( Font.PLAIN ) );
					}

					//Clear all new failed jobs
					newLateProcess.clear();
					newFailedProcess.clear();

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

			//Get the system tray
			tray = SystemTray.getSystemTray();

			//Rundeck monitor exit
			final ActionListener exitListener = new ActionListener() {
				@SuppressWarnings("synthetic-access")
				public void actionPerformed( final ActionEvent e) {
					tray.remove( trayIcon );
					System.exit( 0 );
				}
			};

			//Popup menu
			//SystemLookAndFeel
			JPopupMenu.setDefaultLightWeightPopupEnabled( true );
			final JPopupMenu popupMenu = new JPopupMenu();

			for( int i = 0 ; i < rundeckMonitorConfiguration.getFailedJobNumber() ; i++ ){

				final JMenuItem failedItem = new JMenuItem();
				failedMenuItems.put( failedItem, null );
				popupMenu.add( failedItem );
				failedItem.addActionListener( menuListener );
			}

			popupMenu.addSeparator();
			final JMenuItem reinitItem = new JMenuItem( "Reset alert" ); //$NON-NLS-1$
			popupMenu.add( reinitItem );
			reinitItem.addActionListener( reinitListener );
			final JMenuItem aboutItem = new JMenuItem( "About RundeckMonitor" ); //$NON-NLS-1$
			popupMenu.add( aboutItem );
			aboutItem.addActionListener( aboutListener );
			final JMenuItem exitItem = new JMenuItem( "Quit" ); //$NON-NLS-1$
			popupMenu.add( exitItem );
			exitItem.addActionListener( exitListener );

			//Add the icon  to the system tray
			trayIcon = new TrayIcon( IMAGE_OK, rundeckMonitorConfiguration.getRundeckMonitorName() ); //$NON-NLS-1$
			trayIcon.setImageAutoSize( true );

			trayIcon.addMouseListener( new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					if( e.isPopupTrigger() ) {
						popupMenu.setLocation( e.getX(), e.getY() );
						popupMenu.setInvoker( popupMenu );
						popupMenu.setVisible( true );
					}
				}
			});

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
			tray = null;
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

		for( final Entry<JMenuItem,Long> entry: failedMenuItems.entrySet() ) {

			if( i >= listJobExecutionInfo.size() ) {
				break;
			}

			final JobExecutionInfo jobExecutionInfo = listJobExecutionInfo.get( i );
			final JMenuItem jMenuItem = entry.getKey();

			entry.setValue( jobExecutionInfo.getExecutionId() );
			final SimpleDateFormat formatter = new SimpleDateFormat( rundeckMonitorConfiguration.getDateFormat() );
			final String longExecution = jobExecutionInfo.isLongExecution() ? LONG_EXECUTION_MARKER : ""; //$NON-NLS-1$
			final String message = formatter.format( jobExecutionInfo.getStartedAt() ) + ": " +jobExecutionInfo.getDescription();
			jMenuItem.setText( message + longExecution );

			if( jobExecutionInfo.isNewJob() ) {

				if( jobExecutionInfo.isLongExecution() ) {
					trayIcon.displayMessage( NEW_LONG_EXECUTION_ALERT, message, TrayIcon.MessageType.WARNING );
					newLateProcess.add( jobExecutionInfo.getExecutionId() );
				}
				else {
					trayIcon.displayMessage( NEW_FAILED_JOB_ALERT, message, TrayIcon.MessageType.ERROR );
					newFailedProcess.add( jobExecutionInfo.getExecutionId() );
				}
			}

			//Mark failed and late jobs with an icon and bold menuitem
			if( newFailedProcess.contains( jobExecutionInfo.getExecutionId() ) ) {
				jMenuItem.setFont( entry.getKey().getFont().deriveFont( Font.BOLD ) );
				jMenuItem.setIcon( ICON_KO_SMALL );
			}
			else if( newLateProcess.contains( jobExecutionInfo.getExecutionId() ) ) {
				jMenuItem.setFont( entry.getKey().getFont().deriveFont( Font.BOLD ) );
				jMenuItem.setIcon( ICON_LATE_SMALL );
			}
			else {
				jMenuItem.setFont( entry.getKey().getFont().deriveFont( Font.PLAIN ) );
				jMenuItem.setIcon( null );
			}

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

			if( rundeckMonitorState.isLateJobs() ) {
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

	/**
	 * remove the RundeckMonitor icon from the system tray
	 */
	public void disposeTrayIcon() {

		tray.remove( trayIcon );
	}
}
