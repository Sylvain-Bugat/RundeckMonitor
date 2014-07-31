package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;

public abstract class WizardPanelDescriptor {

	private final String panelIdentifier;

	private String next;

	private String back;

	WizardPanelDescriptor( String id ) {
		panelIdentifier = id;
	}

	public abstract Component getPanelComponent();

	public String getPanelDescriptorIdentifier() {
		return panelIdentifier;
	}

	public String getNextPanelDescriptor() {
		return next;
	}

	public void setNextPanelDescriptor( final String nextArg) {
		next = nextArg;
	}

	public String getBackPanelDescriptor() {
		return back;
	}

	public void setBackPanelDescriptor( final String backArg) {
		back = backArg;
	}

	public void aboutToDisplayPanel() {

		// Place code here that will be executed before the
		// panel is displayed.

	}

	public void displayingPanel() {

		// Place code here that will be executed when the
		// panel is displayed.

	}

	public void aboutToHidePanel() {

		// Place code here that will be executed when the
		// panel is hidden.

	}
}
