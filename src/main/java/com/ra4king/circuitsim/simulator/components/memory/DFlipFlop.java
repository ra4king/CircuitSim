package com.ra4king.circuitsim.simulator.components.memory;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class DFlipFlop extends Component {
	public static final int PORT_D = 0;
	public static final int PORT_CLOCK = 1;
	public static final int PORT_ENABLE = 2;
	public static final int PORT_PRESET = 3;
	public static final int PORT_CLEAR = 4;
	public static final int PORT_Q = 5;
	public static final int PORT_QN = 6;
	
	public DFlipFlop(String name) {
		super(name, Utils.getFilledArray(7, 1));
	}
	
	private void pushValue(CircuitState state, State bit) {
		state.putComponentProperty(this, bit);
		state.pushValue(getPort(PORT_Q), new WireValue(1, bit));
		state.pushValue(getPort(PORT_QN), new WireValue(1, bit.negate()));
	}
	
	@Override
	public void init(CircuitState state, Object lastProperty) {
		pushValue(state, lastProperty == null ? State.ZERO : (State)lastProperty);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_Q || portIndex == PORT_QN) {
			return;
		}
		
		State clear = state.getLastReceived(getPort(PORT_CLEAR)).getBit(0);
		State preset = state.getLastReceived(getPort(PORT_PRESET)).getBit(0);
		State enable = state.getLastReceived(getPort(PORT_ENABLE)).getBit(0);
		
		if (clear == State.ONE) {
			pushValue(state, State.ZERO);
		} else if (preset == State.ONE) {
			pushValue(state, State.ONE);
		} else if (enable != State.ZERO && portIndex == PORT_CLOCK && value.getBit(0) == State.ONE) {
			State d = state.getLastReceived(getPort(PORT_D)).getBit(0);
			if (d != State.Z) {
				pushValue(state, d);
			}
		}
	}
}
