package com.ra4king.circuitsimulator.simulator.components;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Constant extends Pin {
	private final WireValue value;
	
	public static final int PORT_OUT = 0;
	
	public Constant(String name, WireValue value) {
		super(name, value.getBitSize());
		this.value = new WireValue(value);
	}
	
	@Override
	public void setValue(CircuitState circuitState, WireValue value) {
		this.value.set(value);
		circuitState.getCircuit().getCircuitStates().forEach(state -> state.pushValue(getPort(PORT_OUT), value));
	}
	
	@Override
	public void init(CircuitState circuitState) {
		super.init(circuitState);
		circuitState.pushValue(getPort(PORT_OUT), value);
	}
}
