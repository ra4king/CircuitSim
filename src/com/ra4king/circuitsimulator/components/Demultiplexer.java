package com.ra4king.circuitsimulator.components;

import java.util.Arrays;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Demultiplexer extends Component {
	public Demultiplexer(String name, int bitSize, int numSelectBits) {
		super("Demux " + name + "(" + numSelectBits + "," + bitSize + ")", createBitSizeArray(bitSize, numSelectBits));
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
		// len - 2 == select, len - 1 == input
		if(portIndex == getNumPorts() - 2) {
			if(!value.isValidValue()) {
				for(int i = 0; i < getNumPorts() - 2; i++) {
					state.pushValue(getPort(i), new WireValue(value.getBitSize(), State.X));
				}
			} else {
				int selectedPort = value.getValue();
				for(int i = 0; i < getNumPorts() - 2; i++) {
					if(i == selectedPort) {
						state.pushValue(getPort(value.getValue()), state.getValue(getPort(getNumPorts() - 1)));
					} else {
						state.pushValue(getPort(value.getValue()), WireValue.of(0, value.getBitSize()));
					}
				}
			}
		} else if(portIndex == getNumPorts() - 1 && state.getValue(getPort(getNumPorts() - 2)).isValidValue()) {
			int selectedPort = state.getValue(getPort(getNumPorts() - 2)).getValue();
			state.pushValue(getPort(selectedPort), value);
		}
	}
}
