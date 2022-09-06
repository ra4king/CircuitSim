package com.ra4king.circuitsim.simulator.components.wiring;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Austin Adams and Roi Atalla
 */
public class Power extends Component {
	public static final int PORT = 0;

	public Power(String name) {
		super(name, new int[] { 1 });
	}

	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		circuitState.pushValue(getPort(PORT), new WireValue(State.ONE));
	}

	@Override
	public void valueChanged(CircuitState circuitState, WireValue value, int portIndex) {}
}
