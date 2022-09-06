package com.ra4king.circuitsim.simulator.components.gates;

import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class OrGate extends Gate {
	public OrGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs);
	}
	
	public OrGate(String name, int bitSize, int numInputs, boolean[] negateInputs) {
		super(name, bitSize, numInputs, negateInputs, false);
	}
	
	protected OrGate(String name, int bitSize, int numInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateOutput);
	}
	
	protected OrGate(String name, int bitSize, int numInputs, boolean[] negateInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateInputs, negateOutput);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE || bit == State.ONE ? State.ONE : State.ZERO;
	}
}
