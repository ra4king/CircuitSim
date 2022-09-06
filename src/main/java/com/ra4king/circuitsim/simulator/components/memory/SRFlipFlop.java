package com.ra4king.circuitsim.simulator.components.memory;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class SRFlipFlop extends Component {
	public static final int PORT_S = 0;
	public static final int PORT_R = 1;
	public static final int PORT_CLOCK = 2;
	public static final int PORT_ENABLE = 3;
	public static final int PORT_PRESET = 4;
	public static final int PORT_CLEAR = 5;
	public static final int PORT_Q = 6;
	public static final int PORT_QN = 7;
	
	public SRFlipFlop(String name) {
		super(name, Utils.getFilledArray(8, 1));
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
			State s = state.getLastReceived(getPort(PORT_S)).getBit(0);
			State r = state.getLastReceived(getPort(PORT_R)).getBit(0);
			
			if (s == State.ONE && r == State.ZERO) {
				pushValue(state, State.ONE);
			} else if (r == State.ONE && s == State.ZERO) {
				pushValue(state, State.ZERO);
			}
		}
	}
}
