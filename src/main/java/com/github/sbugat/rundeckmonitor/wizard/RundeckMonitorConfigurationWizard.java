package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

public class RundeckMonitorConfigurationWizard {

	private Map<ConfigurationWizardStep, WizardPanelDescriptor> map = new HashMap<>();
	private ConfigurationWizardStep currentStep;

	private final JFrame wizardFrame;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	private JButton backButton;
	private JButton nextButton;
	private JButton cancelButton;

	public RundeckMonitorConfigurationWizard() {

		// wizardModel = new WizardModel();
		wizardFrame = new JFrame();
		wizardFrame.setTitle( "RundeckMonitor configuration wizard" ); //$NON-NLS-1$
		wizardFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		initComponents();

		RundeckMonitorConfiguration rundeckMonitorConfiguration = new RundeckMonitorConfiguration();
		WizardPanelDescriptor wpd1 = new RundeckConfigurationWizardPanelDescriptor( ConfigurationWizardStep.RUNDECK_STEP, null, ConfigurationWizardStep.PROJECT_STEP, rundeckMonitorConfiguration );
		WizardPanelDescriptor wpd2 = new ProjectConfigurationWizardPanelDescriptor( ConfigurationWizardStep.PROJECT_STEP, ConfigurationWizardStep.RUNDECK_STEP, ConfigurationWizardStep.MONITOR_STEP, rundeckMonitorConfiguration );
		WizardPanelDescriptor wpd3 = new MonitorConfigurationWizardPanelDescriptor( ConfigurationWizardStep.MONITOR_STEP, ConfigurationWizardStep.PROJECT_STEP, null, rundeckMonitorConfiguration );

		registerWizardPanel( wpd1 );
		registerWizardPanel( wpd2 );
		registerWizardPanel( wpd3 );
		setCurrentPanel( ConfigurationWizardStep.RUNDECK_STEP );

		wizardFrame.pack();
		wizardFrame.setLocationRelativeTo(null);

		wizardFrame.setVisible(true);

	}

	private void initComponents() {

		// Code omitted

		JPanel buttonPanel = new JPanel();
		Box buttonBox = new Box(BoxLayout.X_AXIS);

		cardPanel = new JPanel();
		cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		backButton = new JButton( "back" ); //$NON-NLS-1$
		nextButton = new JButton( "next" ); //$NON-NLS-1$
		cancelButton = new JButton( "cancel" ); //$NON-NLS-1$

		final ActionListener backListener = new ActionListener() {
			@SuppressWarnings("synthetic-access")
			public void actionPerformed( final ActionEvent e) {
				final ConfigurationWizardStep dest = map.get( currentStep ).getBack();
				setCurrentPanel( dest );
			}
		};
		final ActionListener nextListener = new ActionListener() {
			@SuppressWarnings("synthetic-access")
			public void actionPerformed( final ActionEvent e) {
				final ConfigurationWizardStep dest = map.get( currentStep ).getNext();

				if( null == dest ) {

					final WizardPanelDescriptor oldPanelDescriptor = map.get( currentStep );

					//Write configuration
					oldPanelDescriptor.validate();

					wizardFrame.setVisible( false );
				}
				else {
					setCurrentPanel( dest );
				}
			}
		};
		final ActionListener cancelListener = new ActionListener() {
			@SuppressWarnings("synthetic-access")
			public void actionPerformed( final ActionEvent e) {
				System.exit( 0 );
			}
		};
		backButton.addActionListener(backListener);
		nextButton.addActionListener(nextListener);
		cancelButton.addActionListener(cancelListener);

		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

		buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
		buttonBox.add(backButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(nextButton);
		buttonBox.add(Box.createHorizontalStrut(30));
		buttonBox.add(cancelButton);
		buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
		wizardFrame.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
		wizardFrame.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);


	}

	public void registerWizardPanel( final WizardPanelDescriptor panel ) {
		cardPanel.add( panel.getPanelComponent(), panel.getPanelDescriptorIdentifier().toString() );
		map.put( panel.getPanelDescriptorIdentifier(), panel );
	}


	public void setBackButtonEnabled(boolean b) {
		backButton.setEnabled(b);
	}

	public void setNextButtonEnabled(boolean b) {
		nextButton.setEnabled(b);
	}

	public void setCurrentPanel( final ConfigurationWizardStep id ) {

		if (currentStep != null) {
			WizardPanelDescriptor oldPanelDescriptor = map.get( currentStep );

			if( ! oldPanelDescriptor.validate() ) {
				return;
			}
		}

		currentStep = id;

		map.get( id ).aboutToDisplayPanel();

		if( null == map.get( id ).getNext() ) {
			nextButton.setText( "finish" ); //$NON-NLS-1$
		}
		else {
			nextButton.setText( "next" ); //$NON-NLS-1$
		}

		backButton.setVisible( null != map.get( id ).getBack() );

		cardLayout.show(cardPanel, id.toString());

		wizardFrame.pack();

		map.get( id ).displayingPanel();
	}


	public static void main(String arg[]) {

		//Try to use the system Look&Feel
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch( final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e ) {

			//If System Look&Feel is not supported, stay with the default one
		}

		RundeckMonitorConfigurationWizard rundeckMonitorWizard = new RundeckMonitorConfigurationWizard();
		//RundeckMonitorConfiguration rundeckMonitorConfiguration = new RundeckMonitorConfiguration();
		//WizardPanelDescriptor wpd1 = new RundeckConfigurationWizardPanelDescriptor( ConfigurationWizardStep.RUNDECK_STEP, null, ConfigurationWizardStep.PROJECT_STEP, rundeckMonitorConfiguration );
		//WizardPanelDescriptor wpd2 = new ProjectConfigurationWizardPanelDescriptor( ConfigurationWizardStep.PROJECT_STEP, ConfigurationWizardStep.RUNDECK_STEP, ConfigurationWizardStep.MONITOR_STEP, rundeckMonitorConfiguration );
		//WizardPanelDescriptor wpd3 = new MonitorConfigurationWizardPanelDescriptor( ConfigurationWizardStep.MONITOR_STEP, ConfigurationWizardStep.PROJECT_STEP, null, rundeckMonitorConfiguration );

		//rundeckMonitorWizard.registerWizardPanel( wpd1 );
		//rundeckMonitorWizard.registerWizardPanel( wpd2 );
		//rundeckMonitorWizard.registerWizardPanel( wpd3 );
		//rundeckMonitorWizard.setCurrentPanel( ConfigurationWizardStep.RUNDECK_STEP );
	}
}
