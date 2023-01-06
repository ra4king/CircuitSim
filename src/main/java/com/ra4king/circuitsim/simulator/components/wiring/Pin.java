package com.ra4king.circuitsim.simulator.components.wiring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Pin extends Component {
	private Map<CircuitState, Set<PinChangeListener>> pinChangeListeners;
	private int bitSize;
	private boolean isInput;
	
	public static final int PORT = 0;
	
	public Pin(String name, int bitSize, boolean isInput) {
		super(name, Utils.getFilledArray(1, bitSize));
		pinChangeListeners = new HashMap<>();
		this.bitSize = bitSize;
		this.isInput = isInput;
	}
	
	public Port getPort() {
		return getPort(PORT);
	}
	
	public int getBitSize() {
		return bitSize;
	}
	
	public boolean isInput() {
		return isInput;
	}
	
	public void addChangeListener(CircuitState state, PinChangeListener listener) {
		pinChangeListeners.computeIfAbsent(state, s -> new HashSet<>()).add(listener);
	}
	
	public void removeChangeListener(CircuitState state, PinChangeListener listener) {
		Set<PinChangeListener> listeners = pinChangeListeners.get(state);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
	
	public void setValue(CircuitState state, WireValue value) {
		state.pushValue(getPort(PORT), value);
	}
	
	@Override
	public void init(CircuitState state, Object lastProperty) {
		if (getCircuit() != null && isInput && getCircuit().getTopLevelState() == state) {
			state.pushValue(getPort(Pin.PORT), WireValue.of(0, getBitSize()));
		}
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		Set<PinChangeListener> listeners = pinChangeListeners.get(state);
		if (listeners != null) {
			for (PinChangeListener listener : listeners) {
				listener.valueChanged(this, state, value);
			}
		}
	}
	
	public interface PinChangeListener {
		void valueChanged(Pin pin, CircuitState state, WireValue value);
	}
}
