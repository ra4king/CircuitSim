package com.ra4king.circuitsim.simulator.components.gates;

/**
 * @author Roi Atalla
 */
public class NandGate extends AndGate {
	public NandGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs, true);
	}
	
	public NandGate(String name, int bitSize, int numInputs, boolean[] negateInputs) {
		super(name, bitSize, numInputs, negateInputs, true);
	}
}
