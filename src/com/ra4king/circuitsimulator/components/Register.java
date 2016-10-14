package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Register extends Component {
	public static final int PORT_IN = 0;
	public static final int PORT_ENABLE = 1;
	public static final int PORT_CLK = 2;
	public static final int PORT_ZERO = 3;
	public static final int PORT_OUT = 4;
	
	public Register(Circuit circuit, String name, int bitSize) {
		super(circuit, "Register " + name + "(" + bitSize + ")", new int[] { bitSize, 1, 1, 1, bitSize });
	}
	
	@Override
	public void init(CircuitState state) {
		state.pushValue(ports[PORT_OUT], WireValue.of(0, ports[PORT_IN].getLink().getBitSize()));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == PORT_OUT) return;
		
		if(state.getValue(ports[PORT_ZERO]).getBit(0) == State.ONE) {
			state.pushValue(ports[PORT_OUT], WireValue.of(0, ports[PORT_OUT].getLink().getBitSize()));
		} else if(state.getValue(ports[PORT_ENABLE]).getBit(0) != State.ZERO){
			if(portIndex == PORT_CLK && value.getBit(0) == State.ONE) {
				state.pushValue(ports[PORT_OUT], state.getValue(ports[PORT_IN]));
			}
		}
	}
}
