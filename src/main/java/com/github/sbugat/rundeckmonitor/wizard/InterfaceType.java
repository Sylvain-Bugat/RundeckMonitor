package com.github.sbugat.rundeckmonitor.wizard;

/**
 * Possible value of Java interface type.
 *
 * @author Sylvain Bugat
 *
 */
public enum InterfaceType {

	/** AWT interface for non-Windows system if Swing don't work or if AWT is prefered. */
	AWT("AWT"), //$NON-NLS-1$
	/** Swing interface for Windows system. */
	SWING("Swing"); //$NON-NLS-1$

	/** Displayed String of an interface type. */
	private final String interfaceType;

	/**
	 * Type of a GUI interface.
	 *
	 * @param interfaceTypeArg displayed String of an interface type
	 */
	private InterfaceType(final String interfaceTypeArg) {

		interfaceType = interfaceTypeArg;
	}

	/**
	 * return the String to display for this interface type.
	 *
	 * @return interface type
	 */
	public String getInterfaceType() {
		return interfaceType;
	}
}
