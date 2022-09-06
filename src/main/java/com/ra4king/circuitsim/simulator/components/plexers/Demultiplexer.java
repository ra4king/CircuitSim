package com.ra4king.circuitsim.simulator.components.plexers;

import java.util.Arrays;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Demultiplexer extends Component {
	private final int bitSize;
	private final int numSelectBits;
	private final int numOutputs;
	
	public Demultiplexer(String name, int bitSize, int numSelectBits) {
		super(name, createBitSizeArray(bitSize, numSelectBits));
		
		this.bitSize = bitSize;
		this.numSelectBits = numSelectBits;
		numOutputs = 1 << numSelectBits;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	public int getNumSelectBits() {
		return numSelectBits;
	}
	
	public int getNumOutputs() {
		return numOutputs;
	}
	
	public Port getOutputPort(int index) {
		return getPort(index);
	}
	
	public Port getSelectorPort() {
		return getPort(getNumPorts() - 2);
	}
	
	public Port getInputPort() {
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
		
		if (getPort(portIndex) == selectorPort) {
			if (!value.isValidValue()) {
				for (int i = 0; i < numOutputs; i++) {
					state.pushValue(getOutputPort(i), new WireValue(getBitSize()));
				}
			} else {
				int selectedPort = value.getValue();
				for (int i = 0; i < numOutputs; i++) {
					if (i == selectedPort) {
						state.pushValue(getOutputPort(i), state.getLastReceived(getInputPort()));
					} else {
						state.pushValue(getOutputPort(i), WireValue.of(0, getBitSize()));
					}
				}
			}
		} else if (getPort(portIndex) == getInputPort() && state.getLastReceived(selectorPort).isValidValue()) {
			int selectedPort = state.getLastReceived(selectorPort).getValue();
			state.pushValue(getOutputPort(selectedPort), value);
		}
	}
}
