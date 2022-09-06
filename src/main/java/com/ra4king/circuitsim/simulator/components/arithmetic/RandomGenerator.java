package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class RandomGenerator extends Component {
	public static final int PORT_CLK = 0;
	public static final int PORT_OUT = 1;
	
	private final int bitSize;
	
	public RandomGenerator(String name, int bitSize) {
		super(name, new int[] { 1, bitSize });
		
		this.bitSize = bitSize;
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		circuitState.pushValue(getPort(PORT_OUT), getRandomValue());
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_CLK && value.getBit(0) == State.ONE) {
			state.pushValue(getPort(PORT_OUT), getRandomValue());
		}
	}
	
	private WireValue getRandomValue() {
		return WireValue.of((long)(Math.random() * (1L << bitSize)), bitSize);
	}
}
