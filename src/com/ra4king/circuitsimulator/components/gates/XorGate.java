package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class XorGate extends Gate {
	public XorGate(Simulator simulator, String name, int bitSize, int numInputs) {
		super(simulator, "XOR " + name, bitSize, numInputs);
	}
	
	@Override
	protected State operate(State acc, State bit) {
		return acc != State.X && bit != State.X && acc != bit ? State.ONE : State.ZERO;
		
//		boolean isX = true;
//		int oneCount = 0;
//		for(State bit : bits) {
//			isX &= bit == State.X;
//			
//			if(bit == State.ONE) {
//				oneCount++;
//			}
//		}
//		
//		if(isX) {
//			return State.X;
//		} else if((oneCount & 0x1) == 0x1) {
//			return State.ONE;
//		} else {
//			return State.ZERO;
//		}
	}
}
