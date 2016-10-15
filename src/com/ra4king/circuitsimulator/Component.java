package com.ra4king.circuitsimulator;

/**
 * @author Roi Atalla
 */
public abstract class Component {
	private Circuit circuit;
	private int[] portBits;
	private Port[] ports;
	private String name;
	
	protected Component(String name, int[] portBits) {
		this.name = name;
		this.portBits = portBits;
	}
	
	public void setCircuit(Circuit circuit) {
		if(this.circuit != null) {
			throw new IllegalStateException(this + ": already part of a circuit.");
		}
		
		this.circuit = circuit;
		
		ports = new Port[portBits.length];
		for(int i = 0; i < portBits.length; i++) {
			ports[i] = new Port(this, i, portBits[i]);
		}
	}
	
	public Circuit getCircuit() {
		return circuit;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Port getPort(int portIndex) {
		if(ports == null) {
			throw new IllegalStateException(this + ": component not part of a circuit.");
		}
		
		return ports[portIndex];
	}
	
	public int getNumPorts() {
		if(ports == null) {
			throw new IllegalStateException(this + ": component not part of a circuit.");
		}
		
		return ports.length;
	}
	
	public void init(CircuitState circuitState) {}
	
	public abstract void valueChanged(CircuitState state, WireValue value, int portIndex);
	
	@Override
	public String toString() {
		return name;
	}
}
