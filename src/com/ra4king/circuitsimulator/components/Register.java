package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Register extends Component {
	public static final int PORT_IN = 0;
	public static final int PORT_ENABLE = 1;
	public static final int PORT_CLK = 2;
	public static final int PORT_ZERO = 3;
	public static final int PORT_OUT = 4;
	
	public Register(Simulator simulator, String name, int bitSize) {
		super(simulator, "Register " + name + "(" + bitSize + ")", new int[] { bitSize, 1, 1, 1, bitSize });
		properties.put(PropertyType.BITSIZE, bitSize);
		ports[PORT_OUT].pushValue(WireValue.of(0, bitSize));
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		if(portIndex == PORT_OUT) return;
		
		if(ports[PORT_ZERO].getWireValue().getBit(0) == State.ONE) {
			ports[PORT_OUT].pushValue(WireValue.of(0, (Integer)properties.get(PropertyType.BITSIZE)));
		} else if(ports[PORT_ENABLE].getWireValue().getBit(0) != State.ZERO){
			if(portIndex == PORT_CLK && value.getBit(0) == State.ONE) {
				ports[PORT_OUT].pushValue(ports[PORT_IN].getWireValue());
			}
		}
	}
}
