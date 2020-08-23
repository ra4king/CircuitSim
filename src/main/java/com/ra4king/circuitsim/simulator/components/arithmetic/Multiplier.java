package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Multiplier extends Component {
	public static final int PORT_A = 0;
	public static final int PORT_B = 1;
	public static final int PORT_CARRY_IN = 2;
	public static final int PORT_OUT_LOWER = 3;
	public static final int PORT_OUT_UPPER = 4;
	
	private final int bitSize;
	
	public Multiplier(String name, int bitSize) {
		super(name, Utils.getFilledArray(5, bitSize));
		this.bitSize = bitSize;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == PORT_OUT_LOWER || portIndex == PORT_OUT_UPPER) return;
		
		if(state.getLastReceived(getPort(PORT_A)).isValidValue() &&
				   state.getLastReceived(getPort(PORT_B)).isValidValue()) {
			long a = state.getLastReceived(getPort(PORT_A)).getValue() & 0xFFFFFFFFL;
			long b = state.getLastReceived(getPort(PORT_B)).getValue() & 0xFFFFFFFFL;
			WireValue carry = state.getLastReceived(getPort(PORT_CARRY_IN));
			long c = carry.isValidValue() ? carry.getValue() & 0xFFFFFFFFL : 0;
			
			long product = a * b + c;
			int upper = (int)(product >>> bitSize);
			
			state.pushValue(getPort(PORT_OUT_LOWER), WireValue.of((int)product, bitSize));
			state.pushValue(getPort(PORT_OUT_UPPER), WireValue.of(upper, bitSize));
		} else {
			state.pushValue(getPort(PORT_OUT_LOWER), new WireValue(bitSize));
			state.pushValue(getPort(PORT_OUT_UPPER), new WireValue(bitSize));
		}
	}
}
