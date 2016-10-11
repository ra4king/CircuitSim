package com.ra4king.circuitsimulator.components;

import java.util.Arrays;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Demultiplexer extends Component {
	public Demultiplexer(Simulator simulator, String name, int bitSize, int numSelectBits) {
		super(simulator, "Demux " + name + "(" + numSelectBits + "," + bitSize + ")", createBitSizeArray(bitSize, numSelectBits));
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
		// len - 2 == select, len - 1 == input
		if(portIndex == ports.length - 2) {
			if(!value.isValidValue()) {
				for(int i = 0; i < ports.length - 2; i++) {
					ports[i].pushValue(new WireValue(value.getBitSize(), State.X));
				}
			} else {
				int selectedPort = value.getValue();
				for(int i = 0; i < ports.length - 2; i++) {
					if(i == selectedPort) {
						ports[value.getValue()].pushValue(ports[ports.length - 1].getWireValue());
					} else {
						ports[value.getValue()].pushValue(WireValue.of(0, value.getBitSize()));
					}
				}
			}
		} else if(portIndex == ports.length - 1 && ports[ports.length - 2].getWireValue().isValidValue()) {
			int selectedPort = ports[ports.length - 2].getWireValue().getValue();
			ports[selectedPort].pushValue(value);
		}
	}
}
