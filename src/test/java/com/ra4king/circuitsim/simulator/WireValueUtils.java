package com.ra4king.circuitsim.simulator;

import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class WireValueUtils {
	public static boolean allBitsEqualTo(WireValue value, State state) {
		for (int i = 0; i < value.getBitSize(); i++) {
			if (value.getBit(i) != state) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean allBitsEqualTo(WireValue value, State... bits) {
		if (value.getBitSize() != bits.length) {
			throw new IllegalArgumentException("Wrong length for bits");
		}
		
		for (int i = 0; i < value.getBitSize(); i++) {
			if (value.getBit(i) != bits[i]) {
				return false;
			}
		}
		
		return true;
	}
}
