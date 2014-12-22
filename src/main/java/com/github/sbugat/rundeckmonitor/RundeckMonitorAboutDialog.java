package com.github.sbugat.rundeckmonitor;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class RundeckMonitorAboutDialog extends JFrame{

	private static final XLogger log = XLoggerFactory.getXLogger( RundeckMonitorAboutDialog.class );

	private static final long serialVersionUID = 8614361410937565222L;

	public RundeckMonitorAboutDialog() {

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		//Try to use the system Look&Feel
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch( final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e ) {

			//If System Look&Feel is not supported, stay with the default one
			log.warn( "Unsupported System Look&Feel", e ); //$NON-NLS-1$
		}

		add( new JButton( "OK"), BorderLayout.SOUTH ); //$NON-NLS-1$

		pack();
		setVisible( true );
	}

	public static void main( String args[] ) {

		new RundeckMonitorAboutDialog();
	}
}
