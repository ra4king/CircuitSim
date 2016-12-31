package com.ra4king.circuitsimulator.simulator.components;

import java.util.Arrays;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Demultiplexer extends Component {
	public final int NUM_OUT_PORTS;
	public final int PORT_SEL;
	public final int PORT_IN;
	
	public Demultiplexer(String name, int bitSize, int numSelectBits) {
		super(name, createBitSizeArray(bitSize, numSelectBits));
		
		NUM_OUT_PORTS = 1 << numSelectBits;
		PORT_SEL = NUM_OUT_PORTS;
		PORT_IN = PORT_SEL + 1;
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
		if(portIndex == PORT_SEL) {
			if(!value.isValidValue()) {
				for(int i = 0; i < NUM_OUT_PORTS; i++) {
					state.pushValue(getPort(i), new WireValue(value.getBitSize(), State.X));
				}
			} else {
				int selectedPort = value.getValue();
				for(int i = 0; i < NUM_OUT_PORTS; i++) {
					if(i == selectedPort) {
						state.pushValue(getPort(value.getValue()), state.getLastReceived(getPort(getNumPorts() - 1)));
					} else {
						state.pushValue(getPort(value.getValue()), WireValue.of(0, value.getBitSize()));
					}
				}
			}
		} else if(portIndex == PORT_IN && state.getLastReceived(getPort(PORT_SEL)).isValidValue()) {
			int selectedPort = state.getLastReceived(getPort(PORT_SEL)).getValue();
			state.pushValue(getPort(selectedPort), value);
		}
	}
}
