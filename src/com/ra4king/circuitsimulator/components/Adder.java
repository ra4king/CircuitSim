package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Adder extends Component {
	public static final int PORT_A = 0;
	public static final int PORT_B = 1;
	public static final int PORT_CARRY_IN = 2;
	public static final int PORT_OUT = 3;
	public static final int PORT_CARRY_OUT = 4;
	
	public Adder(Simulator simulator, String name, int bitSize) {
		super(simulator, name, new int[] { bitSize, bitSize, 1, bitSize, 1 });
		properties.put(PropertyType.BITSIZE, bitSize);
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		if(portIndex == PORT_OUT || portIndex == PORT_CARRY_OUT) return;
		
		if(ports[PORT_A].getWireValue().isValidValue() &&
				   ports[PORT_B].getWireValue().isValidValue() &&
				   ports[PORT_CARRY_IN].getWireValue().isValidValue()) {
			WireValue a = ports[PORT_A].getWireValue();
			WireValue b = ports[PORT_B].getWireValue();
			WireValue c = ports[PORT_CARRY_IN].getWireValue();
			
			WireValue sum = new WireValue(a.getBitSize());
			
			State carry = c.getBit(0);
			for(int i = 0; i < sum.getBitSize(); i++) {
				State bitA = a.getBit(i);
				State bitB = b.getBit(i);
				
				sum.setBit(i, bitA == State.ONE ^ bitB == State.ONE ^ carry == State.ONE ? State.ONE : State.ZERO);
				carry = (bitA == State.ONE && bitB == State.ONE) ||
						        (bitA == State.ONE && carry == State.ONE) ||
						        (bitB == State.ONE && carry == State.ONE) ? State.ONE : State.ZERO;
			}
			
			ports[PORT_OUT].pushValue(sum);
			ports[PORT_CARRY_OUT].pushValue(new WireValue(1, carry));
		} else {
			ports[PORT_OUT].pushValue(new WireValue(ports[PORT_OUT].getWireValue().getBitSize()));
			ports[PORT_CARRY_OUT].pushValue(new WireValue(1));
		}
	}
}
