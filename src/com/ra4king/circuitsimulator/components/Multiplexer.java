package com.ra4king.circuitsimulator.components;

import java.util.Arrays;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Multiplexer extends Component {
	public Multiplexer(Circuit circuit, String name, int bitSize, int numSelectBits) {
		super(circuit, "Mux " + name + "(" + numSelectBits + "," + bitSize + ")", createBitSizeArray(bitSize, numSelectBits));
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
		WireValue currentSelect = state.getValue(ports[ports.length - 2]);
		
		// len - 2 == select, len - 1 == output
		if(portIndex == ports.length - 2) {
			if(!value.isValidValue() || !state.getValue(ports[value.getValue()]).isValidValue()) {
				state.pushValue(ports[ports.length - 1], new WireValue(ports[ports.length - 1].getLink().getBitSize()));
			} else {
				state.pushValue(ports[ports.length - 1], state.getValue(ports[value.getValue()]));
			}
		} else if(portIndex < ports.length - 2) {
			if(currentSelect.isValidValue()) {
				if(currentSelect.getValue() == portIndex) {
					state.pushValue(ports[ports.length - 1], value);
				}
			} else {
				state.pushValue(ports[ports.length - 1], new WireValue(value.getBitSize()));
			}
		}
	}
}
