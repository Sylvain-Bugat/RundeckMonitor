package org.rundeck.api;

import org.rundeck.api.RundeckApiException;
import org.rundeck.api.RundeckApiException.RundeckApiLoginException;
import org.rundeck.api.RundeckApiException.RundeckApiTokenException;
import org.rundeck.api.RundeckClient;
import org.rundeck.api.domain.RundeckHistory;
import org.rundeck.api.parser.HistoryParser;
import org.rundeck.api.util.AssertUtil;

public class RundeckMonitorClient extends RundeckClient{

	private static final long serialVersionUID = 6744841390473909439L;

	public RundeckMonitorClient(String url, String login, String password)
			throws IllegalArgumentException {
		super(url, login, password);
	}

	@Override
	public RundeckHistory getHistory( final String project, final String statut, final Long max, final Long offset) throws RundeckApiException, RundeckApiLoginException, RundeckApiTokenException, IllegalArgumentException {

		AssertUtil.notBlank(project, "project is mandatory to get the history !"); //$NON-NLS-1$

		return new ApiCall(this).get( new ApiPathBuilder( "/history" ).param( "project", project).param( "statFilter", statut ).param( "max", max ).param("offset", offset), new HistoryParser( "/events" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}
}
