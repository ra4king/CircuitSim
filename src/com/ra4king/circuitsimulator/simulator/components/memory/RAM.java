package com.ra4king.circuitsimulator.simulator.components.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
	
	private List<BiConsumer<Integer, Integer>> listeners = new ArrayList<>();
	
	public void addMemoryListener(BiConsumer<Integer, Integer> listener) {
		listeners.add(listener);
	}
	
	public void removeMemoryListener(BiConsumer<Integer, Integer> listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(int address, int data) {
		listeners.forEach(listener -> listener.accept(address, data));
	}
	
	public void store(CircuitState state, int address, int data) {
		getMemoryContents(state)[address] = data;
		
		boolean enabled = state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) != State.ZERO;
		boolean load = state.getLastReceived(getPort(PORT_LOAD)).getBit(0) != State.ZERO;
		WireValue addressValue = state.getLastReceived(getPort(PORT_ADDRESS));
		if(enabled && load && addressValue.isValidValue() && addressValue.getValue() == address) {
			state.pushValue(getPort(PORT_DATA), WireValue.of(data, getDataBits()));
		}
		
		notifyListeners(address, data);
	}
	
	public int load(CircuitState circuitState, int address) {
		return getMemoryContents(circuitState)[address];
	}
	
	public int[] getMemoryContents(CircuitState circuitState) {
		return (int[])circuitState.getComponentProperty(this);
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		circuitState.putComponentProperty(this, new int[1 << addressBits]);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		int[] memory = getMemoryContents(state);
		
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
					state.pushValue(getPort(PORT_DATA), WireValue.of(load(state, address.getValue()), getDataBits()));
				}
				break;
			case PORT_CLK:
				if(!load && value.getBit(0) == State.ONE && address.isValidValue()) {
					WireValue lastReceived = state.getLastReceived(getPort(PORT_DATA));
					if(lastReceived.isValidValue()) {
						store(state, address.getValue(), lastReceived.getValue());
					} else {
						store(state, address.getValue(), WireValue.of(-1, getDataBits()).getValue());
					}
				}
				break;
			case PORT_CLEAR:
				if(clear) {
					for(int i = 0; i < memory.length; i++) {
						store(state, i, 0);
					}
				}
				break;
		}
	}
}
