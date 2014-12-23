package com.github.sbugat.rundeckmonitor.tools;

import org.rundeck.api.RundeckClient;
import org.rundeck.api.RundeckClientBuilder;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;

/**
 * Tools class for building rundeck clients
 *
 * @author Sylvain Bugat
 *
 */
public class RundeckClientTools {

	/**
	 * Build a rundeck client with the argument configuration
	 *
	 * @param rundeckMonitorConfiguration configuration to use
	 * @param minimalRundeckAPIVersion use API version 1 if true
	 * @return build rundeck client
	 */
	public static RundeckClient buildRundeckClient( final RundeckMonitorConfiguration rundeckMonitorConfiguration, final boolean minimalRundeckAPIVersion ) {

		final String rundeckUrl = rundeckMonitorConfiguration.getRundeckUrl();

		//Client builder using API token if it is present, otherwise use login and password
		final RundeckClientBuilder rundeckClientBuilder;
		if( null != rundeckMonitorConfiguration.getRundeckAPIKey() && ! rundeckMonitorConfiguration.getRundeckAPIKey().isEmpty() ) {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckMonitorConfiguration.getRundeckAPIKey() );
		}
		else {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckMonitorConfiguration.getRundeckLogin(), rundeckMonitorConfiguration.getRundeckPassword() );
		}

		final RundeckClient rundeckClient;
		if( minimalRundeckAPIVersion ) {
			//Initialize the rundeck client with the minimal rundeck version (1)
			rundeckClient = rundeckClientBuilder.version( 1 ).build();
		}
		else {
			//Use the configured RunDeck API version
			rundeckClient = rundeckClientBuilder.version( rundeckMonitorConfiguration.getRundeckAPIversion() ).build();
		}

		//Test authentication credentials
		rundeckClient.ping();
		rundeckClient.testAuth();

		//Everything is ok, return the client
		return rundeckClient;
	}
}
