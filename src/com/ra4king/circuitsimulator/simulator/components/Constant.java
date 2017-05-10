package com.ra4king.circuitsimulator.simulator.components;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Constant extends Component {
	private final int bitSize;
	private final int value;
	
	public static final int PORT = 0;
	
	public Constant(String name, int bitSize, int value) {
		super(name, new int[] { bitSize });
		this.bitSize = bitSize;
		this.value = value;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		circuitState.pushValue(getPort(PORT), WireValue.of(value, bitSize));
	}
	
	@Override
	public void valueChanged(CircuitState circuitState, WireValue value, int portIndex) {}
}
