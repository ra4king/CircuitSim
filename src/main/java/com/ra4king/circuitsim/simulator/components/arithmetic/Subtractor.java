package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Subtractor extends Component {
	public static final int PORT_A = 0;
	public static final int PORT_B = 1;
	public static final int PORT_CARRY_IN = 2;
	public static final int PORT_OUT = 3;
	public static final int PORT_CARRY_OUT = 4;
	
	private final int bitSize;
	
	public Subtractor(String name, int bitSize) {
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
			int a = state.getLastReceived(getPort(PORT_A)).getValue();
			int b = state.getLastReceived(getPort(PORT_B)).getValue();
			WireValue carry = state.getLastReceived(getPort(PORT_CARRY_IN));
			
			int c = carry.getBit(0) == State.ONE ? 1 : 0;
			
			state.pushValue(getPort(PORT_OUT), WireValue.of(a - b - c, bitSize));
			state.pushValue(getPort(PORT_CARRY_OUT), WireValue.of(a - b - c < 0 ? 1 : 0, 1));
		} else {
			state.pushValue(getPort(PORT_OUT), new WireValue(bitSize));
			state.pushValue(getPort(PORT_CARRY_OUT), new WireValue(1));
		}
	}
}
