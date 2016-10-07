package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class WireValueTest {
	public static void main(String[] args) {
		System.out.println(WireValue.of(0xFF, 4));
		System.out.println(WireValue.of(0xFF, 7));
	}
}
