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
 * An event generated when the recorded temperature has varied from the previously recorded
 * temperature. The originating driver is responsible for defining how much of a change must be
 * recorded before generating a new event. Temperatures are recorded in degrees Celsius.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class TempChangedEvent extends Event {
	int temp;
	int prevTemp;

	public TempChangedEvent() {
		this(0, null, 0, 0);
	}

	public TempChangedEvent(long timestamp, Driver origin, int prevTemp, int newTemp) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.prevTemp = prevTemp;
		this.temp = newTemp;
	}

	public int getTemp() {
		return temp;
	}

	public int getPrevTemp() {
		return prevTemp;
	}
}
