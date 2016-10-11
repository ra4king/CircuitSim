package com.ra4king.circuitsimulator.components;

import java.util.Arrays;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Multiplexer extends Component {
	public Multiplexer(Simulator simulator, String name, int bitSize, int numSelectBits) {
		super(simulator, "Mux " + name + "(" + numSelectBits + "," + bitSize + ")", createBitSizeArray(bitSize, numSelectBits));
		properties.put(PropertyType.BIT_SIZE, bitSize);
		properties.put(PropertyType.MUX_NUM_SELECT_BITS, numSelectBits);
	}
	
	private static int[] createBitSizeArray(int bitSize, int numSelectBits) {
		int[] bitSizes = new int[(1 << numSelectBits) + 2];
		Arrays.fill(bitSizes, 0, 1 << numSelectBits, bitSize);
		bitSizes[bitSizes.length - 2] = numSelectBits;
		bitSizes[bitSizes.length - 1] = bitSize;
		return bitSizes;
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		int bitSize = (Integer)properties.get(PropertyType.BIT_SIZE);
		
		WireValue currentSelect = ports[ports.length - 2].getWireValue();
		
		// len - 2 == select, len - 1 == output
		if(portIndex == ports.length - 2) {
			if(!value.isValidValue() || !ports[value.getValue()].getWireValue().isValidValue()) {
				ports[ports.length - 1].pushValue(new WireValue(bitSize, State.X));
			} else {
				ports[ports.length - 1].pushValue(ports[value.getValue()].getWireValue());
			}
		} else if(portIndex < ports.length - 2) {
			if(currentSelect.isValidValue()) {
				if(currentSelect.getValue() == portIndex) {
					ports[ports.length - 1].pushValue(value);
				}
			} else {
				ports[ports.length - 1].pushValue(new WireValue(value.getBitSize()));
			}
		}
	}
}
