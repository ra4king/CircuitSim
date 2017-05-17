package com.ra4king.circuitsimulator.simulator.components.gates;

/**
 * @author Roi Atalla
 */
public class NorGate extends OrGate {
	public NorGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs, true);
	}
}
