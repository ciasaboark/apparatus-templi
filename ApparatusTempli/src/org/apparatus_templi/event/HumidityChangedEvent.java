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
 * Generated when the humidty level has changed from the previously recorded value. The originating
 * driver is responsible for determining how much of a change is required before generating a new
 * event.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class HumidityChangedEvent extends Event {
	int humidity;
	int prevHumidity;

	public HumidityChangedEvent() {
		this(0, null, 0, 0);
	}

	public HumidityChangedEvent(long timestamp, Driver origin, int humidity, int prevHumidity) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.humidity = humidity;
		this.prevHumidity = prevHumidity;
	}
}
