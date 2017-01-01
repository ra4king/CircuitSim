package com.ra4king.circuitsimulator.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ra4king.circuitsimulator.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsimulator.gui.file.FileFormat;
import com.ra4king.circuitsimulator.gui.file.FileFormat.CircuitInfo;
import com.ra4king.circuitsimulator.gui.file.FileFormat.ComponentInfo;
import com.ra4king.circuitsimulator.gui.file.FileFormat.WireInfo;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * @author Roi Atalla
 */
public class CircuitSimulator extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	private Simulator simulator;
	
	private ComponentManager componentManager;
	
	private TabPane buttonTabPane;
	private ToggleGroup buttonsToggleGroup;
	
	private ComboBox<Integer> bitSizeSelect, secondaryOptionSelect;
	private GridPane propertiesTable;
	
	private TabPane canvasTabPane;
	private HashMap<Tab, CircuitManager> circuitManagers;
	
	private String componentMode;
	
	@Override
	public void init() {
		simulator = new Simulator();
		circuitManagers = new HashMap<>();
		Clock.addChangeListener(value -> {
			try {
				getCurrentCircuit().getCircuitBoard().runSim();
			} catch(Exception exc) {
			}
			getCurrentCircuit().repaint();
		});
	}
	
	private CircuitManager getCurrentCircuit() {
		return circuitManagers.get(canvasTabPane.getSelectionModel().getSelectedItem());
	}
	
	public ComponentManager getComponentManager() {
		return componentManager;
	}
	
	public void clearSelection() {
		if(buttonsToggleGroup.getSelectedToggle() != null) {
			buttonsToggleGroup.getSelectedToggle().setSelected(false);
		}
		componentMode = null;
	}
	
	public void setProperties(Properties properties) {
		propertiesTable.getChildren().clear();
		
		if(properties != null) {
			properties.forEach(property -> {
				int size = propertiesTable.getChildren().size();
				
				Label name = new Label(property.name);
				GridPane.setHgrow(name, Priority.ALWAYS);
				name.setMaxWidth(Double.MAX_VALUE);
				name.setMinHeight(30);
				name.setBackground(new Background(new BackgroundFill((size / 2) % 2 == 0 ? Color.LIGHTGRAY
				                                                                         : Color.WHITE, null, null)));
				
				Node value;
				if(property.validator instanceof PropertyListValidator) {
					ComboBox<String> valueList = new ComboBox<>();
					
					for(String entry : ((PropertyListValidator)property.validator).validValues) {
						valueList.getItems().add(entry);
					}
					valueList.setValue(property.value);
					valueList.getSelectionModel()
					         .selectedItemProperty()
					         .addListener((observable, oldValue, newValue) -> {
						         if(oldValue == null || !newValue.equals(oldValue)) {
							         Properties newProperties = new Properties(properties);
							         newProperties.setValue(property, newValue);
							         modifiedSelection(newProperties);
						         }
					         });
					value = valueList;
				} else {
					TextField valueField = new TextField(property.value);
					valueField.setOnAction(event -> {
						String newValue = valueField.getText();
						if(!newValue.equals(property.value)) {
							Properties newProperties = new Properties(properties);
							newProperties.setValue(property, newValue);
							modifiedSelection(newProperties);
						}
					});
					value = valueField;
				}
				
				Pane valuePane = new Pane(value);
				valuePane.setBackground(
						new Background(new BackgroundFill((size / 2) % 2 == 0 ? Color.LIGHTGRAY
						                                                      : Color.WHITE, null, null)));
				
				GridPane.setHgrow(valuePane, Priority.ALWAYS);
				propertiesTable.addRow(size, name, valuePane);
			});
		}
	}
	
	private void modifiedSelection() {
		modifiedSelection(new Properties());
	}
	
	private void modifiedSelection(Properties properties) {
		CircuitManager current = getCurrentCircuit();
		if(current != null) {
			Tab tab = buttonTabPane.getSelectionModel().getSelectedItem();
			String group = tab == null ? null : tab.getText();
			setProperties(
					current.modifiedSelection(componentManager.getComponentCreator(group, componentMode),
					                          properties));
			current.repaint();
		}
	}
	
	private ToggleButton setupButton(ToggleGroup group, String componentName) {
		ToggleButton button = new ToggleButton(componentName);
		group.getToggles().add(button);
		button.setMinHeight(30);
		button.setMaxWidth(Double.MAX_VALUE);
		button.addEventHandler(ActionEvent.ACTION, (e) -> {
			if(componentMode != componentName) {
				componentMode = componentName;
			} else {
				componentMode = null;
			}
			modifiedSelection();
		});
		return button;
	}
	
	private CircuitManager createTab(String name) {
		Canvas canvas = new Canvas(800, 600) {
			{
				widthProperty().addListener(evt -> {
					CircuitManager manager = getCurrentCircuit();
					if(manager != null) {
						manager.repaint();
					}
				});
				heightProperty().addListener(evt -> {
					CircuitManager manager = getCurrentCircuit();
					if(manager != null) {
						manager.repaint();
					}
				});
			}
			
			@Override
			public boolean isResizable() {
				return true;
			}
			
			@Override
			public double prefWidth(double width) {
				return getWidth();
			}
			
			@Override
			public double prefHeight(double height) {
				return getHeight();
			}
		};
		
		canvas.widthProperty().bind(canvasTabPane.widthProperty());
		canvas.heightProperty().bind(canvasTabPane.heightProperty());
		
		canvas.addEventHandler(MouseEvent.ANY, e -> canvas.requestFocus());
		canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::mouseClicked);
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
		canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
		canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
		canvas.addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
		canvas.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
		canvas.addEventHandler(KeyEvent.KEY_TYPED, this::keyTyped);
		canvas.addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);
		
		Tab canvasTab = new Tab(name, canvas);
		canvasTab.setOnCloseRequest(event -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete this circuit?");
			alert.setHeaderText("Delete this circuit?");
			alert.setContentText("Are you sure you want to delete this circuit?");
			
			Optional<ButtonType> result = alert.showAndWait();
			if(!result.isPresent() || result.get() != ButtonType.OK) {
				event.consume();
			} else {
				CircuitManager manager = circuitManagers.remove(canvasTab);
				manager.getCircuitBoard().clear();
			}
		});
		
		CircuitManager circuitManager = new CircuitManager(this, canvas, simulator);
		circuitManagers.put(canvasTab, circuitManager);
		componentManager.addCircuit(name, circuitManager);
		
		canvasTabPane.getTabs().addAll(canvasTab);
		
		return circuitManager;
	}
	
	@Override
	public void start(Stage stage) {
		componentManager = new ComponentManager();
		
		buttonTabPane = new TabPane();
		propertiesTable = new GridPane();
		propertiesTable.setMaxWidth(Double.MAX_VALUE);
		
		bitSizeSelect = new ComboBox<>();
		for(int i = 1; i <= 32; i++) {
			bitSizeSelect.getItems().add(i);
		}
		bitSizeSelect.setMaxWidth(Double.MAX_VALUE);
		bitSizeSelect.setValue(1);
		bitSizeSelect.getSelectionModel()
		             .selectedItemProperty()
		             .addListener((observable, oldValue, newValue) -> modifiedSelection());
		
		secondaryOptionSelect = new ComboBox<>();
		for(int i = 1; i <= 8; i++) {
			secondaryOptionSelect.getItems().add(i);
		}
		secondaryOptionSelect.setMaxWidth(Double.MAX_VALUE);
		secondaryOptionSelect.setValue(1);
		secondaryOptionSelect.getSelectionModel()
		                     .selectedItemProperty()
		                     .addListener((observable, oldValue, newValue) -> modifiedSelection());
		
		canvasTabPane = new TabPane();
		canvasTabPane.setPrefWidth(800);
		canvasTabPane.setPrefHeight(600);
		canvasTabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		canvasTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			CircuitManager oldManager = circuitManagers.get(oldValue);
			CircuitManager newManager = circuitManagers.get(newValue);
			if(oldManager != null && newManager != null) {
				newManager.setLastMousePosition(oldManager.getLastMousePosition());
			}
			
			modifiedSelection();
		});
		
		for(int i = 0; i < 3; i++) {
			createTab("Circuit " + i);
		}
		
		buttonsToggleGroup = new ToggleGroup();
		Map<String, Tab> buttonTabs = new HashMap<>();
		
		for(Pair<String, String> component : componentManager.getComponentNames()) {
			Tab tab;
			if(buttonTabs.containsKey(component.first)) {
				tab = buttonTabs.get(component.first);
			} else {
				tab = new Tab(component.first);
				tab.setClosable(false);
				tab.setContent(new GridPane());
				buttonTabPane.getTabs().add(tab);
				buttonTabs.put(component.first, tab);
			}
			
			GridPane buttons = (GridPane)tab.getContent();
			
			ToggleButton toggleButton = setupButton(buttonsToggleGroup, component.second);
			GridPane.setHgrow(toggleButton, Priority.ALWAYS);
			buttons.addRow(buttons.getChildren().size(), toggleButton);
		}
