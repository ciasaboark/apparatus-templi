/*
 * Copyright (C) 2014  Jonathan Nelson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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