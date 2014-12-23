package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.rundeck.api.RundeckClient;
import org.rundeck.api.RundeckClientBuilder;
import org.rundeck.api.domain.RundeckProject;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.tools.RundeckClientTools;

public class ProjectConfigurationWizardPanelDescriptor extends WizardPanelDescriptor {

	final Container container = new Container();
	final GridBagLayout layout = new GridBagLayout();

	final JComboBox<String> rundeckProjectNameTextField = new JComboBox<>();

	final JComboBox<RundeckAPIVersion> rundeckRundeckAPIVersionTextField = new JComboBox<>();

	public ProjectConfigurationWizardPanelDescriptor( final ConfigurationWizardStep panelIdentifierArg, final ConfigurationWizardStep backArg, final ConfigurationWizardStep nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg ) {
		super( panelIdentifierArg, backArg, nextArg, rundeckMonitorConfigurationArg );

		container.setLayout( layout );
		final JLabel rundeckProjectlabel = new JLabel( "Rundeck project:" ); //$NON-NLS-1$
		final JLabel rundeckApiVersionlabel = new JLabel( "Rundeck API version:" ); //$NON-NLS-1$

		final GridBagConstraints gridBagConstraits = new GridBagConstraints();
		gridBagConstraits.insets = new Insets( 2,2,2,2 );
		gridBagConstraits.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraits.gridwidth = 1;

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=0;
		container.add( rundeckProjectlabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		container.add( rundeckProjectNameTextField, gridBagConstraits );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=1;
		container.add( rundeckApiVersionlabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		container.add( rundeckRundeckAPIVersionTextField, gridBagConstraits );
	}

	@Override
	public Component getPanelComponent() {

		return container;
	}

	public void aboutToDisplayPanel() {

		//Initialize the rundeck client with the minimal rundeck version (1)
		final RundeckClient rundeckClient = RundeckClientTools.buildRundeckClient( rundeckMonitorConfiguration );

		rundeckProjectNameTextField.removeAllItems();
		//Check if the configured project exists
		boolean existingOldConfiguredProject = false;
		for( final RundeckProject rundeckProject: rundeckClient.getProjects() ) {

			final String currentProjectName = rundeckProject.getName();
			rundeckProjectNameTextField.addItem( currentProjectName );

			if( ! currentProjectName.isEmpty() && currentProjectName.equals( rundeckMonitorConfiguration.getRundeckProject() ) ) {

				existingOldConfiguredProject = true;
			}
		}

		if( existingOldConfiguredProject ) {
			rundeckProjectNameTextField.setSelectedItem( rundeckMonitorConfiguration.getRundeckProject() );
		}

		final String rundeckVersion = rundeckClient.getSystemInfo().getVersion();

		rundeckRundeckAPIVersionTextField.removeAllItems();
		RundeckAPIVersion oldApiVersion = null;
		for( final RundeckAPIVersion version : RundeckAPIVersion.values() ) {

			if( rundeckVersion.compareTo( version.getSinceReturnVersion() ) >= 0  ) {
				rundeckRundeckAPIVersionTextField.addItem( version );

				if( rundeckMonitorConfiguration.getRundeckAPIversion() > 0 ) {
					if( version.getVersion().getVersionNumber() == rundeckMonitorConfiguration.getRundeckAPIversion() ) {

						oldApiVersion = version;
					}
				}
				else if( version.getVersion().getVersionNumber() <= 10 ){

					oldApiVersion = version;
				}
			}
		}

		if( null != oldApiVersion ) {
			rundeckRundeckAPIVersionTextField.setSelectedItem( oldApiVersion );
		}
	}

	public boolean validate() {

		final RundeckClientBuilder rundeckClientBuilder;
		final String rundeckUrl = rundeckMonitorConfiguration.getRundeckUrl();
		if( ! rundeckMonitorConfiguration.getRundeckAPIKey().isEmpty() ) {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckMonitorConfiguration.getRundeckAPIKey() );
		}
		else {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckMonitorConfiguration.getRundeckLogin(), rundeckMonitorConfiguration.getRundeckPassword() );
		}

		final RundeckAPIVersion apiVersion = rundeckRundeckAPIVersionTextField.getItemAt( rundeckRundeckAPIVersionTextField.getSelectedIndex() );

		//Initialize the rundeck client
		final RundeckClient rundeckClient = rundeckClientBuilder.version( apiVersion.getVersion() ).build();

		//Test authentication credentials
		rundeckClient.ping();
		rundeckClient.testAuth();

		//Check if the configured project exists
		boolean existingProject = false;
		for( final RundeckProject rundeckProject: rundeckClient.getProjects() ) {

			if( rundeckProjectNameTextField.getSelectedItem().equals( rundeckProject.getName() ) ) {
				existingProject = true;
				break;
			}
		}

		if( ! existingProject ) {
			JOptionPane.showMessageDialog( null, "Unknown rundeck project," + System.lineSeparator() + "check and change this poject name:" + System.lineSeparator() + '"' + rundeckProjectNameTextField.getSelectedItem() + "\".", "RundeckMonitor wizard error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return false;
		}

		rundeckMonitorConfiguration.setRundeckProject( rundeckProjectNameTextField.getItemAt( rundeckProjectNameTextField.getSelectedIndex() ) );
		rundeckMonitorConfiguration.setRundeckAPIversion( rundeckRundeckAPIVersionTextField.getItemAt( rundeckRundeckAPIVersionTextField.getSelectedIndex() ).getVersion().getVersionNumber() );

		return true;
	}
}
