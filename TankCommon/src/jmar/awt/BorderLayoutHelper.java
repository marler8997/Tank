package jmar.awt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

public class BorderLayoutHelper {
	public static void set(Container container, Component component, String location) {
		Component oldComponent = ((BorderLayout)container.getLayout()).getLayoutComponent(location);
		if(oldComponent != null) container.remove(oldComponent);
		container.add(component, location);
	}
	
	public static void remove(Container container, String location) {
		Component oldComponent = ((BorderLayout)container.getLayout()).getLayoutComponent(location);
		if(oldComponent != null) container.remove(oldComponent);
	}
}
