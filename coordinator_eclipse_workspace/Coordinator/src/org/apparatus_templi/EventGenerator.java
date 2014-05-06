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

package org.apparatus_templi;

import java.util.ArrayList;

/**
 * An interface for all Drivers that can generate events.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public interface EventGenerator {
	// abstract public Event generateEvent();
	/**
	 * Every EventGenerator is expected to keep an internal list of all Event types that it can
	 * generate.
	 * 
	 * @return an ArrayList of Events that this Driver can generate.
	 * @deprecated this method will be removed in a future release. It has no particular use.
	 */
	@Deprecated
	abstract public ArrayList<Event> getEventTypes();

}
