package com.ra4king.circuitsimulator.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Roi Atalla
 */
public class Properties {
	private List<String> propertyNames;
	private HashMap<String, Property> properties;
	
	public Properties() {
		propertyNames = new ArrayList<>();
		properties = new HashMap<>();
	}
	
	public Properties(Properties prop) {
		this();
		merge(prop);
	}
	
	public List<String> getProperties() {
		return propertyNames;
	}
	
	public boolean containsProperty(String name) {
		return properties.keySet().contains(name);
	}
	
	public void forEach(Consumer<Property> consumer) {
		propertyNames.forEach(name -> consumer.accept(properties.get(name)));
	}
	
	public boolean isEmpty() {
		return propertyNames.isEmpty();
	}
	
	public void ensureProperty(Property property) {
		if(!properties.containsKey(property.name)) {
			propertyNames.add(property.name);
			properties.put(property.name, property);
		}
	}
	
	public void setValue(Property property, String value) {
		if(!properties.containsKey(property.name)) {
			propertyNames.add(property.name);
		}
		properties.put(property.name, new Property(property, value));
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
	
	/**
	 * if add is false, it will ignore missing properties
	 */
	public void merge(Properties other) {
		other.forEach((prop) -> {
			if(this.properties.containsKey(prop.name)) {
				Property ourProperty = this.properties.get(prop.name);
				
				if(ourProperty.validator != null
						   && prop.validator != null
						   && !ourProperty.validator.equals(prop.validator)) {
					throw new IllegalArgumentException("Property with the same name but different validator: "
							                                   + prop.name);
				}
				
				if(!prop.value.isEmpty()) {
					this.properties.put(prop.name, prop);
				}
			} else {
				ensureProperty(prop);
			}
		});
	}
	
	public Properties intersect(Properties properties) {
		Properties newProp = new Properties();
		this.forEach((prop) -> {
			if(properties.properties.containsKey(prop.name)) {
				if(!getValue(prop).equals(properties.getValue(prop))) {
					newProp.setValue(prop, "");
				} else {
					newProp.ensureProperty(prop);
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
			if(!value.isEmpty() && !validator.validate(value)) {
				throw new IllegalArgumentException("Value is not validated.");
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
}
