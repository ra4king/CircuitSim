package com.ra4king.circuitsim.simulator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * @author Roi Atalla
 */
public class Circuit {
	private String name;
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
	public Circuit(String name, Simulator simulator) {
		this.name = name;
		
		this.simulator = simulator;
		simulator.addCircuit(this);
		
		components = new HashSet<>();
		states = new HashSet<>();
		
		topLevelState = new CircuitState(this);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	private RuntimeException exception = null;
	
	/**
	 * Add the Component to this Circuit. Its {@code init} method is called for each state belonging to this Circuit.
	 * All attached listeners are notified of the addition.
	 *
	 * @param component The Component to add.
	 * @return The Component given.
	 */
	public <T extends Component> T addComponent(T component) {
		simulator.runSync(() -> {
			if (component.getCircuit() == this) {
				return;
			}
			
			if (component.getCircuit() != null) {
				throw new IllegalArgumentException("Component already belongs to a circuit.");
			}
			
			component.setCircuit(this);
			components.add(component);
			states.forEach(state -> {
				try {
					component.init(state, state.getComponentProperty(component));
				} catch (RuntimeException exc) {
					if (exception == null) {
						exception = exc;
					}
				}
			});
			
			listeners.forEach(listener -> listener.circuitChanged(this, component, true));
		});
		
		if (exception != null) {
			RuntimeException exc = exception;
			exception = null;
			throw exc;
		}
		
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
			states.forEach(state -> state.ensureUnlinked(oldComponent, false));
			
			components.remove(oldComponent);
			states.forEach(state -> {
				try {
					oldComponent.uninit(state);
				} catch (RuntimeException exc) {
					if (exception == null) {
						exception = exc;
					}
				}
			});
			oldComponent.setCircuit(null);
			
			listeners.forEach(listener -> listener.circuitChanged(this, oldComponent, false));
			
			if (inBetween != null) {
				inBetween.run();
			}
			
			newComponent.setCircuit(this);
			components.add(newComponent);
			states.forEach(state -> {
				try {
					newComponent.init(state, state.getComponentProperty(oldComponent));
				} catch (RuntimeException exc) {
					if (exception == null) {
						exception = exc;
					}
				}
			});
			
			listeners.forEach(listener -> listener.circuitChanged(this, newComponent, true));
		});
		
		if (exception != null) {
			RuntimeException exc = exception;
			exception = null;
			throw exc;
		}
	}
	
	/**
	 * Removes the Component from this Circuit. Its {@code uninit} method is called for each belonging to this Circuit.
	 * All attached listeners are notified of the addition.
	 *
	 * @param component The Component to remove.
	 */
	public void removeComponent(Component component) {
		simulator.runSync(() -> {
			if (!components.contains(component)) {
				return;
			}
			
			states.forEach(state -> state.ensureUnlinked(component, true));
			
			components.remove(component);
			states.forEach(state -> {
				try {
					component.uninit(state);
				} catch (RuntimeException exc) {
					if (exception == null) {
						exception = exc;
					}
				}
			});
			component.setCircuit(null);
			
			listeners.forEach(listener -> listener.circuitChanged(this, component, false));
		});
		
		if (exception != null) {
			RuntimeException exc = exception;
			exception = null;
			throw exc;
		}
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
	
	public void addState(CircuitState state) {
		states.add(state);
	}
	
	public boolean containsState(CircuitState state) {
		return states.contains(state);
	}
	
	public void removeState(CircuitState state) {
		states.remove(state);
	}
	
	public void forEachState(Consumer<CircuitState> consumer) {
		states.forEach(consumer);
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
	
	@Override
	public String toString() {
		return "Circuit " + name;
	}
	
	public interface CircuitChangeListener {
		void circuitChanged(Circuit circuit, Component component, boolean added);
	}
}
