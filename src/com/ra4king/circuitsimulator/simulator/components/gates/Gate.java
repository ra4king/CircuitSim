package com.ra4king.circuitsimulator.simulator.components.gates;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.utils.Utils;

/**
 * @author Roi Atalla
 */
public abstract class Gate extends Component {
	private final int bitSize;
	private final int numInputs;
	
	public Gate(String name, int bitSize, int numInputs) {
		super(name, Utils.getFilledArray(numInputs + 1, bitSize));
		
		this.bitSize = bitSize;
		this.numInputs = numInputs;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	public int getNumInputs() {
		return numInputs;
	}
	
	public Port getOutPort() {
		return getPort(numInputs);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(portIndex == numInputs)
			return;
		
		WireValue result = new WireValue(value.getBitSize());
		for(int bit = 0; bit < result.getBitSize(); bit++) {
			result.setBit(bit, state.getLastReceived(getPort(0)).getBit(bit));
			boolean isX = result.getBit(bit) == State.X;
			
			for(int port = 1; port < numInputs; port++) {
				State portBit = state.getLastReceived(getPort(port)).getBit(bit);
				
				isX &= portBit == State.X;
				result.setBit(bit, operate(result.getBit(bit), portBit));
			}
			
			if(isX) {
				result.setBit(bit, State.X);
			}
		}
		
		state.pushValue(getOutPort(), result);
	}
	
	protected State operate(State acc, State bit) {
		return null;
	}
}
