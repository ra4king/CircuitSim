package com.ra4king.circuitsim.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.ra4king.circuitsim.gui.properties.IntegerString;
import com.ra4king.circuitsim.gui.properties.PropertyListValidator;
import com.ra4king.circuitsim.gui.properties.PropertyValidators;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * @author Roi Atalla
 */
@SuppressWarnings("unchecked")
public class Properties {
	private final Map<String, Property<?>> properties;
	private final CircuitSimVersion version;
	
	private Properties(
		Map<String, Property<?>> properties, CircuitSimVersion version) {
		this.properties = properties;
		this.version = version;
	}
	
	Properties(CircuitSimVersion version) {
		this(new LinkedHashMap<>(), version);
	}
	
	public Properties() {
		this(CircuitSimVersion.VERSION);
	}
	
	public Properties(Property<?>... props) {
		this();
		for (Property<?> prop : props) {
			setProperty(prop);
		}
	}
	
	public Properties(Properties prop) {
		this(prop.version);
		union(prop);
	}
	
	public CircuitSimVersion getVersion() {
		return version;
	}
	
	public List<String> getProperties() {
		return new ArrayList<>(properties.keySet());
	}
	
	public boolean containsProperty(String name) {
		return properties.containsKey(name);
	}
	
	public boolean containsProperty(Property<?> property) {
		return containsProperty(property.name);
	}
	
	public void forEach(Consumer<Property<?>> consumer) {
		properties.values().forEach(consumer);
	}
	
	public boolean isEmpty() {
		return properties.isEmpty();
	}
	
	public void ensureProperty(Property<?> property) {
		setProperty(property, false);
	}
	
	public void ensurePropertyIfExists(Property<?> property) {
		if (containsProperty(property)) {
			setProperty(property, false);
		}
	}
	
	public void setProperty(Property<?> property) {
		setProperty(property, true);
	}
	
	public void clearProperty(String name) {
		properties.remove(name);
	}
	
	private <T> void setProperty(Property<T> property, boolean overwriteValue) {
		if (!properties.containsKey(property.name)) {
			properties.put(property.name, property);
		} else {
			if (getProperty(property.name).validator == null) {
				parseAndSetValue(property, getProperty(property.name).value.toString());
			} else if (property.validator == null) {
				parseAndSetValue(getProperty(property.name), property.value.toString());
			} else {
				Property<T> ourProperty = getProperty(property.name);
				
				PropertyValidator<T> validator = chooseValidator(property.validator, ourProperty.validator);
				T value;
				if (overwriteValue) {
					value = property.value == null ? ourProperty.value : property.value;
				} else {
					value = ourProperty.value == null ? property.value : ourProperty.value;
				}
				
				properties.put(property.name, new Property<>(property, validator, value));
			}
		}
	}
	
	public <T> void parseAndSetValue(Property<T> property, String value) {
		setValue(property, property.validator.parse(value));
	}
	
	public void parseAndSetValue(String property, String value) {
		parseAndSetValue(getProperty(property), value);
	}
	
	public <T> void parseAndSetValue(String property, PropertyValidator<T> validator, String value) {
		parseAndSetValue(new Property<>(property, validator, null), value);
	}
	
	public <T> void setValue(Property<T> property, T value) {
		properties.put(property.name, new Property<>(property, value));
	}
	
