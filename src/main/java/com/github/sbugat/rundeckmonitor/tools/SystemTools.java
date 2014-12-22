package com.github.sbugat.rundeckmonitor.tools;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Tools class to do System class calls
 *
 * @author Sylvain Bugat
 *
 */
public class SystemTools {

	private static final XLogger log = XLoggerFactory.getXLogger( SystemTools.class );

	/**
	 * Normal return code
	 */
	public static final int EXIT_CODE_OK = 0;

	/**
	 * Error return code
	 */
	public static final int EXIT_CODE_ERROR = 1;

	/**
	 * Unsupported tray icon by the system return code
	 */
	public static final int EXIT_CODE_TRAY_ICON_UNSUPPORTED = 2;

	/**
	 * tray icon initialization error return code
	 */
	public static final int EXIT_CODE_TRAY_ICON_ERROR = 3;

	public static void exit( final int returnCode ) {

		log.entry( returnCode );

		if( EXIT_CODE_OK == returnCode ) {
			log.info( "RunDeck Monitor exit with code {}", EXIT_CODE_OK ); //$NON-NLS-1$
		}
		else {
			log.error( "RunDeck Monitor exit with code {}", returnCode ); //$NON-NLS-1$
		}

		//Stop the JVM with the expected return code
		log.exit( returnCode );
		System.exit( returnCode );
	}
}
