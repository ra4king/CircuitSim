package com.ra4king.circuitsim.simulator;

/**
 * @author Roi Atalla
 */
public abstract class Component {
	private Circuit circuit;
	private final Port[] ports;
	private String name;
	
	protected Component(String name, int[] portBits) {
		this.name = name;
		
		ports = new Port[portBits.length];
		for (int i = 0; i < portBits.length; i++) {
			ports[i] = new Port(this, i, portBits[i]);
		}
	}
	
	public void setCircuit(Circuit circuit) {
		this.circuit = circuit;
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
		return ports[portIndex];
	}
	
	public int getNumPorts() {
		return ports.length;
	}
	
	public void init(CircuitState circuitState, Object lastProperty) {}
	
	public void uninit(CircuitState circuitState) {}
	
	public abstract void valueChanged(CircuitState state, WireValue value, int portIndex);
	
	@Override
	public String toString() {
		return name.isEmpty() ? getClass().getName() + "@" + Integer.toHexString(hashCode()) : name;
	}
}
