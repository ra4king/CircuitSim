package com.ra4king.circuitsim.simulator.components.gates;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public abstract class Gate extends Component {
	private final int bitSize;
	private final int numInputs;
	private final boolean[] negateInputs;
	private final boolean negateOutput;
	
	public Gate(String name, int bitSize, int numInputs) {
		this(name, bitSize, numInputs, false);
	}
	
	public Gate(String name, int bitSize, int numInputs, boolean negateOutput) {
		this(name, bitSize, numInputs, new boolean[numInputs], negateOutput);
	}
	
	public Gate(String name, int bitSize, int numInputs, boolean[] negateInputs, boolean negateOutput) {
		super(name, Utils.getFilledArray(numInputs + 1, bitSize));
		
		if (negateInputs.length != numInputs) {
			throw new IllegalArgumentException("negateInputs array must be the same length as numInputs");
		}
		
		this.bitSize = bitSize;
		this.numInputs = numInputs;
		this.negateInputs = negateInputs;
		this.negateOutput = negateOutput;
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
	
	public boolean[] getNegateInputs() {
		return negateInputs;
	}
	
	public boolean getNegateOutput() {
		return negateOutput;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == numInputs) {
			return;
		}
		
		WireValue result = new WireValue(value.getBitSize());
		for (int bit = 0; bit < result.getBitSize(); bit++) {
			State portBit = state.getLastReceived(getPort(0)).getBit(bit);
			if (negateInputs[0]) {
				portBit = portBit.negate();
			}
			
			result.setBit(bit, portBit);
			boolean isX = result.getBit(bit) == State.Z;
			
			for (int port = 1; port < numInputs; port++) {
				portBit = state.getLastReceived(getPort(port)).getBit(bit);
				if (negateInputs[port]) {
					portBit = portBit.negate();
				}
				
				isX &= portBit == State.Z;
				result.setBit(bit, operate(result.getBit(bit), portBit));
			}
			
			if (isX) {
				result.setBit(bit, State.Z);
			} else if (negateOutput) {
				result.setBit(bit, result.getBit(bit) == State.ONE ? State.ZERO : State.ONE);
			}
		}
		
		state.pushValue(getOutPort(), result);
	}
	
	protected State operate(State acc, State bit) {
		return null;
	}
}
