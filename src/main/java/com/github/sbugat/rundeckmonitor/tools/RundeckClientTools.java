package com.github.sbugat.rundeckmonitor.tools;

import org.rundeck.api.RundeckClient;
import org.rundeck.api.RundeckClientBuilder;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

/**
 * Tools class for building rundeck clients
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckClientTools {

	private static final XLogger log = XLoggerFactory.getXLogger( RundeckClientTools.class );

	/**
	 * Build a rundeck client with the argument configuration
	 *
	 * @param rundeckMonitorConfiguration configuration to use
	 * @param minimalRundeckAPIVersion use API version 1 if true
	 * @return build rundeck client
	 */
	public static RundeckClient buildRundeckClient( final RundeckMonitorConfiguration rundeckMonitorConfiguration, final boolean minimalRundeckAPIVersion ) {

		log.entry( rundeckMonitorConfiguration, minimalRundeckAPIVersion );

		final String rundeckUrl = rundeckMonitorConfiguration.getRundeckUrl();

		//Client builder using API token if it is present, otherwise use login and password
		final RundeckClientBuilder rundeckClientBuilder;
		if( null != rundeckMonitorConfiguration.getRundeckAPIKey() && ! rundeckMonitorConfiguration.getRundeckAPIKey().isEmpty() ) {
			log.info( "Generate a new RunDeck client to {} using API token authentication", rundeckUrl ); //$NON-NLS-1$
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckMonitorConfiguration.getRundeckAPIKey() );
		}
		else {
			log.info( "Generate a new RunDeck client to {} using login: {} and password authentication", rundeckUrl, rundeckMonitorConfiguration.getRundeckLogin() ); //$NON-NLS-1$
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckMonitorConfiguration.getRundeckLogin(), rundeckMonitorConfiguration.getRundeckPassword() );
		}

		final RundeckClient rundeckClient;
		if( minimalRundeckAPIVersion ) {
			log.info( "Generate a new RunDeck client using API v1 for compatibility issue" ); //$NON-NLS-1$
			//Initialize the rundeck client with the minimal rundeck version (1)
			rundeckClient = rundeckClientBuilder.version( 1 ).build();
		}
		else {
			log.info( "Generate a new RunDeck client using API v{}", rundeckMonitorConfiguration.getRundeckAPIversion() ); //$NON-NLS-1$
			//Use the configured RunDeck API version
			rundeckClient = rundeckClientBuilder.version( rundeckMonitorConfiguration.getRundeckAPIversion() ).build();
		}

		//Test connection and authentication credentials
		log.debug( "Test the connection to RunDeck" ); //$NON-NLS-1$
		rundeckClient.ping();
		log.debug( "Connection  to RunDeck OK, test authentication" ); //$NON-NLS-1$
		rundeckClient.testAuth();
		log.debug( "Connection and authentication OK" ); //$NON-NLS-1$

		//Everything is ok, return the client
		log.exit( rundeckClient );
		return rundeckClient;
	}
}
