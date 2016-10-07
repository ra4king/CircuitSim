package com.ra4king.circuitsimulator;

import java.util.HashMap;

/**
 * @author Roi Atalla
 */
public abstract class Component {
	protected Simulator simulator;
	protected Port[] ports;
	protected HashMap<PropertyType, Object> properties;
	
	protected Component(Simulator simulator, String name, int[] portBits) {
		this.simulator = simulator;
		this.ports = new Port[portBits.length];
		properties = new HashMap<>();
		properties.put(PropertyType.NAME, name);
		
		for(int i = 0; i < portBits.length; i++) {
			ports[i] = new Port(simulator, this, i, portBits[i]);
		}
	}
	
	public Port getPort(int portIndex) {
		return ports[portIndex];
	}
	
	public int getNumPorts() {
		return ports.length;
	}
	
	public abstract void valueChanged(WireValue value, int portIndex);
	
	@Override
	public String toString() {
		return (String)properties.get(PropertyType.NAME);
	}
	
	public enum PropertyType {
		NAME, BITSIZE, IS_INPUT, NUM_SELECT_BITS
	}
}