//		
//		Label bitSizeLabel = new Label("Bit size:");
//		Label secondaryLabel = new Label("Secondary:");
//		GridPane.setHalignment(bitSizeLabel, HPos.CENTER);
//		GridPane.setHalignment(secondaryLabel, HPos.CENTER);
//		buttons.addRow(count++, bitSizeLabel, secondaryLabel);
//		buttons.addRow(count, bitSizeSelect, secondaryOptionSelect);
		
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("File");
		MenuItem load = new MenuItem("Load");
		load.setAccelerator(new KeyCharacterCombination("O", KeyCombination.CONTROL_DOWN));
		load.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose save file");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Circuit Sim file", "*.sim"));
			File selectedFile = fileChooser.showOpenDialog(stage);
			if(selectedFile != null) {
				List<CircuitInfo> circuits = FileFormat.load(selectedFile);
				
				circuitManagers.values().forEach(manager -> manager.getCircuitBoard().clear());
				circuitManagers.clear();
				canvasTabPane.getTabs().clear();
				
				for(CircuitInfo circuit : circuits) {
					CircuitManager manager = createTab(circuit.name);
					
					for(ComponentInfo component : circuit.components) {
						try {
							@SuppressWarnings("unchecked")
							Class<? extends ComponentPeer<?>> clazz =
									(Class<? extends ComponentPeer<?>>)Class.forName(component.className);
							manager.getCircuitBoard().createComponent(ComponentManager.forClass(clazz),
							                                          component.properties, component.x, component.y);
						} catch(Exception exc) {
							exc.printStackTrace();
						}
					}
					
					for(WireInfo wire : circuit.wires) {
						try {
							manager.getCircuitBoard().addWire(wire.x, wire.y, wire.length, wire.isHorizontal);
						} catch(Exception exc) {
							exc.printStackTrace();
						}
					}
				}

//				for(CircuitInfo circuit : circuits) {
//					System.out.println(circuit.name);
//					
//					for(ComponentInfo component : circuit.components) {
//						System.out.println("  Component:");
//						System.out.println("    name = " + component.className);
//						System.out.println("    x    = " + component.x);
//						System.out.println("    y    = " + component.x);
//					}
//					
//					for(WireInfo wire : circuit.wires) {
//						System.out.println("  Wire:");
//						System.out.println("    x       = " + wire.x);
//						System.out.println("    y       = " + wire.y);
//						System.out.println("    length  = " + wire.length);
//						System.out.println("    isHoriz = " + wire.isHorizontal);
//					}
//				}
			}
		});
		MenuItem save = new MenuItem("Save");
		save.setAccelerator(new KeyCharacterCombination("S", KeyCombination.CONTROL_DOWN));
		save.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose save file");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Circuit Sim file", "*.sim"));
			File selectedFile = fileChooser.showOpenDialog(stage);
			if(selectedFile != null) {
				List<CircuitInfo> circuits = new ArrayList<>();
				
				canvasTabPane.getTabs().forEach(tab -> {
					CircuitManager manager = circuitManagers.get(tab);
					
					String name = tab.getText();
					Set<ComponentInfo> components =
							manager.getCircuitBoard()
							       .getComponents().stream()
							       .map(component ->
									            new ComponentInfo(component.getClass().getName(),
									                              component.getX(), component.getY(),
									                              component.getProperties())).collect(
									Collectors.toSet());
					Set<WireInfo> wires = manager.getCircuitBoard()
					                             .getLinks().stream()
					                             .flatMap(linkWires -> linkWires.getWires().stream())
					                             .map(wire -> new WireInfo(wire.getX(), wire.getY(),
					                                                       wire.getLength(), wire.isHorizontal()))
					                             .collect(Collectors.toSet());
					
					circuits.add(new CircuitInfo(name, components, wires));
				});
				
				FileFormat.save(selectedFile, circuits);
				
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Circuits saved.");
				alert.setHeaderText("Circuits saved");
				alert.setContentText("Circuits have successfully been saved.");
				alert.show();
			}
		});
		fileMenu.getItems().add(load);
		fileMenu.getItems().add(save);
		menuBar.getMenus().add(fileMenu);
		
		VBox vBox = new VBox(buttonTabPane, propertiesTable);
		VBox.setVgrow(buttonTabPane, Priority.ALWAYS);
		VBox.setVgrow(propertiesTable, Priority.ALWAYS);
		
		HBox hBox = new HBox(vBox, canvasTabPane);
		HBox.setHgrow(canvasTabPane, Priority.ALWAYS);
		
		Scene scene = new Scene(new VBox(menuBar, hBox));
		
		stage.setScene(scene);
		stage.setTitle("Circuit Simulator");
		stage.sizeToScene();
		stage.show();
		stage.centerOnScreen();
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
			case DIGIT0:
			case DIGIT1:
			case DIGIT2:
			case DIGIT3:
			case DIGIT4:
			case DIGIT5:
			case DIGIT6:
			case DIGIT7:
			case DIGIT8:
			case DIGIT9:
				int value = e.getText().charAt(0) - '0';
				if(value > 0) {
					Platform.runLater(() -> bitSizeSelect.setValue(value));
				}
				break;
			case NUMPAD0:
			case NUMPAD1:
			case NUMPAD2:
			case NUMPAD3:
			case NUMPAD4:
			case NUMPAD5:
			case NUMPAD6:
			case NUMPAD7:
			case NUMPAD8:
			case NUMPAD9:
				value = e.getText().charAt(0) - '0';
				Platform.runLater(() -> secondaryOptionSelect.setValue(value));
				break;
			case SPACE:
				if(Clock.isRunning()) {
					Clock.stopClock();
				} else {
					Clock.startClock(secondaryOptionSelect.getValue());
				}
				break;
			case ESCAPE:
				clearSelection();
				modifiedSelection();
				break;
		}
		
		getCurrentCircuit().keyPressed(e);
	}
	
	public void keyReleased(KeyEvent e) {
		getCurrentCircuit().keyReleased(e);
	}
	
	public void keyTyped(KeyEvent e) {}
	
	public void mouseClicked(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e) {
		getCurrentCircuit().mousePressed(e);
	}
	
	public void mouseReleased(MouseEvent e) {
		getCurrentCircuit().mouseReleased(e);
	}
	
	public void mouseMoved(MouseEvent e) {
		getCurrentCircuit().mouseMoved(e);
	}
	
	public void mouseDragged(MouseEvent e) {
		getCurrentCircuit().mouseDragged(e);
	}
	
	public void mouseEntered(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {}
}
