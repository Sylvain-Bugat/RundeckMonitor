package com.github.sbugat.rundeckmonitor;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Rundeck Monitor about dialog.
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckMonitorAboutDialog extends JFrame {

	/** SLF4J XLogger. */
	private static final XLogger LOG = XLoggerFactory.getXLogger(RundeckMonitorAboutDialog.class);

	/** Serial UID. */
	private static final long serialVersionUID = 8614361410937565222L;

	/**
	 * Create and display Rundeck Monitor about dialog.
	 */
	public RundeckMonitorAboutDialog() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Try to use the system Look&Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {

			// If System Look&Feel is not supported, stay with the default one
			LOG.warn("Unsupported System Look&Feel", e); //$NON-NLS-1$
		}

		add(new JButton("OK"), BorderLayout.SOUTH); //$NON-NLS-1$

		pack();
		setVisible(true);
	}
}
