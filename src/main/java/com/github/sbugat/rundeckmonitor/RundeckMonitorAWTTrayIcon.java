package com.github.sbugat.rundeckmonitor;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.tools.SystemTools;

/**
 * Tray icon management class.
 *
 * @author Sylvain Bugat
 *
 */
public final class RundeckMonitorAWTTrayIcon extends RundeckMonitorTrayIcon {

	/** Size of the hidden dialog box for auto-hide popup-menu when unfocus tray icon. */
	private static final int HIDDEN_DIALOG_SIZE = 10;

	/** Tray Icon menu. */
	private final PopupMenu popupMenu;

	/** MenuItem for lasts late/failed jobs. */
	private final Map<MenuItem, JobExecutionInfo> failedMenuItems = new LinkedHashMap<>();

	/**
	 * Initialize the tray icon for the rundeckMonitor if the OS is compatible with it.
	 *
	 * @param rundeckMonitorConfigurationArg loaded configuration
	 * @param rundeckMonitorStateArg state of the rundeck monitor
	 */
	public RundeckMonitorAWTTrayIcon(final RundeckMonitorConfiguration rundeckMonitorConfigurationArg, final RundeckMonitorState rundeckMonitorStateArg) {

		super(rundeckMonitorConfigurationArg, rundeckMonitorStateArg);

		// Action listener to get job execution detail on the rundeck URL
		menuListener = new ActionListener() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {

				if (MenuItem.class.isInstance(e.getSource())) {

					openBrowser(failedMenuItems.get(e.getSource()));
				}
			}
		};

		// Alert reset of the failed jobs state
		final ActionListener reinitListener = new ActionListener() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				rundeckMonitorState.setFailedJobs(false);

				// Reset all failed icon
				for (final Entry<MenuItem, JobExecutionInfo> entry : failedMenuItems.entrySet()) {

					final Font menuItemFont = entry.getKey().getFont();
					if (null != menuItemFont) {
						entry.getKey().setFont(menuItemFont.deriveFont(Font.PLAIN));
					}
				}

				// Clear all new failed jobs
				newLateProcess.clear();
				newFailedProcess.clear();

