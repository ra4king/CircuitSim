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
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
		new Thread(FileFormat::init).start();
		launch(args);
	}
	
	private Simulator simulator;
	
	private ComponentManager componentManager;
	
	private TabPane buttonTabPane;
	private ToggleGroup buttonsToggleGroup;
	
	private ComboBox<Integer> bitSizeSelect, secondaryOptionSelect;
	private GridPane propertiesTable;
	
	private Tab circuitButtonsTab;
	private TabPane canvasTabPane;
	private HashMap<String, CircuitManager> circuitManagers;
	
	private String componentMode;
	
	private File saveFile;
	
	private int currentClockHz = 1;
	
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
		Tab tab = canvasTabPane.getSelectionModel().getSelectedItem();
		return tab == null ? null : circuitManagers.get(tab.getText());
	}
	
	public CircuitManager getCircuitManager(String name) {
		return circuitManagers.get(name);
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
		GridPane.setHgrow(button, Priority.ALWAYS);
		return button;
	}
	
	private void refreshCircuitsTab() {
		if(circuitButtonsTab == null) {
			circuitButtonsTab = new Tab("Circuits");
			circuitButtonsTab.setClosable(false);
			circuitButtonsTab.setContent(new GridPane());
			buttonTabPane.getTabs().add(circuitButtonsTab);
		} else {
			circuitButtonsTab.setContent(new GridPane());
		}
		
		for(Pair<String, String> component : componentManager.getComponentNames()) {
			if(component.first.equals("Circuits")) {
				GridPane buttons = (GridPane)circuitButtonsTab.getContent();
				
				ToggleButton toggleButton = setupButton(buttonsToggleGroup, component.second);
				buttons.addRow(buttons.getChildren().size(), toggleButton);
			}
		}
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
		
		CircuitManager circuitManager = new CircuitManager(this, canvas, simulator);
		for(int count = 0; ; count++) {
			try {
				String n = name;
				if(count > 0) {
					n += count;
				}
				
				componentManager.addCircuit(n, circuitManager);
				name = n;
				break;
			} catch(Exception exc) {
			}
		}
		
		Tab canvasTab = new Tab(name, canvas);
		MenuItem rename = new MenuItem("Rename");
		rename.setOnAction(event -> {
			String lastTyped = canvasTab.getText();
			while(true) {
				try {
					TextInputDialog dialog = new TextInputDialog(lastTyped);
					dialog.setTitle("Rename circuit");
					dialog.setHeaderText("Rename circuit");
					dialog.setContentText("Enter new name:");
					Optional<String> value = dialog.showAndWait();
					if(value.isPresent() && !(lastTyped = value.get().trim()).isEmpty()
							   && !lastTyped.equals(canvasTab.getText())) {
						componentManager.renameCircuit(canvasTab.getText(), lastTyped);
						canvasTab.setText(value.get());
						
						refreshCircuitsTab();
					}
					break;
				} catch(Exception exc) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Duplicate name");
					alert.setHeaderText("Duplicate name");
					alert.setContentText("Name already exists, please choose a new name.");
					alert.showAndWait();
				}
			}
		});
		canvasTab.setContextMenu(new ContextMenu(rename));
		canvasTab.setOnCloseRequest(event -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete this circuit?");
			alert.setHeaderText("Delete this circuit?");
			alert.setContentText("Are you sure you want to delete this circuit?");
			
			Optional<ButtonType> result = alert.showAndWait();
			if(!result.isPresent() || result.get() != ButtonType.OK) {
				event.consume();
			} else {
				CircuitManager manager = circuitManagers.remove(canvasTab.getText());
				manager.getCircuitBoard().clear();
				componentManager.removeCircuit(canvasTab.getText());
				
				if(canvasTabPane.getTabs().size() == 1) {
					createTab("New circuit");
				} else {
					refreshCircuitsTab();
				}
			}
		});
		
		circuitManagers.put(canvasTab.getText(), circuitManager);
		canvasTabPane.getTabs().addAll(canvasTab);
		
		refreshCircuitsTab();
		
		return circuitManager;
	}
	
	@Override
	public void start(Stage stage) {
		componentManager = new ComponentManager(this);
		
		buttonTabPane = new TabPane();
		buttonTabPane.setMinWidth(100);
		
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
		canvasTabPane.setMaxWidth(Double.MAX_VALUE);
		canvasTabPane.setMaxHeight(Double.MAX_VALUE);
		canvasTabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		canvasTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			CircuitManager oldManager = oldValue == null ? null : circuitManagers.get(oldValue.getText());
			CircuitManager newManager = newValue == null ? null : circuitManagers.get(newValue.getText());
			if(oldManager != null && newManager != null) {
				newManager.setLastMousePosition(oldManager.getLastMousePosition());
			}
			
			modifiedSelection();
		});
		
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
			buttons.addRow(buttons.getChildren().size(), toggleButton);
		}
		
		createTab("New circuit");
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
			fileChooser.setTitle("Choose sim file");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Circuit Sim file", "*.sim"));
			File selectedFile = fileChooser.showOpenDialog(stage);
			if(selectedFile != null) {
				saveFile = selectedFile;
				
				try {
					List<CircuitInfo> circuits = FileFormat.load(selectedFile);
					
					componentManager.clearCircuits();
					circuitManagers.values().forEach(manager -> manager.getCircuitBoard().clear());
					circuitManagers.clear();
					canvasTabPane.getTabs().clear();
					
					
					for(CircuitInfo circuit : circuits) {
						createTab(circuit.name);
					}
					
					for(CircuitInfo circuit : circuits) {
						CircuitManager manager = getCircuitManager(circuit.name);
						
						for(ComponentInfo component : circuit.components) {
							try {
								@SuppressWarnings("unchecked")
								Class<? extends ComponentPeer<?>> clazz =
										(Class<? extends ComponentPeer<?>>)Class.forName(component.className);
								manager.getCircuitBoard().createComponent(componentManager.forClass(clazz),
								                                          component.properties, component.x,
								                                          component.y);
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
					
					if(circuits.size() == 0) {
						createTab("New circuit");
					}
				} catch(Exception exc) {
					exc.printStackTrace();
					
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error loading circuits");
					alert.setHeaderText("Error loading circuits");
					alert.setContentText("Error when loading circuits file: " + exc.getMessage()
							                     + "\nPlease send the stack trace to a developer.");
					alert.show();
				}
			}
		});
		
		MenuItem save = new MenuItem("Save");
		save.setAccelerator(new KeyCharacterCombination("S", KeyCombination.CONTROL_DOWN));
		save.setOnAction(event -> {
			if(saveFile == null) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Choose sim file");
				fileChooser.getExtensionFilters().add(new ExtensionFilter("Circuit Sim file", "*.sim"));
				saveFile = fileChooser.showSaveDialog(stage);
			}
			
			if(saveFile != null) {
				List<CircuitInfo> circuits = new ArrayList<>();
				
				canvasTabPane.getTabs().forEach(tab -> {
					String name = tab.getText();
					
					CircuitManager manager = circuitManagers.get(name);
					
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
				
				try {
					FileFormat.save(saveFile, circuits);
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Circuits saved");
					alert.setHeaderText("Circuits saved");
					alert.setContentText("Circuits have successfully been saved.");
					alert.show();
				} catch(Exception exc) {
					exc.printStackTrace();
					
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error saving circuit");
					alert.setHeaderText("Error saving circuit.");
					alert.setContentText("Error when saving the circuits: " + exc.getMessage());
					alert.show();
				}
			}
		});
		
		MenuItem saveAs = new MenuItem("Save as");
		saveAs.setAccelerator(new KeyCharacterCombination("S", KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
		saveAs.setOnAction(event -> {
			File oldSave = saveFile;
			
			saveFile = null;
			save.fire();
			
			if(saveFile == null) {
				saveFile = oldSave;
			}
		});
		
		fileMenu.getItems().addAll(load, save, saveAs);
		
		Menu circuitsMenu = new Menu("Circuits");
		MenuItem newCircuit = new MenuItem("New circuit");
		newCircuit.setOnAction(event -> createTab("New circuit"));
		circuitsMenu.getItems().add(newCircuit);
		
		Menu clockMenu = new Menu("Clock");
		MenuItem startClock = new MenuItem("Start clock");
		startClock.setAccelerator(new KeyCharacterCombination("K", KeyCombination.CONTROL_DOWN));
		startClock.setOnAction(event -> {
			if(startClock.getText().startsWith("Start")) {
				Clock.startClock(currentClockHz);
				startClock.setText("Stop clock");
			} else {
				Clock.stopClock();
				startClock.setText("Start clock");
			}
		});
		
		MenuItem tickClock = new MenuItem("Tick clock");
		tickClock.setAccelerator(new KeyCharacterCombination("T", KeyCombination.CONTROL_DOWN));
		tickClock.setOnAction(event -> Clock.tick());
		
		Menu frequenciesMenu = new Menu("Frequency");
		ToggleGroup freqToggleGroup = new ToggleGroup();
		for(int i = 0; i <= 10; i++) {
			RadioMenuItem freq = new RadioMenuItem((1 << i) + " Hz");
			freq.setToggleGroup(freqToggleGroup);
			freq.setSelected(i == 0);
			final int j = i;
			freq.setOnAction(event -> {
				currentClockHz = 1 << j;
				if(Clock.isRunning()) {
					Clock.startClock(currentClockHz);
				}
			});
			frequenciesMenu.getItems().add(freq);
		}
		
		clockMenu.getItems().addAll(startClock, tickClock, frequenciesMenu);
		
		menuBar.getMenus().addAll(fileMenu, circuitsMenu, clockMenu);
		
		VBox vBox = new VBox(buttonTabPane, propertiesTable);
		VBox.setVgrow(buttonTabPane, Priority.ALWAYS);
		VBox.setVgrow(propertiesTable, Priority.ALWAYS);
		
		HBox hBox = new HBox(vBox, canvasTabPane);
		HBox.setHgrow(canvasTabPane, Priority.ALWAYS);
		VBox.setVgrow(hBox, Priority.ALWAYS);
		
		Scene scene = new Scene(new VBox(menuBar, hBox));
		
		stage.setScene(scene);
		stage.setTitle("Circuit Simulator");
		stage.sizeToScene();
		stage.show();
		stage.centerOnScreen();
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
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
	
	public void mouseEntered(MouseEvent e) {
		getCurrentCircuit().mouseEntered(e);
	}
	
	public void mouseExited(MouseEvent e) {
		getCurrentCircuit().mouseExited(e);
	}
}
