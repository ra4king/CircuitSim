package com.ra4king.circuitsimulator;

/**
 * @author Roi Atalla
 */
public abstract class Component {
	protected Simulator simulator;
	protected Port[] ports;
	private String name;
	
	protected Component(Simulator simulator, String name, int[] portBits) {
		this.simulator = simulator;
		this.name = name;
		
		ports = new Port[portBits.length];
		for(int i = 0; i < portBits.length; i++) {
			ports[i] = new Port(simulator, this, i, portBits[i]);
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
		return name;
	}
}
