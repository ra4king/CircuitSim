package com.ra4king.circuitsim.gui.properties;

import com.ra4king.circuitsim.simulator.SimulationException;

/**
 * @author Roi Atalla
 */
public final class IntegerString {
	private final String valueString;
	private final int value;
	private final int base;
	
	public IntegerString(String value) {
		String valueToParse;
		if (value.startsWith("0x")) {
			base = 16;
			valueString = value;
			valueToParse = value.substring(2);
		} else if (value.startsWith("x")) {
			base = 16;
			valueString = "0" + value;
			valueToParse = value.substring(1);
		} else if (value.startsWith("0b")) {
			base = 2;
			valueString = value;
			valueToParse = value.substring(2);
		} else if (value.startsWith("b")) {
			base = 2;
			valueString = "0" + value;
			valueToParse = value.substring(1);
		} else {
			base = 10;
			valueString = valueToParse = value;
		}
		
		try {
			this.value = (int)Long.parseLong(valueToParse, base);
		} catch (NumberFormatException exc) {
			throw new SimulationException(valueString + " is not a valid value of base " + base);
		}
	}
	
	public IntegerString(int value) {
		this.value = value;
		this.base = 10;
		valueString = Integer.toString(value);
	}
	
	public int getValue() {
		return value;
	}
	
	public int getBase() {
		return base;
	}
	
	public String getValueString() {
		return valueString;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof IntegerString) {
			IntegerString other = (IntegerString)o;
			return other.valueString.equals(this.valueString);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return valueString.hashCode();
	}
	
	@Override
	public String toString() {
		return valueString;
	}
}
