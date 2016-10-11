package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.Utils;
import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Pin extends Component {
	private final boolean isInput;
	
	public Pin(Simulator simulator, String name, int bitSize, boolean isInput) {
		super(simulator, (isInput ? "Input" : "Output") + " Pin " + name + "(" + bitSize + ")", Utils.getFilledArray(1, bitSize));
		this.isInput = isInput;
	}
	
	public void setValue(WireValue value) {
		if(!isInput) {
			throw new IllegalStateException(this + ": cannot change value of an input pin");
		}
		
		System.out.println(this + ": value changed = " + value);
		ports[0].pushValue(value);
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		System.out.println(this + ": value changed = " + value);
	}
}
