package com.ra4king.circuitsim.simulator.components.wiring;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Splitter extends Component {
	private final int[] bitFanIndices;
	
	public final int PORT_JOINED;
	
	public Splitter(String name, int bitSize, int fanouts) {
		this(name, setupBitFanIndices(bitSize, fanouts));
	}
	
	public Splitter(String name, int[] bitFanIndices) {
		super(name, setupPortBitsizes(bitFanIndices));
		
		this.bitFanIndices = bitFanIndices;
		
		PORT_JOINED = getNumPorts() - 1;
	}
	
	public int[] getBitFanIndices() {
		return bitFanIndices;
	}
	
	private static int[] setupBitFanIndices(int bitSize, int fanouts) {
		int numBitsPerFan = bitSize / fanouts;
		if ((bitSize % fanouts) != 0) {
			numBitsPerFan++;
		}
		
		int[] bitFanIndices = new int[bitSize];
		for (int i = 0; i < bitSize; i++) {
			bitFanIndices[i] = i / numBitsPerFan;
		}
		
		return bitFanIndices;
	}
	
	private static int[] setupPortBitsizes(int[] bitFanIndices) {
		int totalFans = 0;
		for (int bitFanIdx : bitFanIndices) {
			if (bitFanIdx > totalFans) {
				totalFans = bitFanIdx;
			}
		}
		
		int[] fanouts = new int[totalFans + 2];
		fanouts[totalFans + 1] = bitFanIndices.length;
		
		for (int bitFanIndex : bitFanIndices) {
			if (bitFanIndex >= 0) {
				fanouts[bitFanIndex]++;
			}
		}
		
		return fanouts;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_JOINED) {
			if (bitFanIndices.length != value.getBitSize()) {
				throw new IllegalStateException(
					this + ": something went wrong somewhere. bitFanIndices = " + bitFanIndices.length +
					", value.getBitSize() = " + value.getBitSize());
			}
			
			for (int i = 0; i < getNumPorts() - 1; i++) {
				WireValue result = new WireValue(getPort(i).getLink().getBitSize());
				int currBit = 0;
				for (int j = 0; j < bitFanIndices.length; j++) {
					if (bitFanIndices[j] == i) {
						result.setBit(currBit++, value.getBit(j));
					}
				}
				state.pushValue(getPort(i), result);
			}
		} else {
			WireValue result = new WireValue(state.getLastPushed(getPort(PORT_JOINED)));
			int currBit = 0;
			for (int i = 0; i < bitFanIndices.length; i++) {
				if (bitFanIndices[i] == portIndex) {
					result.setBit(i, value.getBit(currBit++));
				}
			}
			
			if (currBit != value.getBitSize()) {
				throw new IllegalStateException(
					this + ": something went wrong somewhere. currBit = " + currBit + ", value.getBitSize() = " +
					value.getBitSize());
			}
			
			state.pushValue(getPort(PORT_JOINED), result);
		}
	}
}
