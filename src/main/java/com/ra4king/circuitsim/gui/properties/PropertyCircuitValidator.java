package com.ra4king.circuitsim.gui.properties;

import java.util.function.Consumer;

import com.ra4king.circuitsim.gui.CircuitManager;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.Properties.PropertyValidator;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * @author Roi Atalla
 */
public class PropertyCircuitValidator implements PropertyValidator<CircuitManager> {
	private final CircuitSim circuitSim;
	private CircuitManager circuitManager;
	
	public PropertyCircuitValidator(CircuitSim circuitSim) {
		this(circuitSim, null);
	}
	
	public PropertyCircuitValidator(CircuitSim circuitSim, CircuitManager circuitManager) {
		this.circuitSim = circuitSim;
		this.circuitManager = circuitManager;
	}
	
	@Override
	public int hashCode() {
		return circuitSim.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PropertyCircuitValidator) {
			PropertyCircuitValidator validator = (PropertyCircuitValidator)other;
			return this.circuitSim == validator.circuitSim;
		}
		
		return false;
	}
	
	@Override
	public CircuitManager parse(String value) {
		if (circuitManager == null && circuitSim != null) {
			return circuitManager = circuitSim.getCircuitManager(value);
		}
		
		return circuitManager;
	}
	
	@Override
	public String toString(CircuitManager circuit) {
		return circuitSim.getCircuitName(circuit);
	}
	
	@Override
	public Node createGui(Stage stage, CircuitManager value, Consumer<CircuitManager> onAction) {
		return null;
	}
}
