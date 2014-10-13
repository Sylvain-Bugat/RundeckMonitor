package com.github.sbugat.rundeckmonitor.wizard;

/**
 * Possible value of Java interface type
 *
 * @author Sylvain Bugat
 *
 */
public enum InterfaceType {

	/**AWT interface for non-Windows system if Swing don't work or if AWT is prefered*/
	AWT( "AWT" ), //$NON-NLS-1$
	/**Swing interface for Windows system*/
	SWING( "Swing" ); //$NON-NLS-1$

	private final String interfaceType;

	private InterfaceType( final String interfaceTypeArg ) {

		interfaceType = interfaceTypeArg;
	}

	public String getInterfaceType() {
		return interfaceType;
	}
}
