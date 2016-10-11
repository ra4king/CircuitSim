package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class ControlledBuffer extends Component {
	private final WireValue X_VALUE;
	
	public ControlledBuffer(Simulator simulator, String name, int bitSize) {
		super(simulator, "Controlled Buffer " + name, new int[] { bitSize, 1, bitSize });
		X_VALUE = new WireValue(bitSize);
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		if(portIndex == 2) return;
		
		if(ports[1].getWireValue().getBit(0) == State.ONE) {
			ports[2].pushValue(ports[0].getWireValue());
		} else {
			ports[2].pushValue(X_VALUE);
		}
	}
}
