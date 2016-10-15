package com.ra4king.circuitsimulator.components;

import java.util.Arrays;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Multiplexer extends Component {
	public final int NUM_IN_PORTS;
	public final int PORT_SEL;
	public final int PORT_OUT;
	
	public Multiplexer(String name, int bitSize, int numSelectBits) {
		super("Mux " + name + "(" + numSelectBits + "," + bitSize + ")", createBitSizeArray(bitSize, numSelectBits));
		
		NUM_IN_PORTS = 1 << numSelectBits;
		PORT_SEL = NUM_IN_PORTS;
		PORT_OUT = PORT_SEL + 1;
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
		
		if(portIndex == PORT_SEL) {
			if(!value.isValidValue() || !state.getValue(getPort(value.getValue())).isValidValue()) {
				state.pushValue(getPort(PORT_OUT), new WireValue(getPort(PORT_OUT).getLink().getBitSize()));
			} else {
				state.pushValue(getPort(PORT_OUT), state.getValue(getPort(value.getValue())));
			}
		} else if(portIndex < NUM_IN_PORTS) {
			if(currentSelect.isValidValue()) {
				if(currentSelect.getValue() == portIndex) {
					state.pushValue(getPort(PORT_OUT), value);
				}
			} else {
				state.pushValue(getPort(PORT_OUT), new WireValue(value.getBitSize()));
			}
		}
	}
}
