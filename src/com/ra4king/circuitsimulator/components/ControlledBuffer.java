package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class ControlledBuffer extends Component {
	private final WireValue X_VALUE;
	
	public ControlledBuffer(Circuit circuit, String name, int bitSize) {
		super(circuit, "Controlled Buffer " + name, new int[] { bitSize, 1, bitSize });
		X_VALUE = new WireValue(bitSize);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == 2) return;
		
		if(state.getValue(ports[1]).getBit(0) == State.ONE) {
			state.pushValue(ports[2], state.getValue(ports[0]));
		} else {
			state.pushValue(ports[2], X_VALUE);
		}
	}
}
