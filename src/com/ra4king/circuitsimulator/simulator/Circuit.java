package com.ra4king.circuitsimulator.simulator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Roi Atalla
 */
public class Circuit {
	private Simulator simulator;
	
	private Set<Component> components;
	private Set<CircuitState> states;
	private CircuitState topLevelState;
	
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
		
		return component;
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
}
