package com.ra4king.circuitsim.simulator.components.memory;

import java.util.Arrays;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class ROM extends Component {
	public static final int PORT_ADDRESS = 0;
	public static final int PORT_ENABLE = 1;
	public static final int PORT_DATA = 2;
	
	private final int addressBits;
	private final int dataBits;
	private final int[] memory;
	
	public ROM(String name, int bitSize, int addressBits, int[] memory) {
		super(name, new int[] { addressBits, 1, bitSize });
		
		if(addressBits > 16 || addressBits <= 0) {
			throw new IllegalArgumentException("Address bits cannot be more than 16 bits.");
		}
		
		this.addressBits = addressBits;
		this.dataBits = bitSize;
		this.memory = Arrays.copyOf(memory, 1 << addressBits);
	}
	
	public int getAddressBits() {
		return addressBits;
	}
	
	public int getDataBits() {
		return dataBits;
	}
	
	public int[] getMemory() {
		return memory;
	}
	
	public WireValue load(int address) {
		if(address < 0 || address >= memory.length) {
			return null;
		}
		
		return WireValue.of(memory[address], dataBits);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		boolean enabled = state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) != State.ZERO;
		WireValue address = state.getLastReceived(getPort(PORT_ADDRESS));
		
		if(enabled && address.isValidValue()) {
			state.pushValue(getPort(PORT_DATA), load(address.getValue()));
		} else {
			state.pushValue(getPort(PORT_DATA), new WireValue(dataBits));
		}
	}
}
