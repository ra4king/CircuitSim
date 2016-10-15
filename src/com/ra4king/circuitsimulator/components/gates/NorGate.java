package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class NorGate extends Gate {
	public NorGate(String name, int bitSize, int numInputs) {
		super("NOR " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE || bit == State.ONE ? State.ZERO : State.ONE;
	}
}
