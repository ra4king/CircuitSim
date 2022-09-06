package com.ra4king.circuitsim.simulator.components.plexers;

import java.util.Arrays;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Multiplexer extends Component {
	private final int bitSize;
	private final int numSelectBits;
	private final int numInputs;
	
	public Multiplexer(String name, int bitSize, int numSelectBits) {
		super(name, createBitSizeArray(bitSize, numSelectBits));
		
		this.bitSize = bitSize;
		this.numSelectBits = numSelectBits;
		numInputs = 1 << numSelectBits;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	public int getNumSelectBits() {
		return numSelectBits;
	}
	
	public int getNumInputs() {
		return numInputs;
	}
	
	public Port getInputPort(int index) {
		return getPort(index);
	}
	
	public Port getSelectorPort() {
		return getPort(getNumPorts() - 2);
	}
	
	public Port getOutPort() {
		return getPort(getNumPorts() - 1);
	}
	
	private static int[] createBitSizeArray(int bitSize, int numSelectBits) {
		int[] bitSizes = new int[(1 << numSelectBits) + 2];
		Arrays.fill(bitSizes, 0, 1 << numSelectBits, bitSize);
		bitSizes[bitSizes.length - 2] = numSelectBits;
		bitSizes[bitSizes.length - 1] = bitSize;
		return bitSizes;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		Port selectorPort = getSelectorPort();
		WireValue currentSelect = state.getLastReceived(selectorPort);
		
		if (getPort(portIndex) == selectorPort) {
			if (!value.isValidValue() || !state.getLastReceived(getPort(value.getValue())).isValidValue()) {
				state.pushValue(getOutPort(), new WireValue(getBitSize()));
			} else {
				state.pushValue(getOutPort(), state.getLastReceived(getPort(value.getValue())));
			}
		} else if (portIndex < getNumPorts() - 2) {
			if (currentSelect.isValidValue()) {
				if (currentSelect.getValue() == portIndex) {
					state.pushValue(getOutPort(), value);
				}
			} else {
				state.pushValue(getOutPort(), new WireValue(getBitSize()));
			}
		}
	}
}
