package com.ra4king.circuitsimulator.simulator.components.arithmetic;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Negator extends Component {
	public static final int IN_PORT = 0;
	public static final int OUT_PORT = 1;
	
	private final WireValue xValue;
	
	public Negator(String name, int bitSize) {
		super(name, new int[] { bitSize, bitSize });
		
		xValue = new WireValue(bitSize);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == OUT_PORT) {
			return;
		}
		
		WireValue result;
		if(value.isValidValue()) {
			result = WireValue.of(-value.getValue(), value.getBitSize());
		} else {
			result = xValue;
		}
		
		state.pushValue(getPort(OUT_PORT), result);
	}
}
