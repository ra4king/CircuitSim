package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.WireValue.State;

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
	
	public RAM(String name, int bitSize, int addressBits) {
		super("RAM " + name + "(" + bitSize + "," + addressBits + ")", new int[] {addressBits, 1, 1, 1, 1, bitSize });
		
		if(addressBits > 16 || addressBits <= 0) {
			throw new IllegalArgumentException("Address bits cannot be more than 16 bits.");
		}
		
		this.addressBits = addressBits;
	}
	
	@Override
	public void init(CircuitState circuitState) {
		super.init(circuitState);
		circuitState.putComponentProperty(this, new WireValue[1 << addressBits]);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		WireValue[] memory = (WireValue[])state.getComponentProperty(this);
		
		boolean enabled = state.getValue(getPort(PORT_ENABLE)).getBit(0) != State.ZERO;
		boolean load = state.getValue(getPort(PORT_LOAD)).getBit(0) != State.ZERO;
		boolean clear = state.getValue(getPort(PORT_CLEAR)).getBit(0) == State.ONE;
		
		WireValue address = state.getValue(getPort(PORT_ADDRESS));
		
		switch(portIndex) {
			case PORT_ENABLE:
			case PORT_LOAD:
				if(!enabled || !load) {
					state.pushValue(getPort(PORT_DATA), new WireValue(memory[0].getBitSize()));
				}
			case PORT_ADDRESS:
				if(enabled && load && address.isValidValue()) {
					state.pushValue(getPort(PORT_DATA), memory[address.getValue()]);
				}
				break;
			case PORT_CLK:
				if(value.getBit(0) == State.ONE && address.isValidValue()) {
					memory[address.getValue()].set(state.getValue(getPort(PORT_DATA)));
				}
				break;
			case PORT_CLEAR:
				if(clear) {
					for(WireValue wireValue : memory) {
						wireValue.setAllBits(State.ZERO);
					}
				}
				break;
		}
	}
}
