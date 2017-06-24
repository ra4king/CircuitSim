package com.ra4king.circuitsim.simulator.components.wiring;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Transistor extends Component {
	public static final int IN_PORT = 0;
	public static final int GATE_PORT = 1;
	public static final int OUT_PORT = 2;
	
	private static final WireValue X_VALUE = new WireValue(1);
	
	private State enableBit;
	
	public Transistor(String name, boolean isPType) {
		super(name, new int[] { 1, 1, 1 });
		
		enableBit = isPType ? State.ZERO : State.ONE;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == OUT_PORT) {
			return;
		}
		
		if(state.getLastReceived(getPort(GATE_PORT)).getBit(0) == enableBit) {
			state.pushValue(getPort(OUT_PORT), state.getLastReceived(getPort(IN_PORT)));
		} else {
			state.pushValue(getPort(OUT_PORT), X_VALUE);
		}
	}
}
