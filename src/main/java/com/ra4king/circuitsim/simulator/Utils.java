package com.ra4king.circuitsim.simulator;

import java.util.Arrays;

/**
 * @author Roi Atalla
 */
public class Utils {
	public static int[] getFilledArray(int count, int value) {
		int[] array = new int[count];
		Arrays.fill(array, value);
		return array;
	}
}
