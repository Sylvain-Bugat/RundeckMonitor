package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

public abstract class WizardPanelDescriptor {

	private final String panelIdentifier;

	private final String back;

	private final String next;

	protected RundeckMonitorConfiguration rundeckMonitorConfiguration;

	public WizardPanelDescriptor( final String panelIdentifierArg, final String backArg, final String nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg ) {
		panelIdentifier = panelIdentifierArg;
		back = backArg;
		next = nextArg;
		rundeckMonitorConfiguration = rundeckMonitorConfigurationArg;
	}

	public abstract Component getPanelComponent();

	public String getPanelDescriptorIdentifier() {
		return panelIdentifier;
	}

	public String getNext() {
		return next;
	}

	public String getBack() {
		return back;
	}

	public void aboutToDisplayPanel() {

	}

	public void displayingPanel() {

	}

	public void aboutToHidePanel() {

	}

	public boolean validate() {

		return true;
	}
}
