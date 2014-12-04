package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

/**
 * Generic wizard panel with a next and previous step
 *
 * @author Sylvain bugat
 *
 */
public abstract class WizardPanelDescriptor {

	private final ConfigurationWizardStep panelIdentifier;

	private final ConfigurationWizardStep back;

	private final ConfigurationWizardStep next;

	protected RundeckMonitorConfiguration rundeckMonitorConfiguration;

	public WizardPanelDescriptor( final ConfigurationWizardStep panelIdentifierArg, final ConfigurationWizardStep backArg, final ConfigurationWizardStep nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg ) {
		panelIdentifier = panelIdentifierArg;
		back = backArg;
		next = nextArg;
		rundeckMonitorConfiguration = rundeckMonitorConfigurationArg;
	}

	public abstract Component getPanelComponent();

	public ConfigurationWizardStep getPanelDescriptorIdentifier() {
		return panelIdentifier;
	}

	/**
	 * Return the next wizard panel or null
	 * @return next panel
	 */
	public ConfigurationWizardStep getNext() {
		return next;
	}

	/**
	 * Return the previous wizard panel or null
	 * @return previous panel
	 */
	public ConfigurationWizardStep getBack() {
		return back;
	}

	/**
	 * Method called before diplaying a panel for dynamic content update
	 */
	public void aboutToDisplayPanel() {
		//Default: nothing to do
	}

	/**
	 * Default method to validate the wizard step, default return true
	 *
	 * @return true by default
	 */
	public boolean validate() {
		//Default: return OK
		return true;
	}
}
