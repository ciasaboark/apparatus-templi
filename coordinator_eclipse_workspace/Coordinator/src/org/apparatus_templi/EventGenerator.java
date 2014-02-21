package org.apparatus_templi;

import java.util.ArrayList;

public interface EventGenerator {
	abstract public Event generateEvent();
	abstract public ArrayList<Event> getEventTypes();
	
}
