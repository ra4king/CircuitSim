package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class NorGate extends Gate {
	public NorGate(Simulator simulator, String name, int bitSize, int numInputs) {
		super(simulator, "NOR " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE || bit == State.ONE ? State.ZERO : State.ONE;
		
//		boolean isX = true;
//		boolean isZero = false;
//		for(State bit : bits) {
//			isX &= bit == State.X;
//			
//			if(bit == State.ONE) {
//				isZero = true;
//			}
//		}
//		
//		if(isX) {
//			return State.X;
//		} else if(isZero) {
//			return State.ZERO;
//		} else {
//			return State.ONE;
//		}
	}
}
