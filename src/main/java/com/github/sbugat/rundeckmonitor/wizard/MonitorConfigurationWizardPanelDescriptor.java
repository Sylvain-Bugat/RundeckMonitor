package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.rundeck.api.RundeckClient;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.tools.EnvironmentTools;
import com.github.sbugat.rundeckmonitor.tools.RundeckClientTools;

/**
 * RunDeck monitor wizard panel.
 * 
 * @author Sylvain Bugat
 * 
 */
public final class MonitorConfigurationWizardPanelDescriptor extends WizardPanelDescriptor {

	/** Main container. */
	private final Container container = new Container();

	/** RunDeck monitor name input. */
	private final JTextField rundeckMonitorName = new JTextField(20);
	/** Refresh delay input. */
	private final JComboBox<RefreshDelay> rundeckMonitorRefreshDelay = new JComboBox<>();
	/** Late threshold input. */
	private final JComboBox<LateExecutionThreshold> rundeckMonitorLateExecutionThreshold = new JComboBox<>();
	/** Number of displayed failed jobs input. */
	private final JComboBox<FailedJobsNumber> rundeckMonitorFailedJobNumber = new JComboBox<>();
	/** Date format input. */
	private final JComboBox<DateFormat> rundeckMonitorDateFormat = new JComboBox<>();
	/** Open a job redirection input. */
	private final JComboBox<JobTabRedirection> rundeckMonitorJobTabRedirection = new JComboBox<>();
	/** Interface type input. */
	private final JComboBox<InterfaceType> rundeckMonitorInterfaceType = new JComboBox<>();

