package org.apparatus_templi;
// Kim -testing commit from Eclipse'

public interface Driver extends Runnable {

	public String getModuleType();
	public String getModuleName();
	public void receiveCommand(String command);
	public void terminate();
	
	public String getWidgetXML();
	public String getFullPageXML();
}
