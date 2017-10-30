package com.ra4king.circuitsim;

import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class WireValueTest {
	public static void main(String[] args) {
		System.out.println(WireValue.of(0xFF, 4));
		System.out.println(WireValue.of(0xFF, 7));
		
		tryMerge(stringToValue("xxx"), stringToValue("10"));
		tryMerge(stringToValue("101010"), stringToValue("1x1x1x"));
		tryMerge(stringToValue("1100"), stringToValue("1100"));
		tryMerge(stringToValue("10101"), stringToValue("01010"));
	}
	
	public static WireValue stringToValue(String s) {
		WireValue value = new WireValue(s.length());
		for(int i = 0; i < s.length(); i++) {
			State bit;
			switch(s.charAt(i)) {
				case '1': bit = State.ONE; break;
				case '0': bit = State.ZERO; break;
				case 'x': bit = State.X; break;
				default: throw new IllegalArgumentException("Wtf dude");
			}
			value.setBit(s.length() - 1 - i, bit);
		}
		return value;
	}
	
	public static void tryMerge(WireValue value1, WireValue value2) {
		System.out.print(value1 + " <=> " + value2 + " = ");
		try {
			System.out.println(value1.merge(value2));
		} catch(Exception exc) {
			System.out.println("Incompatible.");
		}
	}
}
