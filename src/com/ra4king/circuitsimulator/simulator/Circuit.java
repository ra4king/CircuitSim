package com.ra4king.circuitsimulator.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roi Atalla
 */
public class Circuit {
	private Simulator simulator;
	
	private Set<Component> components;
	private Set<CircuitState> states;
	private CircuitState topLevelState;
	
	private List<CircuitChangeListener> listeners = new ArrayList<>();
	
	public Circuit(Simulator simulator) {
		this.simulator = simulator;
		simulator.addCircuit(this);
		
		components = new HashSet<>();
		states = new HashSet<>();
		
		topLevelState = new CircuitState(this);
	}
	
	public <T extends Component> T addComponent(T component) {
		if(component.getCircuit() != null)
			throw new IllegalArgumentException("Component already belongs to a circuit.");
		
		components.add(component);
		component.setCircuit(this);
		
		states.forEach(component::init);
		
		listeners.forEach(listener -> listener.circuitChanged(this));
		
		return component;
	}
	
	public void removeComponent(Component component) {
		components.remove(component);
		states.forEach(state -> state.removeComponentProperty(component));
		listeners.forEach(listener -> listener.circuitChanged(this));
	}
	
	public Set<Component> getComponents() {
		return components;
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	public CircuitState getTopLevelState() {
		return topLevelState;
	}
	
	public Set<CircuitState> getCircuitStates() {
		return states; 
	}
	
	public void addListener(CircuitChangeListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(CircuitChangeListener listener) {
		listeners.remove(listener);
	}
	
	public interface CircuitChangeListener {
		void circuitChanged(Circuit circuit);
	}
}
