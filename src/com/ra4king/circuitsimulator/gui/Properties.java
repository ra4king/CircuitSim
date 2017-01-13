package com.ra4king.circuitsimulator.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.scene.control.skin.TableHeaderRow;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

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
		
		default Node createGui(String value, Consumer<String> onAction) {
			TextField valueField = new TextField(value);
			valueField.setOnAction(event -> {
				String newValue = valueField.getText();
				if(!newValue.equals(value)) {
					onAction.accept(newValue);
				}
			});
			return valueField;
		}
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
		
		@Override
		public Node createGui(String value, Consumer<String> onAction) {
			ComboBox<String> valueList = new ComboBox<>();
			
			for(String entry : validValues) {
				valueList.getItems().add(entry);
			}
			
			valueList.setValue(value);
			valueList.getSelectionModel()
			         .selectedItemProperty()
			         .addListener((observable, oldValue, newValue) -> {
				         if(oldValue == null || !newValue.equals(oldValue)) {
					         onAction.accept(newValue);
				         }
			         });
			
			return valueList;
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
		
		@Override
		public Node createGui(String value, Consumer<String> onAction) {
			return null;
		}
		
		public CircuitManager getCircuitManager(String value) {
			validate(value);
			return circuitManager;
		}
	}
	
	public static class PropertyMemoryValidator implements PropertyValidator {
		private final int addressBits, dataBits;
		
		public PropertyMemoryValidator(int addressBits, int dataBits) {
			this.addressBits = addressBits;
			this.dataBits = dataBits;
		}
		
		public int[] parseToArray(String memory) {
			int[] values = new int[1 << addressBits];
			
			List<MemoryLine> lines = parseMemory(memory);
			for(MemoryLine line : lines) {
				for(int j = 0; j < line.values.size(); j++) {
					values[line.address + j] = Integer.parseUnsignedInt(line.values.get(j), 16);
				}
			}
			
			return values;
		}
		
		public String parseToString(int[] memory) {
			return Arrays.stream(memory)
			             .mapToObj(value -> String.format("%0" + (1 + (dataBits - 1) / 4) + "x", value))
			             .reduce("", (s, s2) -> s + " " + s2);
		}
		
		private String parseValue(String value) {
			int hex;
			try {
				hex = Integer.parseUnsignedInt(value, 16);

//				if(dataBits < 32 && Integer.highestOneBit(hex) >= (1 << dataBits)) {
//					throw new IllegalArgumentException("Value does not fit in " + dataBits + " bits: " + value);
//				}
				
				hex &= (1 << dataBits) - 1;
			} catch(NumberFormatException exc) {
				throw new IllegalArgumentException("Cannot parse invalid hex value: " + value);
			}
			
			return String.format("%0" + (1 + (dataBits - 1) / 4) + "x", hex);
		}
		
		private List<MemoryLine> parseMemory(String memory) {
			List<MemoryLine> lines = new ArrayList<>();
			
			int address = 0;
			MemoryLine currLine = null;
			Scanner scanner = new Scanner(memory);
			while(scanner.hasNext()) {
				if(currLine == null) {
					currLine = new MemoryLine(address);
				}
				
				currLine.values.add(parseValue(scanner.next()));
				
				if(currLine.values.size() == 16) {
					lines.add(currLine);
					currLine = null;
					address += 16;
				}
			}
			
			while(address < (1 << addressBits)) {
				if(currLine == null) {
					currLine = new MemoryLine(address);
				}
				
				currLine.values.add(parseValue("0"));
				
				if(currLine.values.size() == 16) {
					lines.add(currLine);
					currLine = null;
					address += 16;
				}
			}
			
			if(currLine != null) {
				lines.add(currLine);
			}
			
			return lines;
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof PropertyMemoryValidator) {
				PropertyMemoryValidator validator = (PropertyMemoryValidator)other;
				return validator.addressBits == this.addressBits
						       && validator.dataBits == this.dataBits;
			}
			
			return false;
		}
		
		@Override
		public boolean validate(String value) {
			try {
				parseMemory(value);
				return true;
			} catch(IllegalArgumentException exc) {
				return false;
			}
		}
		
		@Override
		public Node createGui(String value, Consumer<String> onAction) {
			Button button = new Button("Click to edit");
			button.setOnAction(event -> onAction.accept(createAndShowMemoryWindow(value)));
			return button;
		}
		
		public String createAndShowMemoryWindow(String value) {
			List<MemoryLine> lines = parseMemory(value);
			createAndShowMemoryWindow(lines);
			return String.join(" ", lines.stream().map(MemoryLine::toString).collect(Collectors.toList()));
		}
		
		public void createAndShowMemoryWindow(int[] memory) {
			int[] newMemory = parseToArray(createAndShowMemoryWindow(parseToString(memory)));
			System.arraycopy(newMemory, 0, memory, 0, memory.length);
		}
		
		private void createAndShowMemoryWindow(List<MemoryLine> lines) {
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Modify memory");
			
			TableView<MemoryLine> tableView = new TableView<>();
			tableView.getSelectionModel().setCellSelectionEnabled(true);
			tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			tableView.setEditable(true);
			tableView.widthProperty().addListener((source, oldWidth, newWidth) -> {
				TableHeaderRow header = (TableHeaderRow)tableView.lookup("TableHeaderRow");
				header.reorderingProperty().addListener(
						(observable, oldValue, newValue) -> header.setReordering(false));
			});
			
			TableColumn<MemoryLine, String> address = new TableColumn<>("Address");
			address.setStyle("-fx-alignment: CENTER-RIGHT;");
			address.setSortable(false);
			address.setEditable(false);
			address.setCellValueFactory(
					param -> new SimpleStringProperty(String.format("%0" + (1 + (addressBits - 1) / 4) + "x",
					                                                param.getValue().address)));
			tableView.getColumns().add(address);
			
			for(int i = 0; i < 16; i++) {
				int j = i;
				
				TableColumn<MemoryLine, String> column = new TableColumn<>(String.format("%x", i));
				column.setStyle("-fx-alignment: CENTER;");
				column.setSortable(false);
				column.setEditable(true);
				column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
				column.setCellFactory(TextFieldTableCell.forTableColumn());
				column.setOnEditCommit(t -> {
					try {
						String newValue = parseValue(t.getNewValue());
						t.getTableView().getItems().get(t.getTablePosition().getRow()).values.set(j, newValue);
					} catch(IllegalArgumentException exc) {
					}
					
					// refresh the column
					column.setVisible(false);
					column.setVisible(true);
				});
				
				tableView.getColumns().add(column);
			}
			
			tableView.setItems(new ObservableListWrapper<>(lines));
			
			dialog.getDialogPane().setContent(tableView);
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
			dialog.setResizable(true);
			
			dialog.showAndWait();
		}
		
		private static class MemoryLine {
			private int address;
			private List<String> values;
			
			private MemoryLine(int address) {
				this.address = address;
				values = new ArrayList<>(16);
			}
			
			private String get(int index) {
				if(index < values.size()) {
					return values.get(index);
				}
				
				return "";
			}
			
			@Override
			public String toString() {
				return String.join(" ", values);
			}
		}
	}
}
