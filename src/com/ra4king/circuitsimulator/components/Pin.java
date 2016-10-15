package com.ra4king.circuitsimulator.components;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.utils.Pair;
import com.ra4king.circuitsimulator.utils.Utils;

/**
 * @author Roi Atalla
 */
public class Pin extends Component {
	private List<Pair<CircuitState, PinChangeListener>> pinChangeListeners;
	
	public Pin(String name, int bitSize) {
		super("Pin " + name + "(" + bitSize + ")", Utils.getFilledArray(1, bitSize));
		pinChangeListeners = new ArrayList<>();
	}
	
	public void addChangeListener(CircuitState state, PinChangeListener listener) {
		pinChangeListeners.add(new Pair<>(state, listener));
	}
	
	public void removeChangeListener(CircuitState state, PinChangeListener listener) {
		pinChangeListeners.remove(new Pair<>(state, listener));
	}
	
	public void setValue(CircuitState state, WireValue value) {
		System.out.println(this + ": value changed = " + value);
		state.pushValue(getPort(0), value);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		System.out.println(this + ": value changed = " + value);
		
		pinChangeListeners.stream().filter(pair -> state == pair.first)
				.forEach(pair -> pair.second.valueChanged(value));
	}
	
	public interface PinChangeListener {
		void valueChanged(WireValue value);
	}
}
