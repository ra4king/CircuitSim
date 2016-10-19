package com.ra4king.circuitsimulator.simulator.components;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Constant extends Component {
	private final WireValue value;
	
	public static final int PORT = 0;
	
	public Constant(String name, WireValue value) {
		super(name, new int[] { 1 });
		this.value = new WireValue(value);
	}
	
	public void setValue(CircuitState circuitState, WireValue value) {
		this.value.set(value);
		circuitState.getCircuit().getCircuitStates().forEach(state -> state.pushValue(getPort(PORT), value));
	}
	
	@Override
	public void init(CircuitState circuitState) {
		circuitState.pushValue(getPort(PORT), value);
	}
	
	@Override
	public void valueChanged(CircuitState circuitState, WireValue value, int portIndex) {}
}
