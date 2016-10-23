package com.ra4king.circuitsimulator.gui;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class DummyCircuitState extends CircuitState {
	public DummyCircuitState(Circuit circuit) {
		super(circuit);
	}
	
	@Override
	public synchronized void pushValue(Port port, WireValue value) {}
	
	@Override
	public WireValue getValue(Link link) {
		return new WireValue(link.getBitSize());
	}
}
