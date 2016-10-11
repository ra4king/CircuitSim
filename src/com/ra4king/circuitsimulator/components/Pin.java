package com.ra4king.circuitsimulator.components;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.Utils;
import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Pin extends Component {
	public Pin(Simulator simulator, String name, int bitSize, boolean isInput) {
		super(simulator, (isInput ? "Input" : "Output") + " Pin " + name + "(" + bitSize + ")", Utils.getFilledArray(1, bitSize));
		properties.put(PropertyType.PIN_IS_INPUT, isInput);
		properties.put(PropertyType.BIT_SIZE, bitSize);
	}
	
	public void setValue(WireValue value) {
		if(!(Boolean)properties.get(PropertyType.PIN_IS_INPUT)) {
			throw new IllegalStateException(this + ": cannot change value of an input pin");
		}
		
		Utils.ensureBitSize(this, value, (Integer)properties.get(PropertyType.BIT_SIZE));
		
		System.out.println(this + ": value changed = " + value);
		ports[0].pushValue(value);
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {
		if(!(Boolean)properties.get(PropertyType.PIN_IS_INPUT)) {
			System.out.println(this + ": value changed = " + value);
		}
	}
}