	public void updateIfExists(Property<?> property) {
		if (properties.containsKey(property.name)) {
			setProperty(property, true);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> Property<T> getProperty(String name) {
		return (Property<T>)properties.get(name);
	}
	
	public <T> T getValue(Property<T> property) {
		return getValue(property.name);
	}
	
	public <T> T getValue(String name) {
		return getValueOrDefault(name, null);
	}
	
	public <T> T getValueOrDefault(Property<T> property, T defaultValue) {
		return getValueOrDefault(property.name, defaultValue);
	}
	
	public <T> T getValueOrDefault(String name, T defaultValue) {
		if (properties.containsKey(name)) {
			return this.<T>getProperty(name).value;
		}
		
		return defaultValue;
	}
	
	private <T> PropertyValidator<T> chooseValidator(PropertyValidator<T> v1, PropertyValidator<T> v2) {
		if (v1 != null && v2 != null && !v1.equals(v2)) {
			throw new IllegalArgumentException("Property with the same name but different validator!");
		}
		
		return v1 == null ? v2 : v1;
	}
	
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
			if (properties.properties.containsKey(prop.name)) {
				if (!Objects.equals(getValue(prop), properties.getValue(prop))) {
					newProp.setValue(prop, null);
				} else {
					newProp.setProperty(prop);
				}
			}
		});
		return newProp;
	}
	
	@Override
	public int hashCode() {
		return properties.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Properties) {
			Properties props = (Properties)other;
			return properties.equals(props.properties);
		}
		
		return false;
	}
	
	public static final Property<String> LABEL;
	public static final Property<Direction> LABEL_LOCATION;
	public static final Property<Integer> BITSIZE;
	public static final Property<Integer> NUM_INPUTS;
	public static final Property<Integer> ADDRESS_BITS;
	public static final Property<Integer> SELECTOR_BITS;
	public static final Property<Direction> DIRECTION;
	public static final Property<Boolean> SELECTOR_LOCATION;
	public static final Property<Base> BASE;
	public static final Property<IntegerString> VALUE;
	
	public enum Direction {
		NORTH, SOUTH, EAST, WEST
	}
	
	public enum Base {
		BINARY, HEXADECIMAL, DECIMAL
	}
	
	static {
		LABEL = new Property<>("Label", PropertyValidators.ANY_STRING_VALIDATOR, "");
		
		LABEL_LOCATION =
			new Property<>("Label location", new PropertyListValidator<>(Direction.values()), Direction.NORTH);
		
		List<Integer> numInputsValues = new ArrayList<>();
		for (int i = 2; i <= 32; i++) {
			numInputsValues.add(i);
		}
		NUM_INPUTS = new Property<>("Number of Inputs", new PropertyListValidator<>(numInputsValues), 2);
		
		List<Integer> bitSizeValues = new ArrayList<>();
		for (int i = 1; i <= 32; i++) {
			bitSizeValues.add(i);
		}
		BITSIZE = new Property<>("Bitsize", new PropertyListValidator<>(bitSizeValues), 1);
		
		DIRECTION = new Property<>("Direction", new PropertyListValidator<>(Direction.values()), Direction.EAST);
		
		List<Integer> addressBits = new ArrayList<>();
		for (int i = 1; i <= 16; i++) {
			addressBits.add(i);
		}
		ADDRESS_BITS = new Property<>("Address bits", new PropertyListValidator<>(addressBits), 8);
		
		List<Integer> selBits = new ArrayList<>();
		for (int i = 1; i <= 8; i++) {
			selBits.add(i);
		}
		SELECTOR_BITS = new Property<>("Selector bits", new PropertyListValidator<>(selBits), 1);
		
		SELECTOR_LOCATION = new Property<>("Selector location", PropertyValidators.LOCATION_VALIDATOR, false);
		
		BASE = new Property<>("Base", "Display Base", new PropertyListValidator<>(Base.values()), Base.BINARY);
		
		VALUE = new Property<>(
			"Value",
			"Value",
			"Three input formats supported: decimal, hexadecimal (with 0x prefix), and binary " + "(with 0b prefix).",
			PropertyValidators.INTEGER_VALIDATOR,
			new IntegerString(0));
	}
	
	public static class Property<T> {
		public final String name;
		public String display;
		public final String helpText;
		public final PropertyValidator<T> validator;
		public final T value;
		
		public Property(Property<T> property) {
			this(property, property.value);
		}
		
		public Property(Property<T> property, T value) {
			this(property.name, property.display, property.helpText, property.validator, value);
		}
		
		public Property(Property<T> property, PropertyValidator<T> validator, T value) {
			this(property.name, property.display, property.helpText, validator, value);
		}
		
		public Property(String name, PropertyValidator<T> validator, T value) {
			this(name, name, validator, value);
		}
		
		public Property(String name, String displayName, PropertyValidator<T> validator, T value) {
			this(name, displayName, "", validator, value);
		}
		
		public Property(String name, String displayName, String helpText, PropertyValidator<T> validator, T value) {
			this.name = name;
			this.display = displayName;
			this.helpText = helpText;
			this.validator = validator;
			this.value = value;
		}
		
		public String getStringValue() {
			return validator == null ? (String)value : validator.toString(value);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(name, validator, value);
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof Property) {
				Property<?> prop = (Property<?>)other;
				return name.equals(prop.name) && validator.equals(prop.validator) && Objects.equals(value, prop.value);
			}
			
			return false;
		}
	}
	
	public interface PropertyValidator<T> {
		T parse(String value);
		
		default String toString(T value) {
			return value == null ? "" : value.toString();
		}
		
		default Node createGui(Stage stage, T value, Consumer<T> onAction) {
			TextField valueField = new TextField(toString(value));
			
			Runnable updateValue = () -> {
				String newValue = valueField.getText();
				if (!newValue.equals(value)) {
					try {
						onAction.accept(parse(newValue));
					} catch (Exception exc) {
						exc.printStackTrace();
						valueField.setText(toString(value));
					}
				}
			};
			
			valueField.setOnAction(event -> updateValue.run());
			valueField.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue) {
					updateValue.run();
				}
			});
			return valueField;
		}
	}
	
}
