package com.ra4king.circuitsimulator.simulator.components.gates;

/**
 * @author Roi Atalla
 */
public class NotGate extends Gate {
	public NotGate(String name, int bitSize) {
		super(name, bitSize, 1, true);
	}
}
