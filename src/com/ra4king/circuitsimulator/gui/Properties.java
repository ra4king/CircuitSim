package com.ra4king.circuitsimulator.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Roi Atalla
 */
public class Properties {
	private Map<String, Property> properties;
	
	public Properties() {
		properties = new LinkedHashMap<>();
	}
	
	public Properties(Properties prop) {
		this();
		union(prop);
	}
	
	public List<String> getProperties() {
		return new ArrayList<>(properties.keySet());
	}
	
	public boolean containsProperty(String name) {
		return properties.containsKey(name);
	}
	
	public void forEach(Consumer<Property> consumer) {
		properties.values().forEach(consumer);
	}
	
	public boolean isEmpty() {
		return properties.isEmpty();
	}
	
	public void ensureProperty(Property property) {
		setProperty(property, false);
	}
	
	public void setProperty(Property property) {
		setProperty(property, true);
	}
	
	private void setProperty(Property property, boolean overwriteValue) {
		if(!properties.containsKey(property.name)) {
			properties.put(property.name, property);
		} else {
			Property ourProperty = properties.get(property.name);
			
			PropertyValidator validator = chooseValidator(property.validator, ourProperty.validator);
			String value;
			if(overwriteValue) {
				value = property.value.isEmpty() ? ourProperty.value : property.value;
			} else {
				value = ourProperty.value.isEmpty() ? property.value : ourProperty.value;
			}
			
			properties.put(property.name, new Property(property.name, validator, value));
		}
	}
	
	public void setValue(String name, String value) {
		setValue(getProperty(name), value);
	}
	
	public void setValue(Property property, String value) {
		properties.put(property.name, new Property(property, value));
	}
	
	public void updateIfExists(Property property) {
		if(properties.containsKey(property.name)) {
			setProperty(property, true);
		}
	}
	
	public Property getProperty(String name) {
		return properties.get(name);
	}
	
	public String getValue(Property property) {
		return getValue(property.name);
	}
	
	public String getValue(String name) {
		return getValueOrDefault(name, null);
	}
	
	public String getValueOrDefault(Property property, String defaultValue) {
		return getValueOrDefault(property.name, defaultValue);
	}
	
	public String getValueOrDefault(String name, String defaultValue) {
		if(properties.containsKey(name)) {
			return properties.get(name).value;
		}
		
		return defaultValue;
	}
	
	public int getIntValue(String name) {
		String value = getValue(name);
		
		try {
			return Integer.parseInt(value);
		} catch(Exception exc) {
			if(value.startsWith("0x")) {
				value = value.substring(2);
			} else if(value.startsWith("x")) {
				value = value.substring(1);
			} else {
				throw exc;
			}
			
			return Integer.parseInt(value, 16);
		}
	}
	
	public int getIntValue(Property property) {
		return getIntValue(property.name);
	}
	
	public int getIntValueOrDefault(String name, int defaultValue) {
		try {
			return getIntValue(name);
		} catch(Exception exc) {
			return defaultValue;
		}
	}
	
	public int getIntValueOrDefault(Property property, int defaultValue) {
		return getIntValueOrDefault(property.name, defaultValue);
	}
	
	private PropertyValidator chooseValidator(PropertyValidator validator1, PropertyValidator validator2) {
		if(validator1 != null && validator2 != null && !validator1.equals(validator2)) {
			throw new IllegalArgumentException("Property with the same name but different validator!");
		}
		
		return validator1 == null ? validator2 : validator1;
	}
	
	/**
	 * if add is false, it will ignore missing properties
	 */
	public Properties mergeIfExists(Properties other) {
		other.forEach(this::updateIfExists);
		return this;
	}
	
	public Properties union(Properties other) {
		other.forEach(this::setProperty);
		return this;
	}
	
