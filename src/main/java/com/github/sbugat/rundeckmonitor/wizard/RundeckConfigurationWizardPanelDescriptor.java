package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.rundeck.api.RundeckClient;
import org.rundeck.api.RundeckClientBuilder;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

public class RundeckConfigurationWizardPanelDescriptor extends WizardPanelDescriptor {

	final Container container = new Container();

	final JTextField rundeckUrlTextField = new JTextField( 30 );

	final JTextField rundeckAPITokenTextField = new JPasswordField( 30 );
	final JTextField rundeckLoginTextField = new JTextField( 30 );
	final JTextField rundeckPasswordTextField = new JPasswordField( 30 );

	public RundeckConfigurationWizardPanelDescriptor( final ConfigurationWizardStep panelIdentifierArg, final ConfigurationWizardStep backArg, final ConfigurationWizardStep nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg ) {
		super( panelIdentifierArg, backArg, nextArg, rundeckMonitorConfigurationArg );

		container.setLayout( new BoxLayout( container, BoxLayout.PAGE_AXIS ) );
		final JLabel rundeckUrllabel = new JLabel( "Rundeck URL:" ); //$NON-NLS-1$
		final JLabel rundeckAPITokenlabel = new JLabel( "API token:" ); //$NON-NLS-1$
		final JLabel rundeckLoginlabel = new JLabel( "Login:" ); //$NON-NLS-1$
		final JLabel rundeckPasswordlabel = new JLabel( "Password:" ); //$NON-NLS-1$

		final JPanel topPanel = new JPanel();
		topPanel.setLayout( new GridBagLayout() );
		final GridBagConstraints gridBagConstraits = new GridBagConstraints();
		gridBagConstraits.insets = new Insets( 2,2,2,2 );
		gridBagConstraits.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraits.gridwidth = 1;

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=0;
		topPanel.add( rundeckUrllabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		topPanel.add( rundeckUrlTextField, gridBagConstraits );

		container.add( topPanel );

		container.add( new JSeparator() );

		final JPanel aPIKeyPanel = new JPanel();
		aPIKeyPanel.setLayout( new GridBagLayout() );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=0;
		aPIKeyPanel.add( rundeckAPITokenlabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		aPIKeyPanel.add( rundeckAPITokenTextField, gridBagConstraits );

		container.add( aPIKeyPanel );

		container.add( new JLabel( "OR" ) ); //$NON-NLS-1$

		final JPanel loginPasswordPanel = new JPanel();
		loginPasswordPanel.setLayout( new GridBagLayout() );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=0;
		loginPasswordPanel.add( rundeckLoginlabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		loginPasswordPanel.add( rundeckLoginTextField, gridBagConstraits );

		gridBagConstraits.gridx=0;
		gridBagConstraits.gridy=1;
		loginPasswordPanel.add( rundeckPasswordlabel, gridBagConstraits );
		gridBagConstraits.gridx=1;
		loginPasswordPanel.add( rundeckPasswordTextField, gridBagConstraits );

		container.add( loginPasswordPanel );

		container.add( new JSeparator() );
	}

	@Override
	public Component getPanelComponent() {

		return container;
	}

	public boolean validate() {

		final RundeckClientBuilder rundeckClientBuilder;
		final String rundeckUrl = rundeckUrlTextField.getText();
		if( ! rundeckAPITokenTextField.getText().isEmpty() ) {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckAPITokenTextField.getText() );
		}
		else {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckLoginTextField.getText(), rundeckPasswordTextField.getText() );
		}

		//Initialize the rundeck client
		final RundeckClient rundeckClient = rundeckClientBuilder.build();

		//Test authentication credentials
		rundeckClient.ping();
		rundeckClient.testAuth();

		rundeckMonitorConfiguration.setRundeckUrl( rundeckUrlTextField.getText() );
		rundeckMonitorConfiguration.setRundeckAPIKey( rundeckAPITokenTextField.getText() );
		rundeckMonitorConfiguration.setRundeckLogin( rundeckLoginTextField.getText() );
		rundeckMonitorConfiguration.setRundeckPassword( rundeckPasswordTextField.getText() );

		return true;
	}
}
