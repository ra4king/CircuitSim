package com.ra4king.circuitsim.simulator.components.gates;

import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class XorGate extends Gate {
	public XorGate(String name, int bitSize, int numInputs) {
		super(name, bitSize, numInputs, false);
	}
	
	public XorGate(String name, int bitSize, int numInputs, boolean[] negateInputs) {
		super(name, bitSize, numInputs, negateInputs, false);
	}
	
	protected XorGate(String name, int bitSize, int numInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateOutput);
	}
	
	protected XorGate(String name, int bitSize, int numInputs, boolean[] negateInputs, boolean negateOutput) {
		super(name, bitSize, numInputs, negateInputs, negateOutput);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc != State.Z && bit != State.Z && acc != bit ? State.ONE : State.ZERO;
	}
}
