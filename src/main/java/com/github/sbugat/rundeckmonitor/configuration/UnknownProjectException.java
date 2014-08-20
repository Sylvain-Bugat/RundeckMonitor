package com.github.sbugat.rundeckmonitor.configuration;

/**
 * Exception to use when an unknow project is configured
 *
 * @author Sylvain Bugat
 *
 */
public class UnknownProjectException extends Exception{

	private static final long serialVersionUID = -2516928050086861224L;

	/**Name of the unknown project*/
	private final String projectName;

	public UnknownProjectException( final String projectNameArg ){
		projectName = projectNameArg;
	}

	public String getProjectName() {
		return projectName;
	}
}
