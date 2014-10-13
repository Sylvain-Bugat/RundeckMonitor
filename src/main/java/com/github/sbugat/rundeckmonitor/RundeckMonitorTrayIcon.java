package com.github.sbugat.rundeckmonitor;

import java.util.List;

public interface RundeckMonitorTrayIcon {

	/**
	 * Update the list of failed/late jobs
	 *
	 * @param listJobExecutionInfo list of failed and late jobs informations
	 */
	public void updateExecutionIdsList( final List<JobExecutionInfo> listJobExecutionInfo );

	/**
	 * Update the image of the tray icon
	 */
	public void updateTrayIcon();

	public void reloadConfiguration();

	/**
	 * remove the RundeckMonitor icon from the system tray
	 */
	public void disposeTrayIcon();
}
