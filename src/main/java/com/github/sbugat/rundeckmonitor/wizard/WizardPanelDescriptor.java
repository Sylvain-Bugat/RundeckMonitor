package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

/**
 * Generic wizard panel with a next and previous step.
 *
 * @author Sylvain bugat
 *
 */
public abstract class WizardPanelDescriptor {

	/** Panel identifier. */
	private final ConfigurationWizardStep panelIdentifier;

	/** Previous panel or null. */
	private final ConfigurationWizardStep back;

	/** Next panel or null. */
	private final ConfigurationWizardStep next;

	/** RunDeck monitor configuration. */
	private final RundeckMonitorConfiguration rundeckMonitorConfiguration;

	/**
	 * Copyr arguments to constants.
	 *
	 * @param panelIdentifierArg panel identifier
	 * @param backArg previous panel or null if none
	 * @param nextArg next panel or null if none
	 * @param rundeckMonitorConfigurationArg RunDeck monitor configuration shared by all panels
	 */
	public WizardPanelDescriptor(final ConfigurationWizardStep panelIdentifierArg, final ConfigurationWizardStep backArg, final ConfigurationWizardStep nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg) {
		panelIdentifier = panelIdentifierArg;
		back = backArg;
		next = nextArg;
		rundeckMonitorConfiguration = rundeckMonitorConfigurationArg;
	}

	/**
	 * Get the main component of a panel.
	 *
	 * @return the main component
	 */
	public abstract Component getPanelComponent();

	/**
	 * Get the panel identifier.
	 *
	 * @return the panel identifier
	 */
	public final ConfigurationWizardStep getPanelDescriptorIdentifier() {
		return panelIdentifier;
	}

	/**
	 * Get the RunDeck monitor main configuration.
	 *
	 * @return monitor configuration
	 */
	public final RundeckMonitorConfiguration getRundeckMonitorConfiguration() {
		return rundeckMonitorConfiguration;
	}

	/**
	 * Return the next wizard panel or null.
	 *
	 * @return next panel
	 */
	public final ConfigurationWizardStep getNext() {
		return next;
	}

	/**
	 * Return the previous wizard panel or null.
	 *
	 * @return previous panel
	 */
	public final ConfigurationWizardStep getBack() {
		return back;
	}

	/**
	 * Method called before diplaying a panel for dynamic content update.
	 */
	public void aboutToDisplayPanel() {
		// Default: nothing to do
	}

	/**
	 * Default method to validate the wizard step, must return true if input data are valid.
	 *
	 * @return true if valid, false otherwise
	 */
	public abstract boolean validate();
}
