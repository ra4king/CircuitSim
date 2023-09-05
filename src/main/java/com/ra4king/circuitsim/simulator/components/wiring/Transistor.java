package com.ra4king.circuitsim.simulator.components.wiring;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Transistor extends Component {
	public static final int PORT_IN = 0;
	public static final int PORT_GATE = 1;
	public static final int PORT_OUT = 2;
	
	private static final WireValue Z_VALUE = new WireValue(1);
	
	private State enableBit;
	
	public Transistor(String name, boolean isPType) {
		super(name, new int[] { 1, 1, 1 });
		
		enableBit = isPType ? State.ZERO : State.ONE;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_OUT) {
			return;
		}
		
		if (state.getLastReceived(getPort(PORT_GATE)).getBit(0) == enableBit) {
			state.pushValue(getPort(PORT_OUT), state.getLastReceived(getPort(PORT_IN)));
		} else {
			state.pushValue(getPort(PORT_OUT), Z_VALUE);
		}
	}
}
