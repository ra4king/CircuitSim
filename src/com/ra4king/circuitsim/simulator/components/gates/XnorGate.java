package com.ra4king.circuitsim.simulator.components.gates;

/**
 * @author Roi Atalla
 */
public class XnorGate extends XorGate {
	public XnorGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs, true);
	}
	
	public XnorGate(String name, int bitSize, int numInputs, boolean[] negateInputs) {
		super(name, bitSize, numInputs, negateInputs, true);
	}
}
