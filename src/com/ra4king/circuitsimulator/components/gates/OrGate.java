package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class OrGate extends Gate {
	public OrGate(String name, int bitSize, int numInputs) {
		super("OR " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE || bit == State.ONE ? State.ONE : State.ZERO; 
	}
}
