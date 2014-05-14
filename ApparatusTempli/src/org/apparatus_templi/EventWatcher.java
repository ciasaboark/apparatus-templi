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

/**
 * An interface for Drivers that wish to be notified of Events. A driver that implements this
 * interface will not be automatically notified of all incoming events. Each Driver must register
 * the types of Events that it wishes to watch through
 * {@link Coordinator#registerEventWatch(org.apparatus_templi.driver.Driver, Event)}.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public interface EventWatcher {
	/**
	 * Receive an Event.
	 * 
	 * @param e
	 *            The Event to receive.
	 */
	public void receiveEvent(Event e);
}
