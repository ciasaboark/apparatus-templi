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

package org.apparatus_templi.service;

/**
 * An interface that all services must implement. Provides methods to start and stop the service as
 * well as a way to notify the service that user preferences related to the service may have
 * changed.
 * 
 * @author Jonathan Nelson <ciasaboark@gmail.com>
 * 
 */
public interface ServiceInterface {
	/**
	 * Notify the service that preferences have changed. The service should re-query the saved
	 * preferences for any changes relevant to its environment and adjust behavior as needed.
	 */
	public void preferencesChanged();

	/**
	 * Notify the service that it should restart. The service should save any resources needed and
	 * re-initialize.
	 */
	public void restartService();

	/**
	 * Notify the service that it will be stopped. The service should save any data needed, close
	 * all open connections, and refrain from allocating any new resources.
	 */
	public void stopService();
}
