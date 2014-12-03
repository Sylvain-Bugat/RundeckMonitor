package com.github.sbugat.rundeckmonitor;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
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

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.wizard.JobTabRedirection;

/**
 * Swing tray icon management class
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitorSwingTrayIcon extends RundeckMonitorTrayIcon{

	/** Tray Icon menu*/
	private final JPopupMenu popupMenu;

	/**MenuItem for lasts late/failed jobs*/
	private final Map<JMenuItem, JobExecutionInfo> failedMenuItems = new LinkedHashMap<>();

	/**
	 * Initialize the tray icon for the rundeckMonitor if the OS is compatible with it
	 *
	 * @param rundeckMonitorConfigurationArg loaded configuration
	 * @param rundeckMonitorStateArg state of the rundeck monitor
	 */
	public RundeckMonitorSwingTrayIcon( final RundeckMonitorConfiguration rundeckMonitorConfigurationArg, final RundeckMonitorState rundeckMonitorStateArg ) {

		super( rundeckMonitorConfigurationArg, rundeckMonitorStateArg );

		//Action listener to get job execution detail on the rundeck URL
		menuListener = new ActionListener() {
			@SuppressWarnings("synthetic-access")
			public void actionPerformed( final ActionEvent e) {

				if( JMenuItem.class.isInstance( e.getSource() ) ){

					final JobExecutionInfo jobExecutionInfo = failedMenuItems.get( e.getSource() );
					final JobTabRedirection jobTabRedirection;

					if( jobExecutionInfo.isLongExecution() ) {
						jobTabRedirection = JobTabRedirection.SUMMARY;
					}
					else {
						jobTabRedirection = JobTabRedirection.valueOf( rundeckMonitorConfiguration.getJobTabRedirection() );
					}

					try {
						final URI executionURI = new URI( rundeckMonitorConfiguration.getRundeckUrl() + RUNDECK_JOB_EXECUTION_URL + jobTabRedirection.getAccessUrlPrefix() + '/' + jobExecutionInfo.getExecutionId() + jobTabRedirection.getAccessUrlSuffix() );
						desktop.browse( executionURI );
					}
					catch ( final URISyntaxException | IOException exception) {

						final StringWriter stringWriter = new StringWriter();
						exception.printStackTrace( new PrintWriter( stringWriter ) );
						JOptionPane.showMessageDialog( null, exception.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor redirection error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
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
				for( final Entry<JMenuItem,JobExecutionInfo> entry: failedMenuItems.entrySet() ) {

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

		//Popup menu
		//SystemLookAndFeel
		JPopupMenu.setDefaultLightWeightPopupEnabled( true );
		popupMenu = new JPopupMenu();

		for( int i = 0 ; i < rundeckMonitorConfiguration.getFailedJobNumber() ; i++ ){

			final JMenuItem failedItem = new JMenuItem();
			failedMenuItems.put( failedItem, null );
			popupMenu.add( failedItem );
			failedItem.addActionListener( menuListener );
		}

		popupMenu.addSeparator();

		final JMenuItem reinitItem = new JMenuItem( "Reset alert" ); //$NON-NLS-1$
		popupMenu.add( reinitItem );

		popupMenu.addSeparator();

		reinitItem.addActionListener( reinitListener );
		final JMenuItem aboutItem = new JMenuItem( "About RundeckMonitor" ); //$NON-NLS-1$
		popupMenu.add( aboutItem );

		aboutItem.addActionListener( aboutListener );
		final JMenuItem configurationItem = new JMenuItem( "Edit configuration" ); //$NON-NLS-1$
		popupMenu.add( configurationItem );
		configurationItem.addActionListener( configurationListener );

		popupMenu.addSeparator();

		final JMenuItem exitItem = new JMenuItem( "Quit" ); //$NON-NLS-1$
		popupMenu.add( exitItem );
		exitItem.addActionListener( exitListener );

		//Add the icon  to the system tray
		trayIcon = new TrayIcon( IMAGE_OK, rundeckMonitorConfiguration.getRundeckMonitorName() );
		trayIcon.setImageAutoSize( true );

		trayIcon.addMouseListener( new MouseAdapter() {

			public void mouseReleased( final MouseEvent e) {

				if( e.isPopupTrigger() ) {
					popupMenu.setLocation( e.getX(), e.getY() );
					hiddenDialog.setLocation( e.getX(), e.getY() );

					popupMenu.setInvoker( hiddenDialog );
					hiddenDialog.setVisible( true );
					popupMenu.setVisible( true );
				}
			}
		});


		hiddenDialog.addWindowFocusListener(new WindowFocusListener () {

			@Override
			public void windowLostFocus ( final WindowEvent e ) {
				hiddenDialog.setVisible( false );
			}

			@Override
			public void windowGainedFocus ( final WindowEvent e ) {
				//Nothing to do
			}
		});

		trayIcon.addMouseListener( new MouseAdapter() {

			public void mouseReleased( final MouseEvent e) {

				if( e.isPopupTrigger() ) {
					popupMenu.setLocation( e.getX(), e.getY() );
					hiddenDialog.setLocation( e.getX(), e.getY() );

					popupMenu.setInvoker( hiddenDialog );
					hiddenDialog.setVisible( true );
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
			JOptionPane.showMessageDialog( null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$

			System.exit( 3 );
		}
	}

	/**
	 * Update the list of failed/late jobs
	 *
	 * @param listJobExecutionInfo list of failed and late jobs informations
	 */
	public void updateExecutionIdsList( final List<JobExecutionInfo> listJobExecutionInfo ) {

		int i=0;

		for( final Entry<JMenuItem,JobExecutionInfo> entry: failedMenuItems.entrySet() ) {

			if( i >= listJobExecutionInfo.size() ) {
				break;
			}

			final JobExecutionInfo jobExecutionInfo = listJobExecutionInfo.get( i );
			final JMenuItem jMenuItem = entry.getKey();

			entry.setValue( jobExecutionInfo );
			final SimpleDateFormat formatter = new SimpleDateFormat( rundeckMonitorConfiguration.getDateFormat() );
			final String longExecution = jobExecutionInfo.isLongExecution() ? LONG_EXECUTION_MARKER : ""; //$NON-NLS-1$
			final String message = formatter.format( jobExecutionInfo.getStartedAt() ) + ": " +jobExecutionInfo.getDescription(); //$NON-NLS-1$
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

	public void reloadConfiguration() {

		//Remove all old failedMenuItems from the popup menu
		for( final JMenuItem failedItem : failedMenuItems.keySet() ) {
			popupMenu.remove( failedItem );
			failedItem.removeActionListener( menuListener );
		}

		failedMenuItems.clear();

		//Add all new menu items to the popup menu
		for( int i = 0 ; i < rundeckMonitorConfiguration.getFailedJobNumber() ; i++ ){

			final JMenuItem failedItem = new JMenuItem();
			failedMenuItems.put( failedItem, null );
			popupMenu.insert( failedItem, i );
			failedItem.addActionListener( menuListener );
		}

		super.reloadConfiguration();
	}
}