				updateTrayIcon();
			}
		};

		// Popup menu
		popupMenu = new PopupMenu();

		for (int i = 0; i < rundeckMonitorConfiguration.getFailedJobNumber(); i++) {

			final MenuItem failedItem = new MenuItem();
			failedMenuItems.put(failedItem, null);
			popupMenu.add(failedItem);
			failedItem.addActionListener(menuListener);
		}

		popupMenu.addSeparator();

		final MenuItem reinitItem = new MenuItem("Reset alert"); //$NON-NLS-1$
		popupMenu.add(reinitItem);

		popupMenu.addSeparator();

		reinitItem.addActionListener(reinitListener);
		final MenuItem aboutItem = new MenuItem("About RundeckMonitor"); //$NON-NLS-1$
		popupMenu.add(aboutItem);

		aboutItem.addActionListener(aboutListener);
		final MenuItem configurationItem = new MenuItem("Edit configuration"); //$NON-NLS-1$
		popupMenu.add(configurationItem);
		configurationItem.addActionListener(configurationListener);

		popupMenu.addSeparator();

		final MenuItem exitItem = new MenuItem("Quit"); //$NON-NLS-1$
		popupMenu.add(exitItem);
		exitItem.addActionListener(exitListener);

		final JDialog hiddenDialog = new JDialog();
		hiddenDialog.setSize(HIDDEN_DIALOG_SIZE, HIDDEN_DIALOG_SIZE);

		final RundeckMonitorAWTListenerAdapter listenerAdapter = new RundeckMonitorAWTListenerAdapter(hiddenDialog);
		hiddenDialog.addWindowFocusListener(listenerAdapter);

		// Add the icon to the system tray
		trayIcon = new TrayIcon(IMAGE_OK, rundeckMonitorConfiguration.getRundeckMonitorName(), popupMenu);
		trayIcon.setImageAutoSize(true);

		trayIcon.addMouseListener(listenerAdapter);

		try {
			tray.add(trayIcon);
		}
		catch (final AWTException e) {

			final StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			JOptionPane.showMessageDialog(null, e.getMessage() + System.lineSeparator() + stringWriter.toString(), "RundeckMonitor initialization error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

			SystemTools.exit(SystemTools.EXIT_CODE_TRAY_ICON_ERROR);
		}
	}

	/**
	 * Update the list of failed/late jobs.
	 *
	 * @param listJobExecutionInfo list of failed and late jobs informations
	 */
	@Override
	public void updateExecutionIdsList(final List<JobExecutionInfo> listJobExecutionInfo) {

		int i = 0;

		for (final Entry<MenuItem, JobExecutionInfo> entry : failedMenuItems.entrySet()) {

			if (i >= listJobExecutionInfo.size()) {
				break;
			}

			final JobExecutionInfo jobExecutionInfo = listJobExecutionInfo.get(i);
			final MenuItem menuItem = entry.getKey();

			entry.setValue(jobExecutionInfo);
			final SimpleDateFormat formatter = new SimpleDateFormat(rundeckMonitorConfiguration.getDateFormat());
			final String longExecution;
			if (jobExecutionInfo.isLongExecution()) {
				longExecution = LONG_EXECUTION_MARKER;
			}
			else {
				longExecution = StringUtils.EMPTY;
			}
			final String message = formatter.format(jobExecutionInfo.getStartedAt()) + ": " + jobExecutionInfo.getDescription(); //$NON-NLS-1$
			menuItem.setLabel(message + longExecution);

			if (jobExecutionInfo.isNewJob()) {

				if (jobExecutionInfo.isLongExecution()) {
					trayIcon.displayMessage(NEW_LONG_EXECUTION_ALERT, message, TrayIcon.MessageType.WARNING);
					newLateProcess.add(jobExecutionInfo.getExecutionId());
				}
				else {
					trayIcon.displayMessage(NEW_FAILED_JOB_ALERT, message, TrayIcon.MessageType.ERROR);
					newFailedProcess.add(jobExecutionInfo.getExecutionId());
				}
			}

			// Check if the font of the menuItem exists
			final Font menuItemFont = entry.getKey().getFont();
			if (null != menuItemFont) {
				// Mark failed and late jobs with an icon and bold menuitem
				if (newFailedProcess.contains(jobExecutionInfo.getExecutionId())) {
					menuItem.setFont(menuItemFont.deriveFont(Font.BOLD));
				}
				else if (newLateProcess.contains(jobExecutionInfo.getExecutionId())) {
					menuItem.setFont(menuItemFont.deriveFont(Font.BOLD));
				}
				else {
					menuItem.setFont(menuItemFont.deriveFont(Font.PLAIN));
				}
			}

			i++;
		}
	}

	/**
	 * Called when configuration is reloaded, clear all menu items and reinitialize action listeners.
	 */
	@Override
	public void reloadConfiguration() {

		// Remove all old failedMenuItems from the popup menu
		for (final MenuItem failedItem : failedMenuItems.keySet()) {
			popupMenu.remove(failedItem);
			failedItem.removeActionListener(menuListener);
		}

		failedMenuItems.clear();

		// Add all new menu items to the popup menu
		for (int i = 0; i < rundeckMonitorConfiguration.getFailedJobNumber(); i++) {

			final MenuItem failedItem = new MenuItem();
			failedMenuItems.put(failedItem, null);
			popupMenu.insert(failedItem, i);
			failedItem.addActionListener(menuListener);
		}

		super.reloadConfiguration();
	}

	/**
	 * Mouse Adapter and WindowsFocusListener to auto-hide the hidden dialog.
	 *
	 * @author Sylvain Bugat
	 *
	 */
	private static final class RundeckMonitorAWTListenerAdapter extends MouseAdapter implements WindowFocusListener {

		/** Diaglo linked to this custom listener/adapter. */
		private final JDialog dialog;

		/**
		 * Copy dialog reference into the listener.
		 *
		 * @param dialogArg dialog to copy
		 */
		private RundeckMonitorAWTListenerAdapter(final JDialog dialogArg) {
			dialog = dialogArg;
		}

		@Override
		public void windowLostFocus(final WindowEvent e) {
			dialog.setVisible(false);
		}

		@Override
		public void windowGainedFocus(final WindowEvent e) {
			// Nothing to do
		}

		@Override
		public void mouseReleased(final MouseEvent e) {

			if (e.isPopupTrigger()) {
				dialog.setLocation(e.getX(), e.getY());

				dialog.setVisible(true);
			}
		}
	}
}
