package com.ra4king.circuitsim.simulator.components.plexers;

import java.util.Arrays;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Elliott Childre
 */
public class PriorityEncoder extends Component {
	private final int numSelectBits;
	private boolean isEnabled;
	
	public PriorityEncoder(String name, int numSelectBits) {
		super(name, createBitSizeArray(numSelectBits));
		this.numSelectBits = numSelectBits;
		this.isEnabled = false;
	}
	
	private static int[] createBitSizeArray(int numSelectBits) {
		// ports = [2^numSelectBits, enableIN, enableOUT, outGroup, out]
		int[] portBits = new int[(1 << numSelectBits) + 4];
		
		// all wires are width: 1 except out
		Arrays.fill(portBits, 0, (1 << numSelectBits) + 3, 1);
		portBits[portBits.length - 1] = numSelectBits;
		
		return portBits;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		Port out = getOutputPort();
		// if enabled IN changes
		if (portIndex == 1 << numSelectBits) {
			this.isEnabled = value.getBit(0) == State.ONE;
		}
		// The only other input Port are the indexed inputs
		if (!this.isEnabled) {
			state.pushValue(getEnabledOutPort(), new WireValue(1, State.ZERO));
			state.pushValue(out, new WireValue(out.getLink().getBitSize(), State.Z));
			state.pushValue(getGroupSignalPort(), new WireValue(1, State.ZERO));
			return;
		}
		
		// Loop through the inputs
		int highest = -1;
		int ports = 1 << numSelectBits;
		for (int i = 0; i < ports; i++) {
			if (state.getLastReceived(getPort(i)).getBit(0) == State.ONE ||
			    (i == portIndex && value.getBit(0) == State.ONE)) {
				highest = i;
			}
		}
		
		if (highest == -1) {
			state.pushValue(getEnabledOutPort(), new WireValue(1, State.ONE));
			state.pushValue(out, new WireValue(out.getLink().getBitSize(), State.Z));
			state.pushValue(getGroupSignalPort(), new WireValue(1, State.ZERO));
			
		} else {
			state.pushValue(getEnabledOutPort(), new WireValue(1, State.ZERO));
			state.pushValue(getGroupSignalPort(), new WireValue(1, State.ONE));
			state.pushValue(getOutputPort(), WireValue.of(highest, out.getLink().getBitSize()));
		}
	}
	
	public int getNumSelectBits() {
		return numSelectBits;
	}
	
	public Port getEnabledInPort() {
		return getPort(1 << numSelectBits);
	}
	
	public Port getEnabledOutPort() {
		return getPort((1 << numSelectBits) + 1);
	}
	
	public Port getGroupSignalPort() {
		return getPort((1 << numSelectBits) + 2);
	}
	
	public Port getOutputPort() {
		return getPort((1 << numSelectBits) + 3);
	}
}
