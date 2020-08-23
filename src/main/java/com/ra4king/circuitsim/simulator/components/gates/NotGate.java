package com.ra4king.circuitsim.simulator.components.gates;

/**
 * @author Roi Atalla
 */
public class NotGate extends Gate {
	public NotGate(String name, int bitSize) {
		super(name, bitSize, 1, new boolean[1], true);
	}
}