	/**
	 * Copy arguments and initialize the RunDeck monitor configuration wizard panel.
	 * 
	 * @param backArg previous panel
	 * @param nextArg next panel
	 * @param rundeckMonitorConfigurationArg RunDeck monitor common configuration
	 */
	public MonitorConfigurationWizardPanelDescriptor(final ConfigurationWizardStep backArg, final ConfigurationWizardStep nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg) {
		super(ConfigurationWizardStep.MONITOR_STEP, backArg, nextArg, rundeckMonitorConfigurationArg);

		container.setLayout(new GridBagLayout());
		final JLabel rundeckMonitorNameLabel = new JLabel("Tray-icon monitor name:"); //$NON-NLS-1$
		final JLabel rundeckMonitorRefreshDelayLabel = new JLabel("Failed/late jobs refresh delay:"); //$NON-NLS-1$
		final JLabel rundeckMonitorLateExecutionThresholdLabel = new JLabel("Late execution detection threshold:"); //$NON-NLS-1$
		final JLabel rundeckMonitorFailedJobNumberLabel = new JLabel("Number of failed/late jobs to display:"); //$NON-NLS-1$
		final JLabel rundeckMonitorDateFormatLabel = new JLabel("Failed/late jobs displayed date format:"); //$NON-NLS-1$
		final JLabel rundeckMonitorJobTabRedirectionLabel = new JLabel("Failed/late job tab redirection: "); //$NON-NLS-1$
		final JLabel rundeckMonitorInterfaceTypeLabel = new JLabel("Type of Java interface: "); //$NON-NLS-1$

		// Fields initialization
		if (null == rundeckMonitorConfigurationArg.getRundeckMonitorName() || rundeckMonitorConfigurationArg.getRundeckMonitorName().isEmpty()) {
			rundeckMonitorName.setText(RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE);
		}
		else {
			rundeckMonitorName.setText(rundeckMonitorConfigurationArg.getRundeckMonitorName());
		}

		RefreshDelay oldConfiguredRefreshDelay = null;
		for (final RefreshDelay refreshDelay : RefreshDelay.values()) {

			rundeckMonitorRefreshDelay.addItem(refreshDelay);
			if (refreshDelay.getDelay() == rundeckMonitorConfigurationArg.getRefreshDelay()) {
				oldConfiguredRefreshDelay = refreshDelay;
			}
		}

		if (null != oldConfiguredRefreshDelay) {
			rundeckMonitorRefreshDelay.setSelectedItem(oldConfiguredRefreshDelay);
		}
		else {
			rundeckMonitorRefreshDelay.setSelectedItem(RefreshDelay.REFRESH_DELAY_1M);
		}

		LateExecutionThreshold oldLateExecutionThreshold = null;
		for (final LateExecutionThreshold lateExecutionThreshold : LateExecutionThreshold.values()) {

			rundeckMonitorLateExecutionThreshold.addItem(lateExecutionThreshold);
			if (lateExecutionThreshold.getThreshold() == rundeckMonitorConfigurationArg.getLateThreshold()) {
				oldLateExecutionThreshold = lateExecutionThreshold;
			}
		}

		if (null != oldLateExecutionThreshold) {
			rundeckMonitorLateExecutionThreshold.setSelectedItem(oldLateExecutionThreshold);
		}
		else {
			rundeckMonitorLateExecutionThreshold.setSelectedItem(LateExecutionThreshold.LATE_EXECUTION_THRESHOLD_30M);
		}

		FailedJobsNumber oldFailedJobsNumber = null;
		for (final FailedJobsNumber failedJobsNumber : FailedJobsNumber.values()) {

			rundeckMonitorFailedJobNumber.addItem(failedJobsNumber);
			if (failedJobsNumber.getFailedJobsNumber() == rundeckMonitorConfigurationArg.getFailedJobNumber()) {
				oldFailedJobsNumber = failedJobsNumber;
			}
		}

		if (null != oldFailedJobsNumber) {
			rundeckMonitorFailedJobNumber.setSelectedItem(oldFailedJobsNumber);
		}
		else {
			rundeckMonitorFailedJobNumber.setSelectedItem(FailedJobsNumber.FAILED_JOBS_10);
		}

		DateFormat oldDateFormat = null;
		for (final DateFormat dateFormat : DateFormat.values()) {

			rundeckMonitorDateFormat.addItem(dateFormat);

			if (dateFormat.getDateFormat().equals(rundeckMonitorConfigurationArg.getDateFormat())) {
				oldDateFormat = dateFormat;
			}
		}

		if (null != oldDateFormat) {
			rundeckMonitorDateFormat.setSelectedItem(oldDateFormat);
		}
		else {
			rundeckMonitorDateFormat.setSelectedItem(DateFormat.DATE_FORMAT_STANDARD);
		}

		JobTabRedirection oldJobTabRedirection = null;
		for (final JobTabRedirection jobTabRedirection : JobTabRedirection.values()) {

			rundeckMonitorJobTabRedirection.addItem(jobTabRedirection);

			if (jobTabRedirection.name().equals(rundeckMonitorConfigurationArg.getJobTabRedirection())) {
				oldJobTabRedirection = jobTabRedirection;
			}
		}

		if (null != oldJobTabRedirection) {
			rundeckMonitorJobTabRedirection.setSelectedItem(oldJobTabRedirection);
		}
		else {
			rundeckMonitorJobTabRedirection.setSelectedItem(JobTabRedirection.SUMMARY);
		}

		InterfaceType oldInterfaceType = null;
		for (final InterfaceType interfaceType : InterfaceType.values()) {

			if (InterfaceType.SWING.equals(interfaceType)) {

				if (EnvironmentTools.isWindows()) {

					rundeckMonitorInterfaceType.addItem(interfaceType);

					if (interfaceType.name().equals(rundeckMonitorConfigurationArg.getInterfaceType())) {
						oldInterfaceType = interfaceType;
					}
				}
			}
			else {
				rundeckMonitorInterfaceType.addItem(interfaceType);

				if (interfaceType.name().equals(rundeckMonitorConfigurationArg.getInterfaceType())) {
					oldInterfaceType = interfaceType;
				}
			}
		}

		if (null != oldInterfaceType) {
			rundeckMonitorInterfaceType.setSelectedItem(oldInterfaceType);
		}
		else {
			rundeckMonitorInterfaceType.setSelectedItem(InterfaceType.SWING);
		}

		final GridBagConstraints gridBagConstraits = new GridBagConstraints();
		gridBagConstraits.insets = new Insets(2, 2, 2, 2);
		gridBagConstraits.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraits.gridwidth = 1;

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 0;
		container.add(rundeckMonitorNameLabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		container.add(rundeckMonitorName, gridBagConstraits);

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 1;
		container.add(rundeckMonitorRefreshDelayLabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		container.add(rundeckMonitorRefreshDelay, gridBagConstraits);

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 2;
		container.add(rundeckMonitorLateExecutionThresholdLabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		container.add(rundeckMonitorLateExecutionThreshold, gridBagConstraits);

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 3;
		container.add(rundeckMonitorFailedJobNumberLabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		container.add(rundeckMonitorFailedJobNumber, gridBagConstraits);

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 4;
		container.add(rundeckMonitorDateFormatLabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		container.add(rundeckMonitorDateFormat, gridBagConstraits);

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 5;
		container.add(rundeckMonitorJobTabRedirectionLabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		container.add(rundeckMonitorJobTabRedirection, gridBagConstraits);

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 6;
		container.add(rundeckMonitorInterfaceTypeLabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		container.add(rundeckMonitorInterfaceType, gridBagConstraits);
	}

	/**
	 * Return the main panel component.
	 * 
	 * @return main component
	 */
	@Override
	public Component getPanelComponent() {

		return container;
	}

	/**
	 * Refresh the panel with possible values.
	 */
	@Override
	public void aboutToDisplayPanel() {

		// Initialize the rundeck client with the minimal rundeck version (1)
		final RundeckClient rundeckClient = RundeckClientTools.buildMinimalRundeckClient(getRundeckMonitorConfiguration());

		final String rundeckVersion = rundeckClient.getSystemInfo().getVersion();

		JobTabRedirection oldJobTabRedirection = null;
		rundeckMonitorJobTabRedirection.removeAllItems();
		for (final JobTabRedirection jobTabRedirection : JobTabRedirection.values()) {

			if (rundeckVersion.compareTo(jobTabRedirection.getSinceRundeckVersion()) >= 0) {
				rundeckMonitorJobTabRedirection.addItem(jobTabRedirection);

				if (jobTabRedirection.name().equals(getRundeckMonitorConfiguration().getJobTabRedirection())) {
					oldJobTabRedirection = jobTabRedirection;
				}
			}
		}

		if (null != oldJobTabRedirection) {
			rundeckMonitorJobTabRedirection.setSelectedItem(oldJobTabRedirection);
		}
		else {
			rundeckMonitorJobTabRedirection.setSelectedItem(JobTabRedirection.SUMMARY);
		}
	}

	/**
	 * Validate the RunDeck monitor panel inputs.
	 * 
	 * @return true if the configuration is valid
	 */
	@Override
	public boolean validate() {

		getRundeckMonitorConfiguration().setRundeckMonitorName(rundeckMonitorName.getText());
		getRundeckMonitorConfiguration().setRefreshDelay(rundeckMonitorRefreshDelay.getItemAt(rundeckMonitorRefreshDelay.getSelectedIndex()).getDelay());
		getRundeckMonitorConfiguration().setLateThreshold(rundeckMonitorLateExecutionThreshold.getItemAt(rundeckMonitorLateExecutionThreshold.getSelectedIndex()).getThreshold());
		getRundeckMonitorConfiguration().setFailedJobNumber(rundeckMonitorFailedJobNumber.getItemAt(rundeckMonitorFailedJobNumber.getSelectedIndex()).getFailedJobsNumber());
		getRundeckMonitorConfiguration().setDateFormat(rundeckMonitorDateFormat.getItemAt(rundeckMonitorDateFormat.getSelectedIndex()).getDateFormat());
		getRundeckMonitorConfiguration().setJobTabRedirection(rundeckMonitorJobTabRedirection.getItemAt(rundeckMonitorJobTabRedirection.getSelectedIndex()).name());
		getRundeckMonitorConfiguration().setInterfaceType(rundeckMonitorInterfaceType.getItemAt(rundeckMonitorInterfaceType.getSelectedIndex()).name());

		try {
			getRundeckMonitorConfiguration().saveMonitorConfigurationPropertieFile();
		}
		catch (final IOException e) {
			return false;
		}

		return true;
	}
}
