package com.ra4king.circuitsimulator;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Roi Atalla
 */
public class Circuit extends Component {
	private HashSet<Component> components;
	
	public Circuit(Simulator simulator, String name) {
		super(simulator, name, new int[0]);
		components = new HashSet<>();
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		if(components.isEmpty())
			return;
		
		Iterator<Component> iter = components.iterator();
		Component curr = iter.next();
		while(portIndex >= curr.ports.length) {
			portIndex -= curr.ports.length;
			
			if(!iter.hasNext()) {
				return;
			}
			
			curr = iter.next();
		}
		
		curr.valueChanged(value, portIndex);
	}
	
	public void addComponent(Component component) {
		components.add(component);
	}
	
	public void removeComponent(Component component) {
		components.remove(component);
	}
}
