package com.ra4king.circuitsim.simulator.components.plexers;

import java.util.Arrays;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Decoder extends Component {
	private final int numSelectBits;
	private final int numOutputs;
	
	public Decoder(String name, int numSelectBits) {
		super(name, createBitSizeArray(numSelectBits));
		
		this.numSelectBits = numSelectBits;
		numOutputs = 1 << numSelectBits;
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
		return getPort(getNumPorts() - 1);
	}
	
	private static int[] createBitSizeArray(int numSelectBits) {
		int[] bitSizes = new int[(1 << numSelectBits) + 1];
		Arrays.fill(bitSizes, 0, 1 << numSelectBits, 1);
		bitSizes[bitSizes.length - 1] = numSelectBits;
		return bitSizes;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		Port selectorPort = getSelectorPort();
		
		if (getPort(portIndex) == selectorPort) {
			if (!value.isValidValue()) {
				for (int i = 0; i < numOutputs; i++) {
					state.pushValue(getOutputPort(i), new WireValue(1));
				}
			} else {
				int selectedPort = value.getValue();
				for (int i = 0; i < numOutputs; i++) {
					if (i == selectedPort) {
						state.pushValue(getOutputPort(i), WireValue.of(1, 1));
					} else {
						state.pushValue(getOutputPort(i), WireValue.of(0, 1));
					}
				}
			}
		}
	}
}
