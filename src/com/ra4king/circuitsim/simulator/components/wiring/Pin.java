package com.ra4king.circuitsim.simulator.components.wiring;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class Pin extends Component {
	private List<Pair<CircuitState, PinChangeListener>> pinChangeListeners;
	private int bitSize = 0;
	private boolean isInput;
	
	public static final int PORT = 0;
	
	public Pin(String name, int bitSize, boolean isInput) {
		super(name, Utils.getFilledArray(1, bitSize));
		pinChangeListeners = new ArrayList<>();
		this.bitSize = bitSize;
		this.isInput = isInput;
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	public boolean isInput() {
		return isInput;
	}
	
	public void addChangeListener(Pair<CircuitState, PinChangeListener> listener) {
		pinChangeListeners.add(listener);
	}
	
	public void removeChangeListener(Pair<CircuitState, PinChangeListener> listener) {
		pinChangeListeners.remove(listener);
	}
	
	public void setValue(CircuitState state, WireValue value) {
		state.pushValue(getPort(PORT), value);
	}
	
	@Override
	public void init(CircuitState state, Object lastProperty) {
		if(getCircuit() != null && isInput && getCircuit().getTopLevelState() == state) {
			state.pushValue(getPort(Pin.PORT), WireValue.of(0, getBitSize()));
		}
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		pinChangeListeners.stream().filter(pair -> state == pair.getKey())
		                  .forEach(pair -> pair.getValue().valueChanged(this, state, value));
	}
	
	public interface PinChangeListener {
		void valueChanged(Pin pin, CircuitState state, WireValue value);
	}
}
