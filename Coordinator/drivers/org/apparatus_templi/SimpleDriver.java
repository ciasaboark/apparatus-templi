package org.apparatus_templi;

public class SimpleDriver implements ControllerModule {
	private String moduleName = "SimpleDriver";
	
	@Override
	public String getModuleType() {
		return "Controller";
	}

	@Override
	public String getControllerListXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void tellController(String controllerName, String command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getModuleName() {
		return moduleName;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveCommand(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getWidgetXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullPageXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getControllerStatusXML(String controllerName) {
		// TODO Auto-generated method stub
		return null;
	}

}
