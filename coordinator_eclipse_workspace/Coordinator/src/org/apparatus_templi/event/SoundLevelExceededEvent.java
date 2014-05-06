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
 * An event generated when the recorded sound level exceeds some pre-defined maximum decibel level.
 * The maximum decibel level is defined by the originating driver.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public class SoundLevelExceededEvent extends Event {
	private final int dbLevel;
	private final int maxDbLevel;

	public SoundLevelExceededEvent() {
		this(0, null, 0, 0);
	}

	public SoundLevelExceededEvent(long timestamp, Driver origin, int maxDbLevel, int dbLevel) {
		this.timestamp = timestamp;
		this.origin = origin;
		this.maxDbLevel = maxDbLevel;
		this.dbLevel = dbLevel;
	}

	public int getDbLevel() {
		return dbLevel;
	}

	public int getMaxDbLevel() {
		return maxDbLevel;
	}
}
