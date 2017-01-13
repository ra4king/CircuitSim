package com.ra4king.circuitsimulator.simulator.components;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class RAM extends Component {
	public static final int PORT_ADDRESS = 0;
	public static final int PORT_ENABLE = 1;
	public static final int PORT_CLK = 2;
	public static final int PORT_LOAD = 3;
	public static final int PORT_CLEAR = 4;
	public static final int PORT_DATA = 5;
	
	private final int addressBits;
	private final int dataBits;
	
	public RAM(String name, int bitSize, int addressBits) {
		super(name, new int[] { addressBits, 1, 1, 1, 1, bitSize });
		
		if(addressBits > 16 || addressBits <= 0) {
			throw new IllegalArgumentException("Address bits cannot be more than 16 bits.");
		}
		
		this.addressBits = addressBits;
		this.dataBits = bitSize;
	}
	
	public int getAddressBits() {
		return addressBits;
	}
	
	public int getDataBits() {
		return dataBits;
	}
	
	public void store(CircuitState circuitState, int address, WireValue data) {
		WireValue[] memory = getMemoryContents(circuitState);
		if(memory[address] == null) {
			if(!data.isValidValue() || data.getValue() != 0) {
				memory[address] = new WireValue(data);
			}
		} else if(data.isValidValue() && data.getValue() == 0) {
			memory[address] = null;
		} else {
			memory[address].set(data);
		}
	}
	
	public WireValue load(CircuitState circuitState, int address) {
		WireValue[] memory = getMemoryContents(circuitState);
		WireValue data = memory[address];
		return data == null ? memory[address] = new WireValue(dataBits, State.ZERO) : data;
	}
	
	@Override
	public void init(CircuitState circuitState) {
		super.init(circuitState);
		circuitState.putComponentProperty(this, new WireValue[1 << addressBits]);
	}
	
	public WireValue[] getMemoryContents(CircuitState circuitState) {
		return (WireValue[])circuitState.getComponentProperty(this);
	}
	
	public void setMemoryContents(CircuitState circuitState, WireValue[] newValues) {
		WireValue[] current = getMemoryContents(circuitState);
		if(current.length != newValues.length) {
			throw new IllegalArgumentException("Incorrect WireValue length");
		}
		
		circuitState.putComponentProperty(this, newValues);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		WireValue[] memory = getMemoryContents(state);
		
		boolean enabled = state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) != State.ZERO;
		boolean load = state.getLastReceived(getPort(PORT_LOAD)).getBit(0) != State.ZERO;
		boolean clear = state.getLastReceived(getPort(PORT_CLEAR)).getBit(0) == State.ONE;
		
		WireValue address = state.getLastReceived(getPort(PORT_ADDRESS));
		
		switch(portIndex) {
			case PORT_ENABLE:
			case PORT_LOAD:
				if(!enabled || !load) {
					state.pushValue(getPort(PORT_DATA), new WireValue(dataBits));
				}
			case PORT_ADDRESS:
				if(enabled && load && address.isValidValue()) {
					state.pushValue(getPort(PORT_DATA), load(state, address.getValue()));
				}
				break;
			case PORT_CLK:
				if(!load && value.getBit(0) == State.ONE && address.isValidValue()) {
					store(state, address.getValue(), state.getLastReceived(getPort(PORT_DATA)));
				}
				break;
			case PORT_CLEAR:
				if(clear) {
					for(WireValue wireValue : memory) {
						if(wireValue != null) {
							wireValue.setAllBits(State.ZERO);
						}
					}
				}
				break;
		}
	}
}
