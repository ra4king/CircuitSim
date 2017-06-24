package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Comparator extends Component {
	public static final int PORT_A = 0;
	public static final int PORT_B = 1;
	public static final int PORT_LT = 2;
	public static final int PORT_EQ = 3;
	public static final int PORT_GT = 4;
	
	public Comparator(String name, int bitSize) {
		super(name, new int[] { bitSize, bitSize, 1, 1, 1 });
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		WireValue inputA = state.getLastReceived(getPort(PORT_A));
		WireValue inputB = state.getLastReceived(getPort(PORT_B));
		
		if(inputA.isValidValue() && inputB.isValidValue()) {
			int valueA = inputA.getValue();
			int valueB = inputB.getValue();
			state.pushValue(getPort(PORT_LT), new WireValue(1, valueA < valueB ? State.ONE : State.ZERO));
			state.pushValue(getPort(PORT_EQ), new WireValue(1, valueA == valueB ? State.ONE : State.ZERO));
			state.pushValue(getPort(PORT_GT), new WireValue(1, valueA > valueB ? State.ONE : State.ZERO));
		} else {
			WireValue xValue = new WireValue(1, State.X);
			state.pushValue(getPort(PORT_LT), xValue);
			state.pushValue(getPort(PORT_EQ), xValue);
			state.pushValue(getPort(PORT_GT), xValue);
		}
	}
}
