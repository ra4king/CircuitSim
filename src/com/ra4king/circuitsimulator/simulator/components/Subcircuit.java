package com.ra4king.circuitsimulator.simulator.components;

import java.util.List;
import java.util.stream.Collectors;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Subcircuit extends Component {
	private Circuit subcircuit;
	private List<Pin> pins;
	
	public Subcircuit(String name, Circuit subcircuit) {
		this(name, subcircuit, getCircuitPins(subcircuit));
	}
	
	private Subcircuit(String name, Circuit subcircuit, List<Pin> pins) {
		super("Subcircuit " + name, setupPortBits(pins));
		
		this.subcircuit = subcircuit;
		this.pins = pins;
	}
	
	@Override
	public void init(CircuitState circuitState) {
		super.init(circuitState);
		CircuitState subcircuitState = new CircuitState(subcircuit);
		circuitState.putComponentProperty(this, subcircuitState);
		
		for(int i = 0; i < pins.size(); i++) {
			Port port = getPort(i);
			pins.get(i).addChangeListener(subcircuitState, value -> circuitState.pushValue(port, value));
		}
		
		for(Component component : subcircuit.getComponents()) {
			component.init(subcircuitState);
		}
	}
	
	public Port getPort(Pin pin) {
		int index = pins.indexOf(pin);
		if(index == -1)
			return null;
		return getPort(index);
	}
	
	private static List<Pin> getCircuitPins(Circuit circuit) {
		return circuit.getComponents().stream()
				       .filter(component -> component instanceof Pin).map(component -> (Pin)component)
				       .collect(Collectors.toList());
	}
	
	private static int[] setupPortBits(List<Pin> pins) {
		int[] portBits = new int[pins.size()];
		for(int i = 0; i < portBits.length; i++) {
			portBits[i] = pins.get(i).getPort(0).getLink().getBitSize();
		}
		return portBits;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		CircuitState subcircuitState = (CircuitState)state.getComponentProperty(this);
		subcircuitState.pushValue(pins.get(portIndex).getPort(0), value);
	}
}
