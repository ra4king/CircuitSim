package com.ra4king.circuitsimulator.simulator.components;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Adder extends Component {
	public static final int PORT_A = 0;
	public static final int PORT_B = 1;
	public static final int PORT_CARRY_IN = 2;
	public static final int PORT_OUT = 3;
	public static final int PORT_CARRY_OUT = 4;
	
	public Adder(String name, int bitSize) {
		super(name, new int[] { bitSize, bitSize, 1, bitSize, 1 });
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == PORT_OUT || portIndex == PORT_CARRY_OUT) return;
		
		if(state.getValue(getPort(PORT_A)).isValidValue() &&
				   state.getValue(getPort(PORT_B)).isValidValue() &&
				   state.getValue(getPort(PORT_CARRY_IN)).isValidValue()) {
			WireValue a = state.getValue(getPort(PORT_A));
			WireValue b = state.getValue(getPort(PORT_B));
			WireValue c = state.getValue(getPort(PORT_CARRY_IN));
			
			WireValue sum = new WireValue(a.getBitSize());
			
			State carry = c.getBit(0);
			for(int i = 0; i < sum.getBitSize(); i++) {
				State bitA = a.getBit(i);
				State bitB = b.getBit(i);
				
				sum.setBit(i, bitA == State.ONE ^ bitB == State.ONE ^ carry == State.ONE ? State.ONE : State.ZERO);
				carry = (bitA == State.ONE && bitB == State.ONE) ||
						        (bitA == State.ONE && carry == State.ONE) ||
						        (bitB == State.ONE && carry == State.ONE) ? State.ONE : State.ZERO;
			}
			
			state.pushValue(getPort(PORT_OUT), sum);
			state.pushValue(getPort(PORT_CARRY_OUT), new WireValue(1, carry));
		} else {
			WireValue value1 = new WireValue(state.getValue(getPort(PORT_OUT)).getBitSize());
			state.pushValue(getPort(PORT_OUT), value1);
			state.pushValue(getPort(PORT_CARRY_OUT), new WireValue(1));
		}
	}
}
