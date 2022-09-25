package com.ra4king.circuitsim.gui.properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ra4king.circuitsim.gui.Properties.PropertyValidator;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

/**
 * @author Roi Atalla
 */
public class PropertyListValidator<T> implements PropertyValidator<T> {
	private final List<T> validValues;
	private final Function<T, String> toString;
	
	public PropertyListValidator(T[] validValues) {
		this(Arrays.asList(validValues));
	}
	
	public PropertyListValidator(List<T> validValues) {
		this(validValues, T::toString);
	}
	
	public PropertyListValidator(T[] validValues, Function<T, String> toString) {
		this(Arrays.asList(validValues), toString);
	}
	
	public PropertyListValidator(List<T> validValues, Function<T, String> toString) {
		this.validValues = Collections.unmodifiableList(validValues);
		this.toString = toString;
	}
	
	public List<T> getValidValues() {
		return validValues;
	}
	
	@Override
	public int hashCode() {
		List<String> values = validValues.stream().map(toString).collect(Collectors.toList());
		return validValues.hashCode() ^ values.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PropertyListValidator) {
			PropertyListValidator<?> validator = (PropertyListValidator<?>)other;
			return this.validValues.equals(validator.validValues);
		}
		
		return true;
	}
	
	@Override
	public T parse(String value) {
		for (T t : validValues) {
			if (toString.apply(t).equals(value)) {
				return t;
			}
		}
		
		throw new IllegalArgumentException("Value not found: " + value);
	}
	
	@Override
	public String toString(T value) {
		return value == null ? "" : toString.apply(value);
	}
	
	@Override
	public Node createGui(Stage stage, T value, Consumer<T> onAction) {
		ComboBox<String> valueList = new ComboBox<>();
		
		for (T t : validValues) {
			valueList.getItems().add(toString.apply(t));
		}
		
		valueList.setValue(toString(value));
		valueList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals(oldValue)) {
				try {
					onAction.accept(parse(newValue));
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		
		return valueList;
	}
}
