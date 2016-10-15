package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class XnorGate extends Gate {
	public XnorGate(String name, int bitSize, int numInputs) {
		super("XNOR " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc != State.X && bit != State.X && acc == bit ? State.ONE : State.ZERO;
	}
}
