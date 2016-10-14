package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.utils.Utils;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public abstract class Gate extends Component {
	public Gate(Circuit circuit, String name, int bitSize, int numInputs) {
		super(circuit, name + "(" + bitSize + ")", Utils.getFilledArray(numInputs + 1, bitSize));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == ports.length - 1)
			return;
		
		WireValue result = new WireValue(value.getBitSize());
		for(int bit = 0; bit < result.getBitSize(); bit++) {
			result.setBit(bit, state.getValue(ports[0]).getBit(bit));
			boolean isX = result.getBit(0) == State.X;
			
			for(int port = 1; port < ports.length - 1; port++) {
				State portBit = state.getValue(ports[port]).getBit(bit);
				
				isX &= portBit == State.X;
				result.setBit(bit, operate(result.getBit(bit), portBit));
			}
			
			if(isX) {
				result.setBit(bit, State.X);
			}
		}
		
		state.pushValue(ports[ports.length - 1], result);
	}
	
	protected State operate(State acc, State bit) {
		return null;
	}
}
