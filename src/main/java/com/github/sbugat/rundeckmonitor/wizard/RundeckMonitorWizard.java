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
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class RundeckMonitorWizard {

	// private WizardModel wizardModel;
	private Map<String, WizardPanelDescriptor> map = new HashMap<>();
	private String currentDescriptor;

	private JDialog Wizard;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	private JButton backButton;
	private JButton nextButton;
	private JButton cancelButton;

	public RundeckMonitorWizard() {

		// wizardModel = new WizardModel();
		Wizard = new JDialog();


		Wizard.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		initComponents();
	}

	private void initComponents() {

		// Code omitted

		JPanel buttonPanel = new JPanel();
		Box buttonBox = new Box(BoxLayout.X_AXIS);

		cardPanel = new JPanel();
		cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		backButton = new JButton("back");
		nextButton = new JButton("next");
		cancelButton = new JButton("cancel");

		final ActionListener backListener = new ActionListener() {
			@SuppressWarnings("synthetic-access")
			public void actionPerformed( final ActionEvent e) {
				final String dest = map.get( currentDescriptor ).getBack();
				setCurrentPanel( dest );
			}
		};
		final ActionListener nextListener = new ActionListener() {
			@SuppressWarnings("synthetic-access")
			public void actionPerformed( final ActionEvent e) {
				final String dest = map.get( currentDescriptor ).getNext();

				if( null == dest ) {
					//Write configuration
					System.exit( 0 );
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
		Wizard.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
		Wizard.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);
		Wizard.setVisible(true);


		Wizard.pack();
		Wizard.setLocationRelativeTo(null);

	}

	public void registerWizardPanel(String id, WizardPanelDescriptor panel) {
		cardPanel.add(panel.getPanelComponent(), id);
		map.put(id, panel);
	}


	void setBackButtonEnabled(boolean b) {
		backButton.setEnabled(b);
	}

	void setNextButtonEnabled(boolean b) {
		nextButton.setEnabled(b);
	}

	public void setCurrentPanel( final String id ) {

		if (currentDescriptor != null) {
			WizardPanelDescriptor oldPanelDescriptor = map.get(currentDescriptor);
			oldPanelDescriptor.aboutToHidePanel();
		}

		currentDescriptor = id;

		map.get( id ).aboutToDisplayPanel();

		if( null == map.get( id ).getNext() ) {

			nextButton.setText( "Finish" );
		}
		else {

			nextButton.setText( "Next" );
		}

		backButton.setVisible( null != map.get( id ).getBack() );

		cardLayout.show(cardPanel, id.toString());

		Wizard.pack();

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

		RundeckMonitorWizard rundeckMonitorWizard = new RundeckMonitorWizard();
		WizardPanelDescriptor1 wpd1 =new WizardPanelDescriptor1( "1", null, "2" );
		WizardPanelDescriptor2 wpd2 = new WizardPanelDescriptor2("2", "1", "3" );
		WizardPanelDescriptor2 wpd3 = new WizardPanelDescriptor2("3", "2", null );

		rundeckMonitorWizard.registerWizardPanel("1", wpd1 );
		rundeckMonitorWizard.registerWizardPanel("2", wpd2 );
		rundeckMonitorWizard.registerWizardPanel("3", wpd3 );
		rundeckMonitorWizard.setCurrentPanel("1");
	}
}
