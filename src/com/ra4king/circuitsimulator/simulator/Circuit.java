package com.ra4king.circuitsimulator.simulator;

import java.util.ArrayList;
import java.util.Collections;
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
		if(component.getCircuit() == this) {
			return component;
		}
		
		if(component.getCircuit() != null) {
			throw new IllegalArgumentException("Component already belongs to a circuit.");
		}
		
		component.setCircuit(this);
		states.forEach(component::init);
		components.add(component);
		
		listeners.forEach(listener -> listener.circuitChanged(this));
		
		return component;
	}
	
	public void removeComponent(Component component) {
		components.remove(component);
		states.forEach(component::uninit);
		states.forEach(state -> state.removeComponentProperty(component));
		component.setCircuit(null);
		
		listeners.forEach(listener -> listener.circuitChanged(this));
	}
	
	public Set<Component> getComponents() {
		return Collections.unmodifiableSet(components);
	}
	
	public void clearComponents() {
		new HashSet<>(components).forEach(this::removeComponent);
		listeners.forEach(listener -> listener.circuitChanged(this));
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
