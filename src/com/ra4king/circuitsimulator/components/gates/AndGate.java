package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class AndGate extends Gate {
	public AndGate(Simulator simulator, String name, int bitSize, int numInputs) {
		super(simulator, "AND " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE && bit == State.ONE ? State.ONE : State.ZERO;
		
//		boolean isX = true;
//		boolean isOne = true;
//		for(State bit : bits) {
//			isX &= bit == State.X;
//			isOne &= bit == State.ONE;
//		}
//		
//		if(isX) {
//			return State.X;
//		} else if(isOne) {
//			return State.ONE;
//		} else {
//			return State.ZERO;
//		}
	}
}
