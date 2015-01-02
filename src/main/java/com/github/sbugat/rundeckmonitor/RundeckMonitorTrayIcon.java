package com.github.sbugat.rundeckmonitor;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.tools.SystemTools;
import com.github.sbugat.rundeckmonitor.wizard.JobTabRedirection;
import com.github.sbugat.rundeckmonitor.wizard.RundeckMonitorConfigurationWizard;

/**
 * Tray icon common class with no-AWT/Swing components.
 * 
 * @author Sylvain Bugat
 * 
 */
public abstract class RundeckMonitorTrayIcon {

	/** SLF4J XLogger. */
	private static final XLogger LOG = XLoggerFactory.getXLogger(RundeckMonitor.class);

	/** URL to access job execution details. */
	static final String RUNDECK_JOB_EXECUTION_URL = "/execution/"; //$NON-NLS-1$

	/** GitHub Project URL. */
	static final String RUNDECK_MONITOR_PROJECT_URL = "https://sylvain-bugat.github.com/RundeckMonitor"; //$NON-NLS-1$

	/** Marker on the job when it is too long. */
	static final String LONG_EXECUTION_MARKER = " - LONG EXECUTION"; //$NON-NLS-1$

	/** Alert message when a new failed job is detected. */
	static final String NEW_FAILED_JOB_ALERT = "New failed job"; //$NON-NLS-1$

	/** Alert message when a new long execution is detected. */
	static final String NEW_LONG_EXECUTION_ALERT = "New long execution"; //$NON-NLS-1$

	/** OK image. */
	static final Image IMAGE_OK = Toolkit.getDefaultToolkit().getImage(RundeckMonitorTrayIcon.class.getClassLoader().getResource("OK.png")); //$NON-NLS-1$
	/** WARNING image when a job seems to be blocked. */
	private static final Image IMAGE_LATE = Toolkit.getDefaultToolkit().getImage(RundeckMonitorTrayIcon.class.getClassLoader().getResource("LATE.png")); //$NON-NLS-1$
	static final Icon ICON_LATE_SMALL = new ImageIcon(Toolkit.getDefaultToolkit().getImage(RundeckMonitorTrayIcon.class.getClassLoader().getResource("LATE_SMALL.png"))); //$NON-NLS-1$
	/** KO image when a job has failed. */
	private static final Image IMAGE_KO = Toolkit.getDefaultToolkit().getImage(RundeckMonitorTrayIcon.class.getClassLoader().getResource("KO.png")); //$NON-NLS-1$
	static final Icon ICON_KO_SMALL = new ImageIcon(Toolkit.getDefaultToolkit().getImage(RundeckMonitorTrayIcon.class.getClassLoader().getResource("KO_SMALL.png"))); //$NON-NLS-1$
	/** KO image when a job has failed and a job seems to be blocked. */
	private static final Image IMAGE_KO_LATE = Toolkit.getDefaultToolkit().getImage(RundeckMonitorTrayIcon.class.getClassLoader().getResource("KO_LATE.png")); //$NON-NLS-1$
	/** Disconnected from rundeck image. */
	private static final Image IMAGE_DISCONNECTED = Toolkit.getDefaultToolkit().getImage(RundeckMonitorTrayIcon.class.getClassLoader().getResource("DISCONNECTED.png")); //$NON-NLS-1$

	/** System tray. */
	final SystemTray tray;

	/** Task bar tray icon. */
	TrayIcon trayIcon;

	/** Desktop to get the default browser. */
	final Desktop desktop;

	/** Menu failed item listener. */
	ActionListener menuListener;

	/** Edit configuration listener. */
	final ActionListener configurationListener;

	/** About menu listener. */
	final ActionListener aboutListener;

	/** Exit menu listener. */
	final ActionListener exitListener;

	/** Dialog to auto-hade the popup menu. */
	JDialog hiddenDialog;

	/** Date format to use for printing the Job start date. */
	final RundeckMonitorConfiguration rundeckMonitorConfiguration;

	/** Current state of the trayIcon. */
	final RundeckMonitorState rundeckMonitorState;

	/** Already known late/long process. */
	final Set<Long> newLateProcess = new HashSet<>();

	/** Already known failed process. */
	final Set<Long> newFailedProcess = new HashSet<>();

