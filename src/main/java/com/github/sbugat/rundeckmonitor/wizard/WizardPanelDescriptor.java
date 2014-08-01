package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;

public abstract class WizardPanelDescriptor {

	private final String panelIdentifier;

	private final String back;

	private final String next;

	public WizardPanelDescriptor( final String panelIdentifierArg, final String backArg, final String nextArg ) {
		panelIdentifier = panelIdentifierArg;
		back = backArg;
		next = nextArg;
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
}
