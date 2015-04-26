package com.github.sbugat.rundeckmonitor.tools;

import org.rundeck.api.RundeckClient;
import org.rundeck.api.RundeckClientBuilder;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.github.sbugat.rundeckmonitor.configuration.RundeckMonitorConfiguration;
import com.github.sbugat.rundeckmonitor.wizard.RundeckAPIVersion;

/**
 * Tools class for building rundeck clients.
 * 
 * @author Sylvain Bugat
 * 
 */
public final class RundeckClientTools {

	/** SLF4J XLogger. */
	private static final XLogger LOG = XLoggerFactory.getXLogger(RundeckClientTools.class);

	/**
	 * Private constructor to prevent Instantiating.
	 */
	private RundeckClientTools() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Build a minimal Rundeck client with the argument configuration to test connection and authentication. The generated Rundeck client use Rundeck API version 1.
	 * 
	 * @param rundeckMonitorConfiguration configuration to use
	 * @return minimal built rundeck client using Rundeck API verison 1
	 */
	public static RundeckClient buildMinimalRundeckClient(final RundeckMonitorConfiguration rundeckMonitorConfiguration) {

		LOG.entry(rundeckMonitorConfiguration);

		final RundeckClient rundeckClient = buildRundeckClient(1, rundeckMonitorConfiguration);

		LOG.exit(rundeckClient);
		return rundeckClient;
	}

	/**
	 * Build a Rundeck client with the argument configuration.
	 * 
	 * @param rundeckMonitorConfiguration configuration to use
	 * @return built rundeck client
	 * @throws InvalidRundeckVersion if the detected Rundeck version don't support API version 5 (before version 1.4.4)
	 */
	public static RundeckClient buildRundeckClient(final RundeckMonitorConfiguration rundeckMonitorConfiguration) throws InvalidRundeckVersion {

		LOG.entry(rundeckMonitorConfiguration);

		final RundeckClient minimalRundeckClient = buildRundeckClient(1, rundeckMonitorConfiguration);
		final String rundeckVersion = minimalRundeckClient.getSystemInfo().getVersion();

		// Get the maximum supported Rundeck API version
		RundeckAPIVersion maximumApiVersion = null;
		for (final RundeckAPIVersion version : RundeckAPIVersion.values()) {

			if (rundeckVersion.compareTo(version.getSinceReturnVersion()) >= 0) {

				if (null == maximumApiVersion) {
					maximumApiVersion = version;
				}
				else if (version.getSinceReturnVersion().compareTo(maximumApiVersion.getSinceReturnVersion()) > 0) {
					maximumApiVersion = version;
				}
			}
		}

		// If none API version can be used with Rundeck because it's before version 1.4.4
		if (null == maximumApiVersion) {
			LOG.error("Invalid Rundeck version {}, Rundeck monitor requires a minimal Rundeck version {}", rundeckVersion, RundeckAPIVersion.RUNDECK_APIVERSION_5.getSinceReturnVersion()); //$NON-NLS-1$
			throw new InvalidRundeckVersion(rundeckVersion);
		}

		// Check the configured Rundeck API version
		final int rundeckAPIversion;
		if (rundeckMonitorConfiguration.getRundeckAPIversion() > maximumApiVersion.getVersion().getVersionNumber()) {
			LOG.warn("Unsupported Rundeck API version {}, use maximum supported Rundeck API version {}", rundeckMonitorConfiguration.getRundeckAPIversion(), maximumApiVersion.getVersion().getVersionNumber()); //$NON-NLS-1$
			rundeckAPIversion = maximumApiVersion.getVersion().getVersionNumber();
		}
		else if (rundeckMonitorConfiguration.getRundeckAPIversion() < RundeckAPIVersion.RUNDECK_APIVERSION_5.getVersion().getVersionNumber()) {
			LOG.warn("Unsupported Rundeck API version {}, use minimum supported Rundeck API version {}", rundeckMonitorConfiguration.getRundeckAPIversion(), RundeckAPIVersion.RUNDECK_APIVERSION_5.getVersion().getVersionNumber()); //$NON-NLS-1$
			rundeckAPIversion = RundeckAPIVersion.RUNDECK_APIVERSION_5.getVersion().getVersionNumber();
		}
		else {
			LOG.debug("Use Rundeck API version {}, maximum supported Rundeck API version {}", rundeckMonitorConfiguration.getRundeckAPIversion(), maximumApiVersion.getVersion().getVersionNumber()); //$NON-NLS-1$
			rundeckAPIversion = rundeckMonitorConfiguration.getRundeckAPIversion();
		}

		final RundeckClient rundeckClient = buildRundeckClient(rundeckAPIversion, rundeckMonitorConfiguration);

		// Everything is ok, return the final Rundeck client
		LOG.exit(rundeckClient);
		return rundeckClient;
	}

	/**
	 * Build a Rundeck client with the API version and configuration arguments.
	 * 
	 * @rundeckAPIVersion Rundeck API version to use
	 * @param rundeckMonitorConfiguration configuration to use
	 * @return built rundeck client
	 */
	public static RundeckClient buildRundeckClient(final int rundeckAPIVersion, final RundeckMonitorConfiguration rundeckMonitorConfiguration) {

		LOG.entry(rundeckAPIVersion, rundeckMonitorConfiguration);

		final String rundeckUrl = rundeckMonitorConfiguration.getRundeckUrl();

		// Client builder using API token if it is present, otherwise use login and password
		final RundeckClientBuilder rundeckClientBuilder;
		if (null != rundeckMonitorConfiguration.getRundeckAPIKey() && !rundeckMonitorConfiguration.getRundeckAPIKey().isEmpty()) {
			LOG.info("Generate a new Rundeck client to {} using API token authentication", rundeckUrl); //$NON-NLS-1$
			rundeckClientBuilder = RundeckClient.builder().url(rundeckUrl).token(rundeckMonitorConfiguration.getRundeckAPIKey());
		}
		else {
			LOG.info("Generate a new Rundeck client to {} using login: {} and password authentication", rundeckUrl, rundeckMonitorConfiguration.getRundeckLogin()); //$NON-NLS-1$
			rundeckClientBuilder = RundeckClient.builder().url(rundeckUrl).login(rundeckMonitorConfiguration.getRundeckLogin(), rundeckMonitorConfiguration.getRundeckPassword());
		}

		// Initialize the rundeck client with the argument Rundeck API version
		LOG.info("Generate a new Rundeck client using rundeck API version {}", rundeckAPIVersion); //$NON-NLS-1$
		final RundeckClient rundeckClient = rundeckClientBuilder.version(rundeckAPIVersion).build();
		// Test connection and authentication credentials
		LOG.debug("Test the connection to Rundeck"); //$NON-NLS-1$
		rundeckClient.ping();
		LOG.debug("Connection to RunDeck OK, test authentication"); //$NON-NLS-1$
		rundeckClient.testAuth();
		final String rundeckVersion = rundeckClient.getSystemInfo().getVersion();
		LOG.info("Connection and authentication OK using API version {} to Rundeck version:{}", rundeckAPIVersion, rundeckVersion); //$NON-NLS-1$

		// Everything is ok, return the client
		LOG.exit(rundeckClient);
		return rundeckClient;
	}
}
