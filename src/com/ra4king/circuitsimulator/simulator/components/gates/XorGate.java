package com.ra4king.circuitsimulator.simulator.components.gates;

import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class XorGate extends Gate {
	public XorGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs, false);
	}
	
	protected XorGate(String name, int bitSize, int numInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateOutput);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc != State.X && bit != State.X && acc != bit ? State.ONE : State.ZERO;
	}
}
