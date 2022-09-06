package com.ra4king.circuitsim.simulator.components.memory;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Register extends Component {
	public static final int PORT_IN = 0;
	public static final int PORT_ENABLE = 1;
	public static final int PORT_CLK = 2;
	public static final int PORT_ZERO = 3;
	public static final int PORT_OUT = 4;
	
	private final int bitSize;
	
	public Register(String name, int bitSize) {
		super(name, new int[] { bitSize, 1, 1, 1, bitSize });
		this.bitSize = bitSize;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		WireValue
			value =
			lastProperty == null ? WireValue.of(0, bitSize) : new WireValue((WireValue)lastProperty, bitSize);
		circuitState.pushValue(getPort(PORT_OUT), value);
		
		circuitState.putComponentProperty(this, value);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_OUT) {
			return;
		}
		
		if (state.getLastReceived(getPort(PORT_ZERO)).getBit(0) == State.ONE) {
			WireValue pushValue = WireValue.of(0, bitSize);
			state.pushValue(getPort(PORT_OUT), pushValue);
			state.putComponentProperty(this, pushValue);
		} else if (state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) != State.ZERO) {
			if (portIndex == PORT_CLK && value.getBit(0) == State.ONE) {
				WireValue pushValue = state.getLastReceived(getPort(PORT_IN));
				state.pushValue(getPort(PORT_OUT), pushValue);
				state.putComponentProperty(this, pushValue);
			}
		}
	}
}
