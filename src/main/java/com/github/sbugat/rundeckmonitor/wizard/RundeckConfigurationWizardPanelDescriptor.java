package com.github.sbugat.rundeckmonitor.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.rundeck.api.RundeckApiException;
import org.rundeck.api.RundeckApiException.RundeckApiLoginException;
import org.rundeck.api.RundeckApiException.RundeckApiTokenException;
import org.rundeck.api.RundeckClient;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.tools.RundeckClientTools;

/**
 * RunDeck configuration wizard panel.
 * 
 * @author Sylvain Bugat
 * 
 */
public final class RundeckConfigurationWizardPanelDescriptor extends WizardPanelDescriptor {

	/** Main container of the panel. */
	private final Container container = new Container();

	/** RunDeck URL input. */
	private final JTextField rundeckUrlTextField = new JTextField(30);

	/** Password input. */
	private final JTextField rundeckAPITokenTextField = new JPasswordField(30);
	/** Login input. */
	private final JTextField rundeckLoginTextField = new JTextField(30);
	/** RunDeck API key input. */
	private final JTextField rundeckPasswordTextField = new JPasswordField(30);

	/**
	 * Copy arguments and initialize the RunDeck configuration wizard panel.
	 * 
	 * @param backArg previous panel
	 * @param nextArg next panel
	 * @param rundeckMonitorConfigurationArg RunDeck monitor common configuration
	 */
	public RundeckConfigurationWizardPanelDescriptor(final ConfigurationWizardStep backArg, final ConfigurationWizardStep nextArg, final RundeckMonitorConfiguration rundeckMonitorConfigurationArg) {
		super(ConfigurationWizardStep.RUNDECK_STEP, backArg, nextArg, rundeckMonitorConfigurationArg);

		rundeckUrlTextField.setText(rundeckMonitorConfigurationArg.getRundeckUrl());
		rundeckAPITokenTextField.setText(rundeckMonitorConfigurationArg.getRundeckAPIKey());
		rundeckLoginTextField.setText(rundeckMonitorConfigurationArg.getRundeckLogin());
		rundeckPasswordTextField.setText(rundeckMonitorConfigurationArg.getRundeckPassword());

		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		final JLabel rundeckUrllabel = new JLabel("Rundeck URL:"); //$NON-NLS-1$
		final JLabel rundeckAPITokenlabel = new JLabel("API token:"); //$NON-NLS-1$
		final JLabel rundeckLoginlabel = new JLabel("Login:"); //$NON-NLS-1$
		final JLabel rundeckPasswordlabel = new JLabel("Password:"); //$NON-NLS-1$

		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		final GridBagConstraints gridBagConstraits = new GridBagConstraints();
		gridBagConstraits.insets = new Insets(2, 2, 2, 2);
		gridBagConstraits.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraits.gridwidth = 1;

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 0;
		topPanel.add(rundeckUrllabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		topPanel.add(rundeckUrlTextField, gridBagConstraits);

		container.add(topPanel);

		container.add(new JSeparator());

		final JPanel aPIKeyPanel = new JPanel();
		aPIKeyPanel.setLayout(new GridBagLayout());

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 0;
		aPIKeyPanel.add(rundeckAPITokenlabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		aPIKeyPanel.add(rundeckAPITokenTextField, gridBagConstraits);

		container.add(aPIKeyPanel);

		container.add(new JLabel("OR")); //$NON-NLS-1$

		final JPanel loginPasswordPanel = new JPanel();
		loginPasswordPanel.setLayout(new GridBagLayout());

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 0;
		loginPasswordPanel.add(rundeckLoginlabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		loginPasswordPanel.add(rundeckLoginTextField, gridBagConstraits);

		gridBagConstraits.gridx = 0;
		gridBagConstraits.gridy = 1;
		loginPasswordPanel.add(rundeckPasswordlabel, gridBagConstraits);
		gridBagConstraits.gridx = 1;
		loginPasswordPanel.add(rundeckPasswordTextField, gridBagConstraits);

		container.add(loginPasswordPanel);

		container.add(new JSeparator());
	}

	@Override
	public Component getPanelComponent() {

		return container;
	}

	@Override
	public boolean validate() {

		if (rundeckUrlTextField.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Missing rundeck URL parameter.", "Missing parameter", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		if (rundeckAPITokenTextField.getText().isEmpty() && (rundeckLoginTextField.getText().isEmpty() || rundeckPasswordTextField.getText().isEmpty())) {

			JOptionPane.showMessageDialog(null, "Missing rundeck API token," + System.lineSeparator() + "or rundeck login/password parameters", "Missing parameters", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}

		getRundeckMonitorConfiguration().setRundeckUrl(rundeckUrlTextField.getText().replaceFirst("/+$", "")); //$NON-NLS-1$ //$NON-NLS-2$
		getRundeckMonitorConfiguration().setRundeckAPIKey(rundeckAPITokenTextField.getText());
		getRundeckMonitorConfiguration().setRundeckLogin(rundeckLoginTextField.getText());
		getRundeckMonitorConfiguration().setRundeckPassword(rundeckPasswordTextField.getText());

		// Test authentication credentials
		final RundeckClient rundeckClient;
		try {
			// Initialize the rundeck client with minimal rundeck version (1)
			rundeckClient = RundeckClientTools.buildMinimalRundeckClient(getRundeckMonitorConfiguration());
			rundeckClient.ping();
			rundeckClient.testAuth();
		}
		/*
		 * catch( final RundeckApiException e ) { JOptionPane.showMessageDialog( null, "Unable to connect to the project URL," + System.lineSeparator() + "check and change this parameter.", "Configuration error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ return false; }
		 */
		catch (final RundeckApiTokenException e) {

			JOptionPane.showMessageDialog(null, "Invalid authentication token," + System.lineSeparator() + "check and change this parameter.", "Configuration error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		catch (final RundeckApiLoginException e) {

			JOptionPane.showMessageDialog(null, "Invalid login/password," + System.lineSeparator() + "check and change these parameters.", "Configuration error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		catch (final Exception e) {

			JOptionPane.showMessageDialog(null, "Invalid project URL," + System.lineSeparator() + "check and change this parameter.", "Configuration error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}

		// Test existing projects
		try {
			if (rundeckClient.getProjects().isEmpty()) {
				JOptionPane.showMessageDialog(null, "Configured rundeck has no project," + System.lineSeparator() + "check and change this rundeck instance.", "Configuration error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return false;
			}
		}
		catch (final RundeckApiException e) {

			JOptionPane.showMessageDialog(null, "Configured rundeck has no project," + System.lineSeparator() + "check and change this rundeck instance.", "Configuration error", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}

		return true;
	}
}
