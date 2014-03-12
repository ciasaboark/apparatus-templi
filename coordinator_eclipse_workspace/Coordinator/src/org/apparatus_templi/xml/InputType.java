package org.apparatus_templi.xml;

/**
 * A convenience class that contains a list of known valid Button input types.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public final class InputType {
	public static final String TEXT = "text";
	public static final String NUM = "numeric";
	public static final String NONE = "none";

	public static boolean isValidInputType(String inputType) {
		boolean isValid = false;
		if (inputType.equals(TEXT) || inputType.equals(NUM) || inputType.equals(NONE)) {
			isValid = true;
		}
		return isValid;
	}
}