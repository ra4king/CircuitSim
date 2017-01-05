package com.ra4king.circuitsimulator.simulator.components;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Register extends Component {
	public static final int PORT_IN = 0;
	public static final int PORT_ENABLE = 1;
	public static final int PORT_CLK = 2;
	public static final int PORT_ZERO = 3;
	public static final int PORT_OUT = 4;
	
	public Register(String name, int bitSize) {
		super(name, new int[] { bitSize, 1, 1, 1, bitSize });
	}
	
	@Override
	public void init(CircuitState circuitState) {
		super.init(circuitState);
		circuitState.pushValue(getPort(PORT_OUT), WireValue.of(0, getPort(PORT_IN).getLink().getBitSize()));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == PORT_OUT) return;
		
		if(state.getLastReceived(getPort(PORT_ZERO)).getBit(0) == State.ONE) {
			state.pushValue(getPort(PORT_OUT), WireValue.of(0, getPort(PORT_OUT).getLink().getBitSize()));
		} else if(state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) != State.ZERO) {
			if(portIndex == PORT_CLK && value.getBit(0) == State.ONE) {
				WireValue pushValue = state.getLastReceived(getPort(PORT_IN));
				state.pushValue(getPort(PORT_OUT), pushValue);
			}
		}
	}
}
