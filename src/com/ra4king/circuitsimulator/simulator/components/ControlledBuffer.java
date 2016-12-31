package com.ra4king.circuitsimulator.simulator.components;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class ControlledBuffer extends Component {
	private final WireValue X_VALUE;
	
	public static final int PORT_IN = 0;
	public static final int PORT_ENABLE = 1;
	public static final int PORT_OUT = 2;
	
	public ControlledBuffer(String name, int bitSize) {
		super(name, new int[] { bitSize, 1, bitSize });
		X_VALUE = new WireValue(bitSize);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == PORT_OUT) return;
		
		if(state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) == State.ONE) {
			state.pushValue(getPort(PORT_OUT), state.getLastReceived(getPort(PORT_IN)));
		} else {
			state.pushValue(getPort(PORT_OUT), X_VALUE);
		}
	}
}
