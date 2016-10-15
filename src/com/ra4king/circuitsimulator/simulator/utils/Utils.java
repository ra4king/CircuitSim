package com.ra4king.circuitsimulator.simulator.utils;

import java.util.Arrays;

import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Utils {
	public static void ensureBitSize(Object source, WireValue value, int requiredBitSize) {
		if(value.getBitSize() != requiredBitSize) {
			throw new IllegalStateException(source + ": different size wires detected: wanted "
			                                + requiredBitSize + ", found " + value.getBitSize());
		}
	}
	
	public static void ensureCompatible(Object source, WireValue value1, WireValue value2) {
		ensureBitSize(source, value2, value1.getBitSize());
		
		if(!value1.isCompatible(value2)) {
			throw new IllegalStateException(source + ": short circuit detected! value1 = "
					                                + value1 + ", value2 = " + value2);
		}
	}
	
	public static int[] getFilledArray(int count, int value) {
		int[] array = new int[count];
		Arrays.fill(array, value);
		return array;
	}
}
