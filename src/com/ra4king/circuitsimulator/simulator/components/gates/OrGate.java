package com.ra4king.circuitsimulator.simulator.components.gates;

import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class OrGate extends Gate {
	public OrGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs, false);
	}
	
	protected OrGate(String name, int bitSize, int numInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateOutput);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE || bit == State.ONE ? State.ONE : State.ZERO; 
	}
}
