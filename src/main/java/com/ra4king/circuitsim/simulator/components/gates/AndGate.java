package com.ra4king.circuitsim.simulator.components.gates;

import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class AndGate extends Gate {
	public AndGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs);
	}
	
	public AndGate(String name, int bitSize, int numInputs, boolean[] negateInputs) {
		super(name, bitSize, numInputs, negateInputs, false);
	}
	
	protected AndGate(String name, int bitSize, int numInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateOutput);
	}
	
	protected AndGate(String name, int bitSize, int numInputs, boolean[] negateInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateInputs, negateOutput);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE && bit == State.ONE ? State.ONE : State.ZERO;
	}
}
