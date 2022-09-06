package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Negator extends Component {
	public static final int PORT_IN = 0;
	public static final int PORT_OUT = 1;
	
	private final WireValue xValue;
	
	public Negator(String name, int bitSize) {
		super(name, new int[] { bitSize, bitSize });
		
		xValue = new WireValue(bitSize);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_OUT) {
			return;
		}
		
		WireValue result;
		if (value.isValidValue()) {
			result = WireValue.of(-value.getValue(), value.getBitSize());
		} else {
			result = xValue;
		}
		
		state.pushValue(getPort(PORT_OUT), result);
	}
}
