package com.ra4king.circuitsimulator;

/**
 * @author Roi Atalla
 */
public abstract class Component {
	protected final Circuit circuit;
	protected final Port[] ports;
	private String name;
	
	protected Component(Circuit circuit, String name, int[] portBits) {
		this.circuit = circuit;
		this.name = name;
		
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
		return ports[portIndex];
	}
	
	public int getNumPorts() {
		return ports.length;
	}
	
	public void init(CircuitState state) {}
	
	public abstract void valueChanged(CircuitState state, WireValue value, int portIndex);
	
	@Override
	public String toString() {
		return name;
	}
}
