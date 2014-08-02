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

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

public class MonitorConfigurationWizardPanelDescriptor extends WizardPanelDescriptor {

	final Container container = new Container();
	final GridBagLayout layout = new GridBagLayout();

	final JTextField rundeckMonitorName = new JTextField( 30 );
	final JComboBox<Integer> rundeckMonitorRefreshDelay = new JComboBox<>();
	final JComboBox<Integer> rundeckMonitorLateExecutionThreshold = new JComboBox<>();
	final JComboBox<Integer> rundeckMonitorFailedJobNumber = new JComboBox<>();
	final JComboBox<String> rundeckMonitorDateFormat = new JComboBox<>();

	public MonitorConfigurationWizardPanelDescriptor( final String panelIdentifierArg, final String backArg, final String nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg ) {
		super( panelIdentifierArg, backArg, nextArg, rundeckMonitorConfigurationArg );

		container.setLayout( layout );
		final JLabel rundeckMonitorNameLabel = new JLabel( "Tray-icon monitor name:" ); //$NON-NLS-1$
		final JLabel rundeckMonitorRefreshDelayLabel = new JLabel( "Failed/late jobs refresh delay:" ); //$NON-NLS-1$
		final JLabel rundeckMonitorLateExecutionThresholdLabel = new JLabel( "Late execution detection threshold:" ); //$NON-NLS-1$
		final JLabel rundeckMonitorFailedJobNumberLabel = new JLabel( "Number of failed/late jobs to display:" ); //$NON-NLS-1$
		final JLabel rundeckMonitorDateFormatLabel = new JLabel( "Failed/late jobs displayed date format:" ); //$NON-NLS-1$

		//Fields initialization
		rundeckMonitorName.setText( RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_NAME_DEFAULT_VALUE );

		rundeckMonitorRefreshDelay.addItem( Integer.valueOf( 10 ) );
		rundeckMonitorRefreshDelay.addItem( Integer.valueOf( 30 ) );
		rundeckMonitorRefreshDelay.addItem( Integer.valueOf( 60 ) );
		rundeckMonitorRefreshDelay.addItem( Integer.valueOf( 120 ) );
		rundeckMonitorRefreshDelay.addItem( Integer.valueOf( 300 ) );
		rundeckMonitorRefreshDelay.addItem( Integer.valueOf( 6000 ) );
		rundeckMonitorRefreshDelay .setSelectedItem( Integer.valueOf( 60 ) );

		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 5 ) );
		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 10 ) );
		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 20 ) );
		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 30 ) );
		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 40 ) );
		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 60 ) );
		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 90 ) );
		rundeckMonitorLateExecutionThreshold.addItem( Integer.valueOf( 120 ) );
		rundeckMonitorLateExecutionThreshold.setSelectedItem( Integer.valueOf( 30 ) );

		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 5 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 6 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 7 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 8 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 9 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 10 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 11 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 12 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 13 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 14 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 15 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 16 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 17 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 18 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 19 ) );
		rundeckMonitorFailedJobNumber.addItem( Integer.valueOf( 20 ) );
		rundeckMonitorFailedJobNumber.setSelectedItem( Integer.valueOf( 20 ) );

		rundeckMonitorDateFormat.addItem( RundeckMonitorConfiguration.RUNDECK_MONITOR_PROPERTY_DATE_FORMAT_DEFAULT_VALUE );

		final GridBagConstraints gridBagConstraits = new GridBagConstraints();
		gridBagConstraits.insets = new Insets( 2,2,2,2 );
		gridBagConstraits.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraits.gridwidth = 1;

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=0;
		container.add( rundeckMonitorNameLabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		container.add( rundeckMonitorName, gridBagConstraits );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=1;
		container.add( rundeckMonitorRefreshDelayLabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		container.add( rundeckMonitorRefreshDelay, gridBagConstraits );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=2;
		container.add( rundeckMonitorLateExecutionThresholdLabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		container.add( rundeckMonitorLateExecutionThreshold, gridBagConstraits );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=3;
		container.add( rundeckMonitorFailedJobNumberLabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		container.add( rundeckMonitorFailedJobNumber, gridBagConstraits );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=4;
		container.add( rundeckMonitorDateFormatLabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		container.add( rundeckMonitorDateFormat, gridBagConstraits );
	}

	@Override
	public Component getPanelComponent() {

		return container;
	}

	public void aboutToDisplayPanel() {

	}

	public boolean validate() {

		rundeckMonitorConfiguration.setRundeckMonitorName( rundeckMonitorName.getText() );
		rundeckMonitorConfiguration.setRefreshDelay( rundeckMonitorRefreshDelay.getItemAt( rundeckMonitorRefreshDelay.getSelectedIndex() ) );
		rundeckMonitorConfiguration.setLateThreshold( rundeckMonitorLateExecutionThreshold.getItemAt( rundeckMonitorLateExecutionThreshold.getSelectedIndex() ) );
		rundeckMonitorConfiguration.setFailedJobNumber( rundeckMonitorFailedJobNumber.getItemAt( rundeckMonitorFailedJobNumber.getSelectedIndex() ) );
		rundeckMonitorConfiguration.setDateFormat( rundeckMonitorDateFormat.getItemAt( rundeckMonitorDateFormat.getSelectedIndex() ) );

		try {
			rundeckMonitorConfiguration.saveMonitorConfigurationPropertieFile();
		}
		catch( final IOException e ) {
			return false;
		}

		return true;
	}
}
