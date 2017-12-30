package com.ra4king.circuitsim.gui;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.scene.control.skin.TableHeaderRow;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author Roi Atalla
 */
public class Properties {
	private Map<String, Property<?>> properties;
	
	public Properties() {
		properties = new LinkedHashMap<>();
	}
	
	public Properties(Property<?>... props) {
		this();
		for(Property<?> prop : props) {
			setProperty(prop);
		}
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
	
	public void setProperty(Property<?> property) {
		setProperty(property, true);
	}
	
	private <T> void setProperty(Property<T> property, boolean overwriteValue) {
		if(!properties.containsKey(property.name)) {
			properties.put(property.name, property);
		} else {
			if(getProperty(property.name).validator == null) {
				parseAndSetValue(property, getProperty(property.name).value.toString());
			} else if(property.validator == null) {
				parseAndSetValue(getProperty(property.name), property.value.toString());
			} else {
				Property<T> ourProperty = getProperty(property.name);
				
				PropertyValidator<T> validator = chooseValidator(property.validator, ourProperty.validator);
				T value;
				if(overwriteValue) {
					value = property.value == null ? ourProperty.value : property.value;
				} else {
					value = ourProperty.value == null ? property.value : ourProperty.value;
				}
				
				properties.put(property.name, new Property<>(property.name, validator, value));
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
		if(properties.containsKey(property.name)) {
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
		if(properties.containsKey(name)) {
			return this.<T>getProperty(name).value;
		}
		
		return defaultValue;
	}
	
	private <T> PropertyValidator<T> chooseValidator(PropertyValidator<T> v1, PropertyValidator<T> v2) {
		if(v1 != null && v2 != null && !v1.equals(v2)) {
			throw new IllegalArgumentException("Property with the same name but different validator!");
		}
		
		return v1 == null ? v2 : v1;
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
				if(!Objects.equals(getValue(prop), properties.getValue(prop))) {
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
		if(other instanceof Properties) {
			Properties props = (Properties)other;
			return properties.equals(props.properties);
		}
		
		return false;
	}
	
	public static final PropertyValidator<String> ANY_STRING_VALIDATOR = value -> value;
	public static final PropertyValidator<Boolean> YESNO_VALIDATOR =
			new PropertyListValidator<>(new Boolean[] { true, false }, bool -> bool ? "Yes" : "No");
	public static final PropertyValidator<Integer> INTEGER_VALIDATOR = value -> {
		try {
			return Integer.parseInt(value);
		} catch(Exception exc) {
			String modified;
			if(value.startsWith("0x")) {
				modified = value.substring(2);
			} else if(value.startsWith("x")) {
				modified = value.substring(1);
			} else {
				throw new IllegalArgumentException(value + " is not a valid integer.");
			}
			
			try {
				return Integer.parseInt(modified, 16);
			} catch(Exception exc2) {
				throw new IllegalArgumentException(value + " is not a valid integer.");
			}
		}
	};
	public static final PropertyListValidator<Boolean> LOCATION_VALIDATOR =
			new PropertyListValidator<>(Arrays.asList(true, false), bool -> bool ? "Left/Top" : "Right/Down");
	public static final PropertyValidator<Color> COLOR_VALIDATOR = new PropertyValidator<Color>() {
		@Override
		public Color parse(String value) {
			return Color.valueOf(value);
		}
		
		@Override
		public Node createGui(Stage stage, Color value, Consumer<Color> onAction) {
			ColorPicker picker = new ColorPicker(value);
			picker.setOnAction(event -> onAction.accept(picker.getValue()));
			return picker;
		}
	};
	
	public static final Property<String> LABEL;
	public static final Property<Direction> LABEL_LOCATION;
	public static final Property<Integer> BITSIZE;
	public static final Property<Integer> NUM_INPUTS;
	public static final Property<Integer> ADDRESS_BITS;
	public static final Property<Integer> SELECTOR_BITS;
	public static final Property<Direction> DIRECTION;
	public static final Property<Boolean> SELECTOR_LOCATION;
	
	public enum Direction {
		NORTH, SOUTH, EAST, WEST
	}
	
	static {
		LABEL = new Property<>("Label", ANY_STRING_VALIDATOR, "");
		
		LABEL_LOCATION = new Property<>("Label location",
		                                new PropertyListValidator<>(Direction.values()),
		                                Direction.NORTH);
		
		List<Integer> numInputsValues = new ArrayList<>();
		for(int i = 2; i <= 32; i++) {
			numInputsValues.add(i);
		}
		NUM_INPUTS = new Property<>("Number of Inputs", new PropertyListValidator<>(numInputsValues), 2);
		
		List<Integer> bitSizeValues = new ArrayList<>();
		for(int i = 1; i <= 32; i++) {
			bitSizeValues.add(i);
		}
		BITSIZE = new Property<>("Bitsize", new PropertyListValidator<>(bitSizeValues), 1);
		
		DIRECTION = new Property<>("Direction", new PropertyListValidator<>(Direction.values()), Direction.EAST);
		
		List<Integer> addressBits = new ArrayList<>();
		for(int i = 1; i <= 16; i++) {
			addressBits.add(i);
		}
		ADDRESS_BITS = new Property<>("Address bits", new PropertyListValidator<>(addressBits), 8);
		
		List<Integer> selBits = new ArrayList<>();
		for(int i = 1; i <= 8; i++) {
			selBits.add(i);
		}
		SELECTOR_BITS = new Property<>("Selector bits", new PropertyListValidator<>(selBits), 1);
		
		SELECTOR_LOCATION = new Property<>("Selector location", LOCATION_VALIDATOR, false);
	}
	
	public static class Property<T> {
		public final String name;
		public String display;
		public final PropertyValidator<T> validator;
		public final T value;
		
		public Property(Property<T> property) {
			this(property.name, property.validator, property.value);
		}
		
		public Property(Property<T> property, T value) {
			this(property.name, property.validator, value);
		}
		
		public Property(String name, PropertyValidator<T> validator, T value) {
			this(name, name, validator, value);
		}
		
		public Property(String name, String displayName, PropertyValidator<T> validator, T value) {
			this.name = name;
			this.display = name;
			this.validator = validator;
			this.value = value;
		}
		
		public String getStringValue() {
			return validator.toString(value);
		}
		
		@Override
		public int hashCode() {
			return (name.hashCode() << 13) ^ validator.hashCode() ^ value.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof Property) {
				Property prop = (Property)other;
				return name.equals(prop.name) && validator.equals(prop.validator) && value.equals(prop.value);
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
			valueField.setOnAction(event -> {
				String newValue = valueField.getText();
				if(!newValue.equals(value)) {
					try {
						onAction.accept(parse(newValue));
					} catch(Exception exc) {
						exc.printStackTrace();
						valueField.setText(toString(value));
					}
				}
			});
			return valueField;
		}
	}
	
	public static class PropertyListValidator<T> implements PropertyValidator<T> {
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
			if(other instanceof PropertyListValidator) {
				PropertyListValidator validator = (PropertyListValidator)other;
				return this.validValues.equals(validator.validValues);
			}
			
			return true;
		}
		
		@Override
		public T parse(String value) {
			for(T t : validValues) {
				if(toString.apply(t).equals(value)) {
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
			
			for(T t : validValues) {
				valueList.getItems().add(toString.apply(t));
			}
			
			valueList.setValue(toString(value));
			valueList.getSelectionModel()
			         .selectedItemProperty()
			         .addListener((observable, oldValue, newValue) -> {
				         if(oldValue == null || !newValue.equals(oldValue)) {
					         try {
						         onAction.accept(parse(newValue));
					         } catch(Exception exc) {
						         exc.printStackTrace();
					         }
				         }
			         });
			
			return valueList;
		}
	}
	
	public static class PropertyCircuitValidator implements PropertyValidator<CircuitManager> {
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
			if(other instanceof PropertyCircuitValidator) {
				PropertyCircuitValidator validator = (PropertyCircuitValidator)other;
				return this.circuitSim == validator.circuitSim;
			}
			
			return false;
		}
		
		@Override
		public CircuitManager parse(String value) {
			if(circuitManager == null && circuitSim != null) {
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
	
	public static class PropertyMemoryValidator implements PropertyValidator<List<MemoryLine>> {
		private final int addressBits, dataBits;
		
		public PropertyMemoryValidator(int addressBits, int dataBits) {
			this.addressBits = addressBits;
			this.dataBits = dataBits;
		}
		
		public String parseValue(int value) {
			if(dataBits < 32) {
				value &= (1 << dataBits) - 1;
			}
			return String.format("%0" + (1 + (dataBits - 1) / 4) + "x", value);
		}
		
		public int parseValue(String value) {
			try {
				return Integer.parseUnsignedInt(value, 16);
			} catch(NumberFormatException exc) {
				throw new IllegalArgumentException("Cannot parse invalid hex value: " + value);
			}
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
		
		public List<MemoryLine> parse(int[] values, BiConsumer<Integer, Integer> memoryListener) {
			List<MemoryLine> lines = new ArrayList<>();
			
			int address = 0;
			MemoryLine currLine = null;
			for(int value : values) {
				if(currLine == null) {
					currLine = new MemoryLine(address);
				}
				
				SimpleStringProperty prop = new SimpleStringProperty(parseValue(value));
				
				if(memoryListener != null) {
					MemoryLine currMemoryLine = currLine;
					int currSize = currMemoryLine.values.size();
					prop.addListener(
							(observable, oldValue, newValue) ->
									memoryListener.accept(currMemoryLine.address + currSize, parseValue(newValue)));
				}
				
				currLine.values.add(prop);
				
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
				
				currLine.values.add(new SimpleStringProperty("0"));
				
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
		public List<MemoryLine> parse(String contents) {
			int[] values = new int[1 << addressBits];
			Scanner scanner = new Scanner(contents);
			for(int i = 0; i < values.length && scanner.hasNext(); i++) {
				String piece = scanner.next();
				if(piece.matches("^\\d+-[\\da-fA-F]+$")) {
					String[] split = piece.split("-");
					int count = Integer.parseInt(split[0]);
					int val = parseValue(split[1]);
					for(int j = 0; j < count && i < values.length; j++, i++) {
						values[i] = val;
					}
					i--; // to account for extra increment
				} else {
					values[i] = parseValue(piece);
				}
			}
			return parse(values, null);
		}
		
		@Override
		public String toString(List<MemoryLine> lines) {
			String values = String.join(" ", lines.stream().map(MemoryLine::toString).collect(Collectors.toList()));
			
			// expensive I know, but whatever...
			String[] split = values.split(" ");
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < split.length; ) {
				int count = 1;
				
				while((i + count) < split.length && split[i].equals(split[i + count])) {
					count++;
				}
				
				if(count == 1) {
					builder.append(split[i]);
				} else {
					builder.append(count).append('-').append(split[i]);
				}
				
				i += count;
				
				if(i < split.length) {
					builder.append(' ');
				}
			}
			
			return builder.length() < values.length() ? builder.toString() : values;
		}
		
		@Override
		public Node createGui(Stage stage, List<MemoryLine> value, Consumer<List<MemoryLine>> onAction) {
			Button button = new Button("Click to edit");
			button.setOnAction(event -> {
				List<MemoryLine> lines = value == null ? parse(new int[0], null) : value;
				createAndShowMemoryWindow(stage, lines);
				onAction.accept(lines);
			});
			return button;
		}
		
		public void createAndShowMemoryWindow(Stage stage, List<MemoryLine> lines) {
			Stage memoryStage = new Stage();
			memoryStage.initOwner(stage);
			memoryStage.setTitle("Modify memory");
			
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
			
			int columns = Math.min(1 << addressBits, 16);
			for(int i = 0; i < columns; i++) {
				int j = i;
				
				TableColumn<MemoryLine, String> column = new TableColumn<>(String.format("%x", i));
				column.setStyle("-fx-alignment: CENTER;");
				column.setSortable(false);
				column.setEditable(true);
				column.setCellValueFactory(param -> param.getValue().get(j));
				column.setCellFactory(TextFieldTableCell.forTableColumn());
				column.setOnEditCommit(t -> {
					try {
						String newValue = parseValue(parseValue(t.getNewValue()));
						t.getTableView().getItems().get(t.getTablePosition().getRow()).values.get(j).set(newValue);
					} catch(IllegalArgumentException exc) {
						// ignore
					}
					
					// refresh the column
					column.setVisible(false);
					column.setVisible(true);
				});
				
				tableView.getColumns().add(column);
			}
			
			tableView.setItems(new ObservableListWrapper<>(lines));
			
			Button loadButton = new Button("Load from file");
			loadButton.setOnAction(event -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Choose save file");
				fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
				File selectedFile = fileChooser.showOpenDialog(memoryStage);
				if(selectedFile != null) {
					try {
						List<String> strings = Files.readAllLines(selectedFile.toPath());
						List<MemoryLine> fileLines = parse(String.join(" ", strings));
						for(int i = 0; i < fileLines.size(); i++) {
							MemoryLine fileLine = fileLines.get(i);
							MemoryLine tableLine = lines.get(i);
							
							for(int j = 0; j < fileLine.values.size() && j < tableLine.values.size(); j++) {
								tableLine.values.get(j).set(fileLine.values.get(j).get());
							}
						}
					} catch(Exception exc) {
						new Alert(AlertType.ERROR, "Could not open file").showAndWait();
					}
				}
			});
			Button saveButton = new Button("Save to file");
			saveButton.setOnAction(event -> {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Choose save file");
				fileChooser.setInitialFileName("Memory.dat");
				File selectedFile = fileChooser.showSaveDialog(memoryStage);
				if(selectedFile != null) {
					List<String> strings = lines.stream().map(MemoryLine::toString).collect(Collectors.toList());
					try {
						Files.write(selectedFile.toPath(), strings);
					} catch(Exception exc) {
						new Alert(AlertType.ERROR, "Could not open file").showAndWait();
					}
				}
			});
			
			VBox.setVgrow(tableView, Priority.ALWAYS);
			Platform.runLater(tableView::refresh);
			
			memoryStage.setScene(new Scene(new VBox(new HBox(loadButton, saveButton), tableView)));
			memoryStage.sizeToScene();
			memoryStage.showAndWait();
		}
	}
	
	public static class MemoryLine {
		public final int address;
		public final List<StringProperty> values;
		
		public MemoryLine(int address) {
			this.address = address;
			values = new ArrayList<>(16);
		}
		
		public StringProperty get(int index) {
			if(index < values.size()) {
				return values.get(index);
			}
			
			return new SimpleStringProperty("");
		}
		
		@Override
		public String toString() {
			return String.join(" ", values.stream().map(StringProperty::get).collect(Collectors.toList()));
		}
	}
}
