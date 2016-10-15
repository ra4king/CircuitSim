package com.ra4king.circuitsimulator.components;

import java.util.Arrays;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Multiplexer extends Component {
	public Multiplexer(String name, int bitSize, int numSelectBits) {
		super("Mux " + name + "(" + numSelectBits + "," + bitSize + ")", createBitSizeArray(bitSize, numSelectBits));
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
		WireValue currentSelect = state.getValue(getPort(getNumPorts() - 2));
		
		// len - 2 == select, len - 1 == output
		if(portIndex == getNumPorts() - 2) {
			if(!value.isValidValue() || !state.getValue(getPort(value.getValue())).isValidValue()) {
				state.pushValue(getPort(getNumPorts() - 1), new WireValue(getPort(getNumPorts() - 1).getLink().getBitSize()));
			} else {
				state.pushValue(getPort(getNumPorts() - 1), state.getValue(getPort(value.getValue())));
			}
		} else if(portIndex < getNumPorts() - 2) {
			if(currentSelect.isValidValue()) {
				if(currentSelect.getValue() == portIndex) {
					state.pushValue(getPort(getNumPorts() - 1), value);
				}
			} else {
				state.pushValue(getPort(getNumPorts() - 1), new WireValue(value.getBitSize()));
			}
		}
	}
}
