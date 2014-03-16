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
