package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Splitter extends Component {
	private final int[] bitFanIndices;
	
	public Splitter(String name, int bitSize, int fanouts) {
		this(name, setupBitFanIndices(bitSize, fanouts));
	}
	
	public Splitter(String name, int[] bitFanIndices) {
		super("Splitter " + name, setupPortBitsizes(bitFanIndices));
		
		this.bitFanIndices = bitFanIndices;
	}
	
	private static int[] setupBitFanIndices(int bitSize, int fanouts) {
		int numBitsPerFan = bitSize / fanouts;
		if((bitSize % fanouts) != 0)
			numBitsPerFan++;
		
		int[] bitFanIndices = new int[bitSize];
		for(int i = 0; i < bitSize; i++) {
			bitFanIndices[i] = i / numBitsPerFan;
		}
		
		return bitFanIndices;
	}
	
	private static int[] setupPortBitsizes(int[] bitFanIndices) {
		int totalFans = 0;
		for(int bitFanIdx : bitFanIndices) {
			if(bitFanIdx > totalFans)
				totalFans = bitFanIdx;
		}
		
		if(totalFans == 0) {
			throw new IllegalArgumentException("Must have at least one bit going to a fanout.");
		}
		
		int[] fanouts = new int[totalFans + 2];
		fanouts[totalFans + 1] = bitFanIndices.length;
		
		for(int bitFanIndex : bitFanIndices) {
			if(bitFanIndex >= 0)
				fanouts[bitFanIndex]++;
		}
		
		return fanouts;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if(bitFanIndices.length != value.getBitSize()) {
			throw new IllegalStateException(this + ": something went wrong somewhere. bitFanIndices = " + bitFanIndices.length + ", value.getBitSize() = " + value.getBitSize());
		}
		
		if(portIndex == getNumPorts() - 1) {
			for(int i = 0; i < getNumPorts() - 1; i++) {
				WireValue result = new WireValue(getPort(i).getLink().getBitSize());
				int currBit = 0;
				for(int j = 0; j < bitFanIndices.length; j++) {
					if(bitFanIndices[j] == i) {
						result.setBit(currBit++, value.getBit(j));
					}
				}
				state.pushValue(getPort(i), result);
			}
		} else {
			WireValue result = new WireValue(state.getValue(getPort(getNumPorts() - 1)));
			int currBit = 0;
			for(int i = 0; i < bitFanIndices.length; i++) {
				if(bitFanIndices[i] == portIndex) {
					result.setBit(i, value.getBit(currBit++));
				}
			}
			state.pushValue(getPort(getNumPorts() - 1), result);
		}
	}
}
