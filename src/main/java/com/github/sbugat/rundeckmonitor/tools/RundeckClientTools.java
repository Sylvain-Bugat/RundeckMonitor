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

	public static RundeckClient buildRundeckClient( final RundeckMonitorConfiguration rundeckMonitorConfiguration ) {

		//Client builder using API token if it is present, otherwise use login and password
		final RundeckClientBuilder rundeckClientBuilder;
		final String rundeckUrl = rundeckMonitorConfiguration.getRundeckUrl();
		if( null != rundeckMonitorConfiguration.getRundeckAPIKey() && ! rundeckMonitorConfiguration.getRundeckAPIKey().isEmpty() ) {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).token( rundeckMonitorConfiguration.getRundeckAPIKey() );
		}
		else {
			rundeckClientBuilder = RundeckClient.builder().url( rundeckUrl ).login( rundeckMonitorConfiguration.getRundeckLogin(), rundeckMonitorConfiguration.getRundeckPassword() );
		}

		//Initialize the rundeck client with the minimal rundeck version (1)
		final RundeckClient rundeckClient = rundeckClientBuilder.version(1).build();

		//Test authentication credentials
		rundeckClient.ping();
		rundeckClient.testAuth();

		//Everything is ok, return the client
		return rundeckClient;
	}
}
