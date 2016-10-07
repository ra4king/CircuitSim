package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.Utils;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public abstract class Gate extends Component {
	public Gate(Simulator simulator, String name, int bitSize, int numInputs) {
		super(simulator, name + "(" + bitSize + ")", Utils.getFilledArray(numInputs + 1, bitSize));
		properties.put(PropertyType.BITSIZE, bitSize);
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		if(portIndex == ports.length - 1)
			return;
		
		WireValue result = new WireValue(value.getBitSize());
		for(int bit = 0; bit < result.getBitSize(); bit++) {
			result.setBit(bit, ports[0].getWireValue().getBit(bit));
			boolean isX = result.getBit(0) == State.X;
			
			for(int port = 1; port < ports.length - 1; port++) {
				State portBit = ports[port].getWireValue().getBit(bit);
				
				isX &= portBit == State.X;
				result.setBit(bit, operate(result.getBit(bit), portBit));
			}
			
			if(isX) {
				result.setBit(bit, State.X);
			}
		}
		
		ports[ports.length - 1].pushValue(result);
	}
	
	protected State operate(State acc, State bit) {
		return null;
	}
}
