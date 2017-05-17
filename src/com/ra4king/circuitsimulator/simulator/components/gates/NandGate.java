package com.ra4king.circuitsimulator.simulator.components.gates;

/**
 * @author Roi Atalla
 */
public class NandGate extends AndGate {
	public NandGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs, true);
	}
}
