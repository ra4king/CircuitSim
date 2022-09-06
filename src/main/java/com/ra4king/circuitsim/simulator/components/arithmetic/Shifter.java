package com.ra4king.circuitsim.simulator.components.arithmetic;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class Shifter extends Component {
	public static final int PORT_IN = 0;
	public static final int PORT_SHIFT = 1;
	public static final int PORT_OUT = 2;
	
	public enum ShiftType {
		LOGICAL_LEFT, LOGICAL_RIGHT, ARITHMETIC_RIGHT, ROTATE_LEFT, ROTATE_RIGHT
	}
	
	private final int bitSize;
	private final ShiftType shiftType;
	
	public Shifter(String name, int bitSize, ShiftType shiftType) {
		super(name, new int[] { bitSize, getShiftBits(bitSize), bitSize });
		
		this.bitSize = bitSize;
		this.shiftType = shiftType;
	}
	
	private static int getShiftBits(int bitSize) {
		return Math.max(1, (int)Math.ceil(Math.log(bitSize) / Math.log(2)));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_OUT) {
			return;
		}
		
		WireValue valueIn = state.getLastReceived(getPort(PORT_IN));
		WireValue shift = state.getLastReceived(getPort(PORT_SHIFT));
		
		WireValue result = new WireValue(bitSize);
		
		if (shift.isValidValue()) {
			result.setAllBits(State.ZERO);
			
			int shiftValue = shift.getValue();
			
			boolean rotateRight = false;
			switch (shiftType) {
				case ROTATE_LEFT:
					for (int i = shiftValue - 1; i >= 0; i--) {
						result.setBit(i, valueIn.getBit(bitSize - (shiftValue - i)));
					}
				case LOGICAL_LEFT:
					for (int i = bitSize - 1; i >= shiftValue; i--) {
						result.setBit(i, valueIn.getBit(i - shiftValue));
					}
					break;
				case ROTATE_RIGHT:
					rotateRight = true;
					for (int i = bitSize - shiftValue; i < bitSize; i++) {
						result.setBit(i, valueIn.getBit(i - (bitSize - shiftValue)));
					}
				case ARITHMETIC_RIGHT:
					if (!rotateRight) {
						for (int i = bitSize - shiftValue; i < bitSize; i++) {
							result.setBit(i, valueIn.getBit(bitSize - 1));
						}
					}
				case LOGICAL_RIGHT:
					for (int i = 0; i < bitSize - shiftValue; i++) {
						result.setBit(i, valueIn.getBit(i + shiftValue));
					}
					break;
			}
		}
		
		state.pushValue(getPort(PORT_OUT), result);
	}
}
