package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class NandGate extends Gate {
	public NandGate(Circuit circuit, String name, int bitSize, int numInputs) {
		super(circuit, "NAND " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE && bit == State.ONE ? State.ZERO : State.ONE;
	}
}
