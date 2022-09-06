package com.ra4king.circuitsim.simulator.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.SimulationException;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;
import com.ra4king.circuitsim.simulator.components.wiring.Pin.PinChangeListener;

/**
 * @author Roi Atalla
 */
public class Subcircuit extends Component {
	private Circuit subcircuit;
	private List<Pin> pins;
	private Map<CircuitState, Map<Pin, PinChangeListener>> pinListeners;
	
	public Subcircuit(String name, Circuit subcircuit) {
		this(name, subcircuit, getCircuitPins(subcircuit));
	}
	
	private Subcircuit(String name, Circuit subcircuit, List<Pin> pins) {
		super(name, setupPortBits(pins));
		
		this.subcircuit = subcircuit;
		this.pins = pins;
		pinListeners = new HashMap<>();
	}
	
	private static List<Pin> getCircuitPins(Circuit circuit) {
		return circuit
			.getComponents()
			.stream()
			.filter(component -> component instanceof Pin)
			.map(component -> (Pin)component)
			.collect(Collectors.toList());
	}
	
	private static int[] setupPortBits(List<Pin> pins) {
		int[] portBits = new int[pins.size()];
		for (int i = 0; i < portBits.length; i++) {
			portBits[i] = pins.get(i).getBitSize();
		}
		return portBits;
	}
	
	public List<Pin> getPins() {
		return pins;
	}
	
	public Circuit getSubcircuit() {
		return subcircuit;
	}
	
	private void checkCircuitLoop(Circuit circuit) {
		if (circuit == getCircuit()) {
			throw new SimulationException("Subcircuit loop detected.");
		}
		
		for (Component component : circuit.getComponents()) {
			if (component != this && component instanceof Subcircuit) {
				Subcircuit subcircuit = (Subcircuit)component;
				checkCircuitLoop(subcircuit.getSubcircuit());
			}
		}
	}
	
	@Override
	public void setCircuit(Circuit circuit) {
		super.setCircuit(circuit);
		
		checkCircuitLoop(subcircuit);
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		CircuitState subcircuitState = new CircuitState(subcircuit);
		circuitState.putComponentProperty(this, subcircuitState);
		
		Map<Pin, PinChangeListener> listeners = new HashMap<>();
		
		for (int i = 0; i < pins.size(); i++) {
			Pin pin = pins.get(i);
			if (!pin.isInput()) {
				Port port = getPort(i);
				
				PinChangeListener listener = (p, state, value) -> circuitState.pushValue(port, value);
				pin.addChangeListener(subcircuitState, listener);
				
				listeners.put(pin, listener);
			}
		}
		
		pinListeners.put(subcircuitState, listeners);
		
		CircuitState oldState = (CircuitState)lastProperty;
		
		for (Component component : subcircuit.getComponents()) {
			component.init(subcircuitState, oldState == null ? null : oldState.getComponentProperty(component));
		}
		
		if (oldState != null) {
			getCircuit().removeState(oldState);
		}
	}
	
	public CircuitState getSubcircuitState(CircuitState parentState) {
		return (CircuitState)parentState.getComponentProperty(this);
	}
	
	@Override
	public void uninit(CircuitState circuitState) {
		CircuitState subcircuitState = (CircuitState)circuitState.getComponentProperty(this);
		subcircuit.getComponents().forEach(component -> component.uninit(subcircuitState));
		subcircuit.removeState(subcircuitState);
		if (pinListeners.containsKey(subcircuitState)) {
			Map<Pin, PinChangeListener> listeners = pinListeners.get(subcircuitState);
			pins.forEach(pin -> {
				if (listeners.containsKey(pin)) {
					pin.removeChangeListener(subcircuitState, listeners.get(pin));
				}
			});
		}
	}
	
	public Port getPort(Pin pin) {
		int index = pins.indexOf(pin);
		if (index == -1) {
			return null;
		}
		return getPort(index);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		CircuitState subcircuitState = (CircuitState)state.getComponentProperty(this);
		Pin pin = pins.get(portIndex);
		// Sometimes we get updates for pins that were just removed
		if (pin.isInput() && pin.getCircuit() != null) {
			subcircuitState.pushValue(pin.getPort(0), value);
		}
	}
}
