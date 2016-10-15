package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class NotGate extends Gate {
	public static final int PORT_IN = 0;
	public static final int PORT_OUT = 1;
	
	public NotGate(String name, int bitSize) {
		super("NOT", bitSize, 1);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == PORT_OUT) return;
		
		WireValue result = new WireValue(value.getBitSize());
		for(int i = 0; i < result.getBitSize(); i++) {
			State bit = value.getBit(i);
			result.setBit(i, bit == State.X ? State.X : bit == State.ONE ? State.ZERO : State.ONE);
		}
		
		state.pushValue(getPort(PORT_OUT), result);
	}
}
