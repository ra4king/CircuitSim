package com.ra4king.circuitsimulator.components.gates;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;
import com.ra4king.circuitsimulator.utils.Utils;

/**
 * @author Roi Atalla
 */
public abstract class Gate extends Component {
	public Gate(String name, int bitSize, int numInputs) {
		super(name + "(" + bitSize + ")", Utils.getFilledArray(numInputs + 1, bitSize));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == getNumPorts() - 1)
			return;
		
		WireValue result = new WireValue(value.getBitSize());
		for(int bit = 0; bit < result.getBitSize(); bit++) {
			result.setBit(bit, state.getValue(getPort(0)).getBit(bit));
			boolean isX = result.getBit(0) == State.X;
			
			for(int port = 1; port < getNumPorts() - 1; port++) {
				State portBit = state.getValue(getPort(port)).getBit(bit);
				
				isX &= portBit == State.X;
				result.setBit(bit, operate(result.getBit(bit), portBit));
			}
			
			if(isX) {
				result.setBit(bit, State.X);
			}
		}
		
		state.pushValue(getPort(getNumPorts() - 1), result);
	}
	
	protected State operate(State acc, State bit) {
		return null;
	}
}
