package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Adder extends Component {
	public static final int PORT_A = 0;
	public static final int PORT_B = 1;
	public static final int PORT_CARRY_IN = 2;
	public static final int PORT_OUT = 3;
	public static final int PORT_CARRY_OUT = 4;
	
	private final int bitSize;
	
	public Adder(String name, int bitSize) {
		super(name, new int[] { bitSize, bitSize, 1, bitSize, 1 });
		this.bitSize = bitSize;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == PORT_OUT || portIndex == PORT_CARRY_OUT) return;
		
		if(state.getLastReceived(getPort(PORT_A)).isValidValue() &&
				   state.getLastReceived(getPort(PORT_B)).isValidValue()) {
			WireValue a = state.getLastReceived(getPort(PORT_A));
			WireValue b = state.getLastReceived(getPort(PORT_B));
			WireValue c = state.getLastReceived(getPort(PORT_CARRY_IN));
			
			WireValue sum = new WireValue(bitSize);
			
			State carry = c.getBit(0) == State.ONE ? State.ONE : State.ZERO;
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
			state.pushValue(getPort(PORT_OUT), new WireValue(bitSize));
			state.pushValue(getPort(PORT_CARRY_OUT), new WireValue(1));
		}
	}
}
