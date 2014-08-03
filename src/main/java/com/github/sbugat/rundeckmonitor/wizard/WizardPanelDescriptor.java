package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

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

	public ConfigurationWizardStep getNext() {
		return next;
	}

	public ConfigurationWizardStep getBack() {
		return back;
	}

	public void aboutToDisplayPanel() {
		//Default: nothing to do
	}

	public boolean validate() {
		//Default: return OK
		return true;
	}
}
