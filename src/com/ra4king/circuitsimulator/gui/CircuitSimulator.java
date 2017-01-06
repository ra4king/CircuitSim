package com.ra4king.circuitsimulator.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentLauncherInfo;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.gui.Properties.PropertyCircuitValidator;
import com.ra4king.circuitsimulator.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsimulator.gui.file.FileFormat;
import com.ra4king.circuitsimulator.gui.file.FileFormat.CircuitInfo;
import com.ra4king.circuitsimulator.gui.file.FileFormat.ComponentInfo;
import com.ra4king.circuitsimulator.gui.file.FileFormat.WireInfo;
import com.ra4king.circuitsimulator.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.components.Subcircuit;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	
	private Stage stage;
	
	private Simulator simulator;
	
	private ComponentManager componentManager;
	
	private TabPane buttonTabPane;
	private ToggleGroup buttonsToggleGroup;
	
	private ComboBox<Integer> bitSizeSelect, secondaryOptionSelect;
	private GridPane propertiesTable;
	
	private Tab circuitButtonsTab;
	private TabPane canvasTabPane;
	private Map<String, Pair<ComponentLauncherInfo, CircuitManager>> circuitManagers;
	
	private ComponentLauncherInfo selectedComponent;
	
	private File saveFile;
	private boolean hasUnsavedChanges;
	
	private int currentClockHz = 1;
	
	@Override
	public void init() {
		simulator = new Simulator();
		circuitManagers = new LinkedHashMap<>();
		Clock.addChangeListener(
				value -> Platform.runLater(() -> {
					CircuitManager manager = getCurrentCircuit();
					if(manager != null) {
						try {
							manager.getCircuitBoard().runSim();
							manager.repaint();
						} catch(Exception exc) {
						}
					}
				}));
	}
	
	private CircuitManager getCurrentCircuit() {
		Tab tab = canvasTabPane.getSelectionModel().getSelectedItem();
		return tab == null ? null : circuitManagers.get(tab.getText()).second;
	}
	
	public CircuitManager getCircuitManager(String name) {
		return circuitManagers.containsKey(name) ? circuitManagers.get(name).second : null;
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
							         updateProperties(newProperties);
						         }
					         });
					value = valueList;
				} else if(property.validator instanceof PropertyCircuitValidator) {
					return;
				} else {
					TextField valueField = new TextField(property.value);
					valueField.setOnAction(event -> {
						String newValue = valueField.getText();
						if(!newValue.equals(property.value)) {
							Properties newProperties = new Properties(properties);
							newProperties.setValue(property, newValue);
							updateProperties(newProperties);
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
	
	private Properties getDefaultProperties() {
		Properties properties = new Properties();
		properties.setValue(Properties.BITSIZE, String.valueOf(bitSizeSelect.getSelectionModel().getSelectedItem()));
		properties.setValue(Properties.NUM_INPUTS,
		                    String.valueOf(secondaryOptionSelect.getSelectionModel().getSelectedItem()));
		return properties;
	}
	
	public void clearSelection() {
		if(buttonsToggleGroup.getSelectedToggle() != null) {
			buttonsToggleGroup.getSelectedToggle().setSelected(false);
		}
		
		modifiedSelection(null, null);
	}
	
	private void updateProperties(Properties properties) {
		modifiedSelection(null, properties);
	}
	
	private void modifiedSelection(ComponentLauncherInfo component) {
		selectedComponent = component;
		if(component != null) {
			Properties properties = getDefaultProperties().union(component.properties);
			modifiedSelection(component.creator, properties);
		} else {
			modifiedSelection(null, null);
		}
	}
	
	private void modifiedSelection(ComponentCreator<?> creator, Properties properties) {
		CircuitManager current = getCurrentCircuit();
		if(current != null) {
			setProperties(current.modifiedSelection(creator, properties));
			current.repaint();
		}
	}
	
	private ImageView setupImageView(Image image) {
		ImageView imageView = new ImageView(image);
		imageView.setFitHeight(40);
		imageView.setFitHeight(30);
		imageView.setSmooth(true);
		return imageView;
	}
	
	private ToggleButton setupButton(ToggleGroup group, ComponentLauncherInfo componentInfo) {
		ToggleButton button = new ToggleButton(componentInfo.name.second, setupImageView(componentInfo.image));
		group.getToggles().add(button);
		button.setMinHeight(30);
		button.setMaxWidth(Double.MAX_VALUE);
		button.setOnAction(e -> {
			if(button.isSelected()) {
				modifiedSelection(componentInfo);
			} else {
				modifiedSelection(null);
			}
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
		
		circuitManagers.forEach((name, circuitPair) -> {
			GridPane buttons = (GridPane)circuitButtonsTab.getContent();
			
			ToggleButton toggleButton = setupButton(buttonsToggleGroup, circuitPair.first);
			buttons.addRow(buttons.getChildren().size(), toggleButton);
		});
	}

//	private boolean hasUnsavedChanges() {
//		for(Pair<ComponentLauncherInfo, CircuitManager> circuitPair : circuitManagers.values()) {
//			if(!circuitPair.second.getCircuitBoard().getComponents().isEmpty()
//					   || !circuitPair.second.getCircuitBoard().getLinks().isEmpty()) {
//				return true;
//			}
//		}
//		
//		return circuitManagers.size() > 1;
//	}
	
	private void updateTitle() {
		String name = "";
		if(saveFile != null) {
			name = " - " + saveFile.getName();
		}
		if(hasUnsavedChanges) {
			name += " *";
		}
		stage.setTitle("Circuit simulator" + name);
	}
	
	private ComponentCreator<?> getSubcircuitPeerCreator(String name) {
		return (props, x, y) -> {
			props.ensureProperty(new Property(SubcircuitPeer.SUBCIRCUIT, new PropertyCircuitValidator(this), name));
			return ComponentManager.forClass(SubcircuitPeer.class).createComponent(props, x, y);
		};
	}
	
	private ComponentLauncherInfo createCircuitLauncherInfo(String name) {
		return new ComponentLauncherInfo(SubcircuitPeer.class,
		                                 new Pair<>("Circuits", name),
		                                 null,
		                                 new Properties(),
		                                 getSubcircuitPeerCreator(name));
	}
	
	private void renameCircuit(String oldName, String newName) {
		Pair<ComponentLauncherInfo, CircuitManager> removed = circuitManagers.remove(oldName);
		circuitManagers.put(newName, new Pair<>(createCircuitLauncherInfo(newName), removed.second));
		refreshCircuitsTab();
		
		circuitManagers.values().forEach(componentPair -> {
			for(ComponentPeer<?> componentPeer : componentPair.second.getCircuitBoard().getComponents()) {
				if(componentPeer.getClass() == SubcircuitPeer.class &&
						   ((Subcircuit)componentPeer.getComponent()).getSubcircuit() == removed.second.getCircuit()) {
					componentPeer.getProperties().setValue(SubcircuitPeer.SUBCIRCUIT, newName);
				}
			}
		});
	}
	
	private void circuitModified(Circuit circuit) {
		circuitModified(circuit, false);
	}
	
	private void circuitModified(Circuit circuit, boolean removed) {
		hasUnsavedChanges = true;
		updateTitle();
		
		circuitManagers.values().forEach(componentPair -> {
			for(ComponentPeer<?> componentPeer :
					new HashSet<>(componentPair.second.getCircuitBoard().getComponents())) {
				if(componentPeer.getClass() == SubcircuitPeer.class
						   && ((Subcircuit)componentPeer.getComponent()).getSubcircuit() == circuit) {
					try {
						componentPair.second.getCircuitBoard().removeElements(Collections.singleton(componentPeer));
					} catch(Exception exc) {
						exc.printStackTrace();
					}
					if(!removed) {
						try {
							componentPair.second.getCircuitBoard().addComponent(
									new SubcircuitPeer(componentPeer.getProperties(),
									                   componentPeer.getX(),
									                   componentPeer.getY()));
						} catch(Exception exc) {
							exc.printStackTrace();
						}
					}
				}
			}
		});
	}
	
	private CircuitManager createCircuit(String name) {
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
		circuitManager.getCircuit().addListener(this::circuitModified);
		
		String originalName;
		for(int count = 0; getCircuitManager(name) != null; count++) {
			originalName = name;
			if(count > 0) {
				originalName += count;
			}
			name = originalName;
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
						renameCircuit(canvasTab.getText(), lastTyped);
						canvasTab.setText(value.get());
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
				Pair<ComponentLauncherInfo, CircuitManager> removed = circuitManagers.remove(canvasTab.getText());
				circuitModified(removed.second.getCircuit(), true);
				
				if(canvasTabPane.getTabs().size() == 1) {
					createCircuit("New circuit").repaint();
				}
				
				refreshCircuitsTab();
			}
		});
		
		circuitManagers.put(canvasTab.getText(), new Pair<>(createCircuitLauncherInfo(name), circuitManager));
		canvasTabPane.getTabs().addAll(canvasTab);
		
		refreshCircuitsTab();
		
		return circuitManager;
	}
	
	@Override
	public void start(Stage stage) {
		this.stage = stage;
		
		componentManager = new ComponentManager();
		
		bitSizeSelect = new ComboBox<>();
		for(int i = 1; i <= 32; i++) {
			bitSizeSelect.getItems().add(i);
		}
		bitSizeSelect.setMaxWidth(Double.MAX_VALUE);
		bitSizeSelect.setValue(1);
		bitSizeSelect.getSelectionModel()
		             .selectedItemProperty()
		             .addListener((observable, oldValue, newValue) -> modifiedSelection(selectedComponent));
		
		secondaryOptionSelect = new ComboBox<>();
		for(int i = 2; i <= 32; i++) {
			secondaryOptionSelect.getItems().add(i);
		}
		secondaryOptionSelect.setMaxWidth(Double.MAX_VALUE);
		secondaryOptionSelect.setValue(2);
		secondaryOptionSelect.getSelectionModel()
		                     .selectedItemProperty()
		                     .addListener((observable, oldValue, newValue) -> modifiedSelection(selectedComponent));
		
		buttonTabPane = new TabPane();
		buttonTabPane.setMinWidth(100);
		
		propertiesTable = new GridPane();
		propertiesTable.setMaxWidth(Double.MAX_VALUE);
		
		canvasTabPane = new TabPane();
		canvasTabPane.setPrefWidth(800);
		canvasTabPane.setPrefHeight(600);
		canvasTabPane.setMaxWidth(Double.MAX_VALUE);
		canvasTabPane.setMaxHeight(Double.MAX_VALUE);
		canvasTabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		canvasTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			CircuitManager oldManager = oldValue == null || !circuitManagers.containsKey(oldValue.getText())
			                            ? null : circuitManagers.get(oldValue.getText()).second;
			CircuitManager newManager = newValue == null || !circuitManagers.containsKey(newValue.getText())
			                            ? null : circuitManagers.get(newValue.getText()).second;
			if(oldManager != null && newManager != null) {
				newManager.setLastMousePosition(oldManager.getLastMousePosition());
			}
			
			modifiedSelection(selectedComponent);
		});
		
		buttonsToggleGroup = new ToggleGroup();
		Map<String, Tab> buttonTabs = new HashMap<>();
		
		componentManager.forEach(componentInfo -> {
			Tab tab;
			if(buttonTabs.containsKey(componentInfo.name.first)) {
				tab = buttonTabs.get(componentInfo.name.first);
			} else {
				tab = new Tab(componentInfo.name.first);
				tab.setClosable(false);
				tab.setContent(new GridPane());
				buttonTabPane.getTabs().add(tab);
				buttonTabs.put(componentInfo.name.first, tab);
			}
			
			GridPane buttons = (GridPane)tab.getContent();
			
			ToggleButton toggleButton = setupButton(buttonsToggleGroup, componentInfo);
			buttons.addRow(buttons.getChildren().size(), toggleButton);
		});
		
		createCircuit("New circuit");
		
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
					
					clearSelection();
					
					circuitManagers.clear();
					canvasTabPane.getTabs().clear();
					
					for(CircuitInfo circuit : circuits) {
						createCircuit(circuit.name);
					}
					
					for(CircuitInfo circuit : circuits) {
						CircuitManager manager = getCircuitManager(circuit.name);
						
						for(ComponentInfo component : circuit.components) {
							@SuppressWarnings("unchecked")
							Class<? extends ComponentPeer<?>> clazz =
									(Class<? extends ComponentPeer<?>>)Class.forName(component.className);
							
							ComponentCreator<?> creator;
							if(clazz == SubcircuitPeer.class) {
								creator = getSubcircuitPeerCreator(
										component.properties.getValueOrDefault(SubcircuitPeer.SUBCIRCUIT, ""));
							} else {
								creator = ComponentManager.forClass(clazz);
							}
							
							manager.getCircuitBoard().addComponent(
									creator.createComponent(component.properties,
									                        component.x,
									                        component.y));
						}
						
						for(WireInfo wire : circuit.wires) {
							manager.getCircuitBoard().addWire(wire.x, wire.y, wire.length, wire.isHorizontal);
						}
					}
					
					if(circuits.size() == 0) {
						createCircuit("New circuit");
					}
					
					hasUnsavedChanges = false;
					updateTitle();
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
					
					CircuitManager manager = circuitManagers.get(name).second;
					
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
					hasUnsavedChanges = false;
					updateTitle();
//					Alert alert = new Alert(AlertType.INFORMATION);
//					alert.setTitle("Circuits saved");
//					alert.setHeaderText("Circuits saved");
//					alert.setContentText("Circuits have successfully been saved.");
//					alert.show();
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
			
			updateTitle();
		});
		
		fileMenu.getItems().addAll(load, save, saveAs);
		
		Menu circuitsMenu = new Menu("Circuits");
		MenuItem newCircuit = new MenuItem("New circuit");
		newCircuit.setOnAction(event -> createCircuit("New circuit"));
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
		
		VBox leftPanel = new VBox(buttonTabPane, propertiesTable);
		VBox.setVgrow(buttonTabPane, Priority.ALWAYS);
		VBox.setVgrow(propertiesTable, Priority.ALWAYS);
		
		HBox hBox = new HBox(leftPanel, canvasTabPane);
		HBox.setHgrow(canvasTabPane, Priority.ALWAYS);
		VBox.setVgrow(hBox, Priority.ALWAYS);
		
		ToolBar toolBar = new ToolBar();
		ComponentLauncherInfo andGateInfo = componentManager.get(new Pair<>("Gates", "AND"));
		ToggleButton andButton = new ToggleButton("", setupImageView(andGateInfo.image));
		andButton.setToggleGroup(buttonsToggleGroup);
		andButton.setOnAction(event -> modifiedSelection(andGateInfo));
		
		ComponentLauncherInfo orGateInfo = componentManager.get(new Pair<>("Gates", "OR"));
		ToggleButton orButton = new ToggleButton("", setupImageView(orGateInfo.image));
		orButton.setToggleGroup(buttonsToggleGroup);
		orButton.setOnAction(event -> modifiedSelection(orGateInfo));
		
		toolBar.getItems().addAll(andButton, orButton,
		                          new Label("Bit size:"), bitSizeSelect,
		                          new Label("Secondary:"), secondaryOptionSelect);
		
		Scene scene = new Scene(new VBox(menuBar, toolBar, hBox));
		
		stage.setScene(scene);
		stage.setTitle("Circuit Simulator");
		stage.sizeToScene();
		stage.show();
		stage.centerOnScreen();
		
		stage.setOnCloseRequest(event -> {
			if(hasUnsavedChanges) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Unsaved changes");
				alert.setHeaderText("Unsaved changes");
				alert.setContentText("There are unsaved changes, do you want to save them?");
				Optional<ButtonType> result = alert.showAndWait();
				if(result.isPresent() && result.get() == ButtonType.OK) {
					save.fire();
					if(saveFile == null) {
						event.consume();
					}
				}
			}
		});
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
			case ESCAPE:
				clearSelection();
				break;
		}
		
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.keyPressed(e);
		}
	}
	
	public void keyReleased(KeyEvent e) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.keyReleased(e);
		}
	}
	
	public void keyTyped(KeyEvent e) {}
	
	public void mouseClicked(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.mousePressed(e);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.mouseReleased(e);
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.mouseMoved(e);
		}
	}
	
	public void mouseDragged(MouseEvent e) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.mouseDragged(e);
		}
		;
	}
	
	public void mouseEntered(MouseEvent e) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.mouseEntered(e);
		}
	}
	
	public void mouseExited(MouseEvent e) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			manager.mouseExited(e);
		}
	}
}
