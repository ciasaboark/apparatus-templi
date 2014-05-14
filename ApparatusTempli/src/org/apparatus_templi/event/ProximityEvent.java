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
 * Generated when a proximity sensor is triggered. The distance recorded is in millimeters. A
 * distance value of -1 indicates that the proximity sensor was not capable of recording a distance
 * value.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class ProximityEvent extends Event {
	private final long distance;

	public ProximityEvent() {
		this(0, null, 0);
	}

	public ProximityEvent(long timestamp, Driver origin, long distance) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.distance = distance;
	}

	public long getDistance() {
		return distance;
	}
}
