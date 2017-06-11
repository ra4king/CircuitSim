package com.ra4king.circuitsimulator.simulator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Roi Atalla
 */
public class Circuit {
	private final Simulator simulator;
	
	private final Set<Component> components;
	private final Set<CircuitState> states;
	private final CircuitState topLevelState;
	
	private final Queue<CircuitChangeListener> listeners = new ConcurrentLinkedQueue<>();
	
	/**
	 * Creates a new Circuit. It is added to the Simulator's list of circuits.
	 *
	 * @param simulator The Simulator instance this Circuit belongs to.
	 */
	public Circuit(Simulator simulator) {
		this.simulator = simulator;
		simulator.addCircuit(this);
		
		components = new HashSet<>();
		states = new HashSet<>();
		
		topLevelState = new CircuitState(this);
	}
	
	/**
	 * Add the Component to this Circuit. Its {@code init} method is called for each state belonging to this Circuit.
	 * All attached listeners are notified of the addition.
	 *
	 * @param component The Component to add.
	 * @return The Component given.
	 */
	public <T extends Component> T addComponent(T component) {
		simulator.runSync(() -> {
			if(component.getCircuit() == this) {
				return;
			}
			
			if(component.getCircuit() != null) {
				throw new IllegalArgumentException("Component already belongs to a circuit.");
			}
			
			component.setCircuit(this);
			components.add(component);
			states.forEach(state -> component.init(state, state.getComponentProperty(component)));
			
			listeners.forEach(listener -> listener.circuitChanged(this, component, true));
		});
		
		return component;
	}
	
	/**
	 * Replaces the old Component with the new Component, preserving the component properties. The inBetween Runnable
	 * is run in between the remove and the add operations.
	 * <p>
	 * All attached listeners are notified of the removal before the inBetween is run, then of the addition.
	 *
	 * @param oldComponent The old Component.
	 * @param newComponent The new Component.
	 * @param inBetween    Any code that needs to run in between the remove/add operations. May be null.
	 */
	public <T extends Component> void updateComponent(T oldComponent, T newComponent, Runnable inBetween) {
		simulator.runSync(() -> {
			states.forEach(state -> state.ensureUnlinked(oldComponent));
			
			components.remove(oldComponent);
			states.forEach(oldComponent::uninit);
			oldComponent.setCircuit(null);
			
			listeners.forEach(listener -> listener.circuitChanged(this, oldComponent, false));
			
			if(inBetween != null) {
				inBetween.run();
			}
			
			newComponent.setCircuit(this);
			components.add(newComponent);
			states.forEach(state -> newComponent.init(state, state.getComponentProperty(oldComponent)));
			
			listeners.forEach(listener -> listener.circuitChanged(this, newComponent, true));
		});
	}
	
	/**
	 * Removes the Component from this Circuit. Its {@code uninit} method is called for each belonging to this Circuit.
	 * All attached listeners are notified of the addition.
	 *
	 * @param component The Component to remove.
	 */
	public void removeComponent(Component component) {
		simulator.runSync(() -> {
			states.forEach(state -> state.ensureUnlinked(component));
			
			components.remove(component);
			states.forEach(component::uninit);
			component.setCircuit(null);
			
			listeners.forEach(listener -> listener.circuitChanged(this, component, false));
		});
	}
	
	public Set<Component> getComponents() {
		return components;
	}
	
	/**
	 * Calls {@code this.removeComponent} on each Component in this Circuit.
	 */
	public void clearComponents() {
		simulator.runSync(() -> new HashSet<>(components).forEach(this::removeComponent));
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	/**
	 * Returns the top-level state of this Circuit. Each Circuit has a top-level state.
	 *
	 * @return The top-level state.
	 */
	public CircuitState getTopLevelState() {
		return topLevelState;
	}
	
	public Set<CircuitState> getCircuitStates() {
		return states;
	}
	
	public void addListener(CircuitChangeListener listener) {
		listeners.add(listener);
	}
	
	public Collection<CircuitChangeListener> getListeners() {
		return listeners;
	}
	
	public void removeListener(CircuitChangeListener listener) {
		listeners.remove(listener);
	}
	
	public interface CircuitChangeListener {
		void circuitChanged(Circuit circuit, Component component, boolean added);
	}
}
