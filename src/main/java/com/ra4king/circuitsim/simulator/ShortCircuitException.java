package com.ra4king.circuitsim.simulator;

/**
 * @author Roi Atalla
 */
public class ShortCircuitException extends SimulationException {
	public final WireValue value1, value2;
	
	public ShortCircuitException(WireValue value1, WireValue value2) {
		super("Short circuit detected! value1 = " + value1 + ", value2 = " + value2);
		
		this.value1 = value1;
		this.value2 = value2;
	}
}