	public Properties intersect(Properties properties) {
		Properties newProp = new Properties();
		this.forEach((prop) -> {
			if(properties.properties.containsKey(prop.name)) {
				if(!getValue(prop).equals(properties.getValue(prop))) {
					newProp.setValue(prop, "");
				} else {
					newProp.setProperty(prop);
				}
			}
		});
		return newProp;
	}
	
	public static final PropertyValidator ANY_STRING_VALIDATOR = value -> true;
	public static final PropertyValidator YESNO_VALIDATOR = new PropertyListValidator(new String[] { "Yes", "No" });
	public static final PropertyValidator INTEGER_VALIDATOR = value -> {
		try {
			Integer.parseInt(value);
			return true;
		} catch(Exception exc) {
			if(value.startsWith("0x")) {
				value = value.substring(2);
			} else if(value.startsWith("x")) {
				value = value.substring(1);
			} else {
				return false;
			}
			
			try {
				Integer.parseInt(value, 16);
				return true;
			} catch(Exception exc2) {
				return false;
			}
		}
	};
	
	public static final Property LABEL;
	public static final Property BITSIZE;
	public static final Property NUM_INPUTS;
	
	static {
		LABEL = new Property("Label", ANY_STRING_VALIDATOR, "");
		
		String[] numInputsValues = new String[31];
		for(int i = 0; i < numInputsValues.length; i++) {
			numInputsValues[i] = String.valueOf(i + 2);
		}
		NUM_INPUTS = new Property("Number of Inputs", new PropertyListValidator(numInputsValues), "2");
		
		String[] bitsizeValues = new String[32];
		for(int i = 0; i < bitsizeValues.length; i++) {
			bitsizeValues[i] = String.valueOf(i + 1);
		}
		BITSIZE = new Property("Bitsize", new PropertyListValidator(bitsizeValues), "1");
	}
	
	public static class Property {
		public final String name;
		public final PropertyValidator validator;
		public final String value;
		
		public Property(Property property) {
			this(property.name, property.validator, property.value);
		}
		
		public Property(Property property, String value) {
			this(property.name, property.validator, value);
		}
		
		public Property(String name, PropertyValidator validator, String value) {
			if(validator != null && !value.isEmpty() && !validator.validate(value)) {
				throw new IllegalArgumentException("Value is not validated: " + value);
			}
			
			this.name = name;
			this.validator = validator;
			this.value = value;
		}
	}
	
	public interface PropertyValidator {
		boolean validate(String value);
	}
	
	public static class PropertyListValidator implements PropertyValidator {
		public final List<String> validValues;
		
		public PropertyListValidator(String[] validValues) {
			this(Arrays.asList(validValues));
		}
		
		public PropertyListValidator(List<String> validValues) {
			this.validValues = Collections.unmodifiableList(validValues);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof PropertyListValidator) {
				PropertyListValidator validator = (PropertyListValidator)other;
				return this.validValues.equals(validator.validValues);
			}
			
			return true;
		}
		
		@Override
		public boolean validate(String value) {
			return validValues.contains(value);
		}
	}
	
	public static class PropertyCircuitValidator implements PropertyValidator {
		private final CircuitSimulator circuitSimulator;
		private CircuitManager circuitManager;
		
		public PropertyCircuitValidator(CircuitSimulator circuitSimulator) {
			this(circuitSimulator, null);
		}
		
		public PropertyCircuitValidator(CircuitSimulator circuitSimulator, CircuitManager circuitManager) {
			this.circuitSimulator = circuitSimulator;
			this.circuitManager = circuitManager;
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof PropertyCircuitValidator) {
				PropertyCircuitValidator validator = (PropertyCircuitValidator)other;
				return this.circuitSimulator == validator.circuitSimulator;
			}
			
			return false;
		}
		
		@Override
		public boolean validate(String value) {
			if(circuitManager == null && circuitSimulator != null) {
				circuitManager = circuitSimulator.getCircuitManager(value);
			}
			
			return circuitManager != null;
		}
		
		public CircuitManager getCircuitManager(String value) {
			validate(value);
			return circuitManager;
		}
	}
}
