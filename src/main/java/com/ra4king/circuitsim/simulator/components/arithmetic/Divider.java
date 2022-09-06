package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Divider extends Component {
	public static final int PORT_DIVIDEND = 0;
	public static final int PORT_DIVISOR = 1;
	public static final int PORT_QUOTIENT = 2;
	public static final int PORT_REMAINDER = 3;
	
	private final int bitSize;
	
	public Divider(String name, int bitSize) {
		super(name, Utils.getFilledArray(4, bitSize));
		this.bitSize = bitSize;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_QUOTIENT || portIndex == PORT_REMAINDER) {
			return;
		}
		
		if (state.getLastReceived(getPort(PORT_DIVIDEND)).isValidValue() &&
		    state.getLastReceived(getPort(PORT_DIVISOR)).isValidValue()) {
			int a = state.getLastReceived(getPort(PORT_DIVIDEND)).getValue();
			int b = state.getLastReceived(getPort(PORT_DIVISOR)).getValue();
			
			int quotient = b == 0 ? a : a / b;
			int remainder = b == 0 ? 0 : a % b;
			
			state.pushValue(getPort(PORT_QUOTIENT), WireValue.of(quotient, bitSize));
			state.pushValue(getPort(PORT_REMAINDER), WireValue.of(remainder, bitSize));
		} else {
			state.pushValue(getPort(PORT_QUOTIENT), new WireValue(bitSize));
			state.pushValue(getPort(PORT_REMAINDER), new WireValue(bitSize));
		}
	}
}
