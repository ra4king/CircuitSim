package com.ra4king.circuitsimulator.simulator.components.memory;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.Utils;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class DFlipFlop extends Component {
	public static final int D_PORT = 0;
	public static final int CLOCK_PORT = 1;
	public static final int ENABLE_PORT = 2;
	public static final int PRESET_PORT = 3;
	public static final int CLEAR_PORT = 4;
	public static final int Q_PORT = 5;
	public static final int QN_PORT = 6;
	
	public DFlipFlop(String name) {
		super(name, Utils.getFilledArray(7, 1));
	}
	
	private void pushValue(CircuitState state, State bit) {
		state.putComponentProperty(this, bit);
		state.pushValue(getPort(Q_PORT), new WireValue(1, bit));
		state.pushValue(getPort(QN_PORT), new WireValue(1, bit.negate()));
	}
	
	@Override
	public void init(CircuitState state, Object lastProperty) {
		pushValue(state, lastProperty == null ? State.ZERO : (State)lastProperty);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == Q_PORT || portIndex == QN_PORT) {
			return;
		}
		
		State clear = state.getLastReceived(getPort(CLEAR_PORT)).getBit(0);
		State preset = state.getLastReceived(getPort(PRESET_PORT)).getBit(0);
		State enable = state.getLastReceived(getPort(ENABLE_PORT)).getBit(0);
		
		if(clear == State.ONE) {
			pushValue(state, State.ZERO);
		} else if(preset == State.ONE) {
			pushValue(state, State.ONE);
		} else if(enable != State.ZERO && portIndex == CLOCK_PORT && value.getBit(0) == State.ONE) {
			State d = state.getLastReceived(getPort(D_PORT)).getBit(0);
			if(d != State.X) {
				pushValue(state, d);
			}
		}
	}
}
