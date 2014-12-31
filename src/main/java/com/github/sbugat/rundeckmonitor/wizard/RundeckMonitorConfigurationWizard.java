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
import javax.swing.border.EmptyBorder;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.tools.SystemTools;

/**
 * Class of the configuration Wizard
 * 
 * @author Sylvain Bugat
 * 
 */
public final class RundeckMonitorConfigurationWizard {

	/** Configuration wizard frame title. */
	private static final String WIZARD_FRAME_TITLE = "RundeckMonitor configuration wizard"; //$NON-NLS-1$

	private static final String BACK_BUTTON_LABEL = "Back"; //$NON-NLS-1$
	private static final String CANCEL_BUTTON_LABEL = "Cancel"; //$NON-NLS-1$
	private static final String FINISH_BUTTON_LABEL = "Finish"; //$NON-NLS-1$
	private static final String NEXT_BUTTON_LABEL = "Next"; //$NON-NLS-1$

	private final Map<ConfigurationWizardStep, WizardPanelDescriptor> map = new HashMap<>();
	private ConfigurationWizardStep currentStep;

	/** Configuration wizard main frame. */
	private final JFrame wizardFrame;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	/** Back/previous step button of the wizard */
	private JButton backButton;
	/** Next step button of the wizard */
	private JButton nextButton;

	/**
	 * Initialize a new configuration wizard.
	 * 
	 * @param rundeckMonitorConfiguration RunDeck monitor configuration
	 * @param exitOnClose indicate if the program must exit if the wizard is closed
	 */
	public RundeckMonitorConfigurationWizard(final RundeckMonitorConfiguration rundeckMonitorConfiguration, final boolean exitOnClose) {

		wizardFrame = new JFrame();
		wizardFrame.setTitle(WIZARD_FRAME_TITLE);

		if (exitOnClose) {
			wizardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		else {
			wizardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}

		// Initialize displayed components
		initComponents(exitOnClose);

		// Initialize steps and set the first step
		final WizardPanelDescriptor wpd1 = new RundeckConfigurationWizardPanelDescriptor(null, ConfigurationWizardStep.PROJECT_STEP, rundeckMonitorConfiguration);
		final WizardPanelDescriptor wpd2 = new ProjectConfigurationWizardPanelDescriptor(ConfigurationWizardStep.RUNDECK_STEP, ConfigurationWizardStep.MONITOR_STEP, rundeckMonitorConfiguration);
		final WizardPanelDescriptor wpd3 = new MonitorConfigurationWizardPanelDescriptor(ConfigurationWizardStep.PROJECT_STEP, null, rundeckMonitorConfiguration);

		registerWizardPanel(wpd1);
		registerWizardPanel(wpd2);
		registerWizardPanel(wpd3);
		setCurrentPanel(ConfigurationWizardStep.RUNDECK_STEP, false);

		// Resize the frame
		wizardFrame.pack();
		wizardFrame.setResizable(false);
		// Center the frame on the screen
		wizardFrame.setLocationRelativeTo(null);

		// Display the frame
		wizardFrame.setVisible(true);
	}

	private void initComponents(final boolean exitOnClose) {

		final JPanel buttonPanel = new JPanel();
		final Box buttonBox = new Box(BoxLayout.X_AXIS);

		cardPanel = new JPanel();
		cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		backButton = new JButton(BACK_BUTTON_LABEL);
		nextButton = new JButton(NEXT_BUTTON_LABEL);
		final JButton cancelButton = new JButton(CANCEL_BUTTON_LABEL);

		final ActionListener backListener = new ActionListener() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final ConfigurationWizardStep dest = map.get(currentStep).getBack();
				setCurrentPanel(dest, false);
			}
		};
		final ActionListener nextListener = new ActionListener() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {
				final ConfigurationWizardStep dest = map.get(currentStep).getNext();

				if (null == dest) {

					final WizardPanelDescriptor oldPanelDescriptor = map.get(currentStep);

					// Write configuration
					oldPanelDescriptor.validate();

					wizardFrame.setVisible(false);
					wizardFrame.dispose();
				}
				else {
					setCurrentPanel(dest, true);
				}
			}
		};
		final ActionListener cancelListener = new ActionListener() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void actionPerformed(final ActionEvent e) {

				if (exitOnClose) {
					SystemTools.exit(SystemTools.EXIT_CODE_OK);
				}
				else {
					wizardFrame.setVisible(false);
					wizardFrame.dispose();
				}
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
		buttonPanel.add(buttonBox, BorderLayout.EAST);
		wizardFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		wizardFrame.getContentPane().add(cardPanel, BorderLayout.CENTER);
	}

	public void registerWizardPanel(final WizardPanelDescriptor panel) {
		cardPanel.add(panel.getPanelComponent(), panel.getPanelDescriptorIdentifier().toString());
		map.put(panel.getPanelDescriptorIdentifier(), panel);
	}

	public void setCurrentPanel(final ConfigurationWizardStep id, final boolean next) {

		// If going to the next panel, validate it and don't advance if validation failed
		if (next && currentStep != null && !map.get(currentStep).validate()) {
			return;
		}

		currentStep = id;

		map.get(id).aboutToDisplayPanel();

		if (null == map.get(id).getNext()) {
			nextButton.setText(FINISH_BUTTON_LABEL);
		}
		else {
			nextButton.setText(NEXT_BUTTON_LABEL);
		}

		backButton.setVisible(null != map.get(id).getBack());

		cardLayout.show(cardPanel, id.toString());
	}
}
