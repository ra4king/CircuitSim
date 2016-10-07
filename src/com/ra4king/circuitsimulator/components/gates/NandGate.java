package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class NandGate extends Gate {
	public NandGate(Simulator simulator, String name, int bitSize, int numInputs) {
		super(simulator, "NAND " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc == State.ONE && bit == State.ONE ? State.ZERO : State.ONE;
		
//		boolean isX = true;
//		boolean isZero = true;
//		for(State bit : bits) {
//			isX &= bit == State.X;
//			
//			if(bit != State.ONE) {
//				isZero = false;
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
