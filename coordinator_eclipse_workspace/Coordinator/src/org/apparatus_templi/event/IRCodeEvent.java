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

package org.apparatus_templi.event;

import org.apparatus_templi.Event;
import org.apparatus_templi.driver.Driver;

/**
 * An Event representing the reception of an infrared signal. The signal code is represented as an
 * array of integer timing values, with the first value representing the first on state. The timings
 * are stored is microseconds. A valid code should have an odd number of timing events, beginning
 * with the length of the first on code, and ending with the length of the last on code.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class IRCodeEvent extends Event {
	private final int[] code;

	public IRCodeEvent() {
		this(0, null, null);
	}

	public IRCodeEvent(long timestamp, Driver origin, int[] code) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.code = code;
	}

	public int[] getCode() {
		return code;
	}
}