	/**
	 * Initialize the tray icon for the rundeckMonitor if the OS is compatible with it.
	 * 
	 * @param rundeckMonitorConfigurationArg loaded configuration
	 * @param rundeckMonitorStateArg state of the rundeck monitor
	 */
	public RundeckMonitorTrayIcon(final RundeckMonitorConfiguration rundeckMonitorConfigurationArg, final RundeckMonitorState rundeckMonitorStateArg) {

		LOG.entry(rundeckMonitorConfigurationArg, rundeckMonitorStateArg);

		rundeckMonitorConfiguration = rundeckMonitorConfigurationArg;
		rundeckMonitorState = rundeckMonitorStateArg;

		if (SystemTray.isSupported()) {

			// Try to use the system Look&Feel
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {

				// If System Look&Feel is not supported, stay with the default one
				LOG.warn("Unsupported System Look&Feel", e); //$NON-NLS-1$
			}

			// Get the system default browser to open execution details
			desktop = Desktop.getDesktop();

			// Edit configuration listener
			configurationListener = new ActionListener() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void actionPerformed(final ActionEvent e) {
					new RundeckMonitorConfigurationWizard(new RundeckMonitorConfiguration(rundeckMonitorConfiguration), false);
				}
			};

			// Rundeck monitor about
			aboutListener = new ActionListener() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void actionPerformed(final ActionEvent e) {

					try {
						final URI executionURI = new URI(RUNDECK_MONITOR_PROJECT_URL);
						desktop.browse(executionURI);
					}
					catch (final URISyntaxException | IOException exception) {

						final StringWriter stringWriter = new StringWriter();
						exception.printStackTrace(new PrintWriter(stringWriter));
						JOptionPane.showMessageDialog(null, exception.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor redirection error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					}
				}
			};

			// Get the system tray
			tray = SystemTray.getSystemTray();

			// Rundeck monitor exit
			exitListener = new ActionListener() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void actionPerformed(final ActionEvent e) {
					tray.remove(trayIcon);
					SystemTools.exit(SystemTools.EXIT_CODE_OK);
				}
			};

			hiddenDialog = new JDialog();
			hiddenDialog.setSize(10, 10);

			hiddenDialog.addWindowFocusListener(new WindowFocusListener() {

				@Override
				public void windowLostFocus(final WindowEvent e) {
					hiddenDialog.setVisible(false);
				}

				@Override
				public void windowGainedFocus(final WindowEvent e) {
					// Nothing to do
				}
			});
		}
		else {
			// if the System is not compatible with SystemTray
			tray = null;
			desktop = null;
			configurationListener = null;
			aboutListener = null;
			exitListener = null;

			JOptionPane.showMessageDialog(null, "SystemTray cannot be initialized", "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$

			SystemTools.exit(SystemTools.EXIT_CODE_TRAY_ICON_UNSUPPORTED);
		}
	}

	/**
	 * Update the list of failed/late jobs.
	 * 
	 * @param listJobExecutionInfo list of failed and late jobs informations
	 */
	public abstract void updateExecutionIdsList(final List<JobExecutionInfo> listJobExecutionInfo);

	/**
	 * Update the image of the tray icon.
	 */
	public final void updateTrayIcon() {

		LOG.entry();

		if (rundeckMonitorState.isDisconnected()) {
			trayIcon.setImage(IMAGE_DISCONNECTED);
		}
		else if (rundeckMonitorState.isFailedJobs()) {

			if (rundeckMonitorState.isLateJobs()) {
				trayIcon.setImage(IMAGE_KO_LATE);
			}
			else {
				trayIcon.setImage(IMAGE_KO);
			}
		}
		else if (rundeckMonitorState.isLateJobs()) {
			trayIcon.setImage(IMAGE_LATE);
		}
		else {
			trayIcon.setImage(IMAGE_OK);
		}

		LOG.exit();
	}

	/**
	 * Called when configuration is reloaded, clear all known process and reinitialize the tooltip.
	 */
	public void reloadConfiguration() {

		LOG.entry();

		newLateProcess.clear();
		newFailedProcess.clear();

		trayIcon.setToolTip(rundeckMonitorConfiguration.getRundeckMonitorName());

		LOG.exit();
	}

	/**
	 * remove the RundeckMonitor icon from the system tray.
	 */
	public final void disposeTrayIcon() {

		LOG.entry();
		tray.remove(trayIcon);
		LOG.exit();
	}

	/**
	 * Open a browser page using the default browser to a job execution.
	 * 
	 * @param jobExecutionInfo the job exeuction to open
	 */
	final void openBrowser(final JobExecutionInfo jobExecutionInfo) {

		LOG.entry();
		final JobTabRedirection jobTabRedirection;

		if (jobExecutionInfo.isLongExecution()) {
			jobTabRedirection = JobTabRedirection.SUMMARY;
		}
		else {
			jobTabRedirection = JobTabRedirection.valueOf(rundeckMonitorConfiguration.getJobTabRedirection());
		}

		try {
			final String uRI = rundeckMonitorConfiguration.getRundeckUrl() + RUNDECK_JOB_EXECUTION_URL + jobTabRedirection.getAccessUrlPrefix() + '/' + jobExecutionInfo.getExecutionId() + jobTabRedirection.getAccessUrlSuffix();
			LOG.info("Open execution with URL: {}", uRI); //$NON-NLS-1$
			final URI executionURI = new URI(uRI);
			desktop.browse(executionURI);
		}
		catch (final URISyntaxException | IOException exception) {

			final StringWriter stringWriter = new StringWriter();
			exception.printStackTrace(new PrintWriter(stringWriter));
			JOptionPane.showMessageDialog(null, exception.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor redirection error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
		}

		LOG.exit();
	}
}
