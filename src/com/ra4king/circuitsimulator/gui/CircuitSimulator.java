package com.ra4king.circuitsimulator.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentLauncherInfo;
import com.ra4king.circuitsimulator.gui.EditHistory.EditAction;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.gui.Properties.PropertyCircuitValidator;
import com.ra4king.circuitsimulator.gui.file.FileFormat;
import com.ra4king.circuitsimulator.gui.file.FileFormat.CircuitInfo;
import com.ra4king.circuitsimulator.gui.file.FileFormat.ComponentInfo;
import com.ra4king.circuitsimulator.gui.file.FileFormat.WireInfo;
import com.ra4king.circuitsimulator.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.Subcircuit;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Pair;

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
	
	private MenuItem toggleClock;
	
	private ComponentManager componentManager;
	
	private Canvas overlayCanvas;
	
	private TabPane buttonTabPane;
	private ToggleGroup buttonsToggleGroup;
	
	private ComboBox<Integer> bitSizeSelect, secondaryOptionSelect;
	private GridPane propertiesTable;
	private Label componentLabel;
	
	private Tab circuitButtonsTab;
	private TabPane canvasTabPane;
	private Map<String, Pair<ComponentLauncherInfo, CircuitManager>> circuitManagers;
	
	private ComponentLauncherInfo selectedComponent;
	
	private File saveFile, lastSaveFile;
	
	private DataFormat copyDataFormat = new DataFormat("x-circuit-simulator");
	
	private EditHistory editHistory;
	private int savedEditStackSize;
	
	private int currentClockHz = 1;
	
	@Override
	public void init() {
		simulator = new Simulator();
		circuitManagers = new LinkedHashMap<>();
		Clock.addChangeListener(
				value -> Platform.runLater(() -> {
					CircuitManager manager = getCurrentCircuit();
					if(manager != null
							   && manager.mayThrow(() -> manager.getCircuitBoard().runSim())
							   && Clock.isRunning()) {
						toggleClock.fire();
					}
				}));
	}
	
	public EditHistory getEditHistory() {
		return editHistory;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	private CircuitManager getCurrentCircuit() {
		Tab tab = canvasTabPane.getSelectionModel().getSelectedItem();
		
		// sigh yes this sometimes happens
		if(!canvasTabPane.getTabs().contains(tab)) {
			return null;
		}
		
		return tab == null || circuitManagers.get(tab.getText()) == null
		       ? null
		       : circuitManagers.get(tab.getText()).getValue();
	}
	
	public String getCircuitName(CircuitManager manager) {
		for(Entry<String, Pair<ComponentLauncherInfo, CircuitManager>> entry : circuitManagers.entrySet()) {
			if(entry.getValue().getValue() == manager) {
				return entry.getKey();
			}
		}
		
		return null;
	}
	
	public CircuitManager getCircuitManager(String name) {
		return circuitManagers.containsKey(name) ? circuitManagers.get(name).getValue() : null;
	}
	
	public CircuitManager getCircuitManager(Circuit circuit) {
		for(Entry<String, Pair<ComponentLauncherInfo, CircuitManager>> entry : circuitManagers.entrySet()) {
			if(entry.getValue().getValue().getCircuit() == circuit) {
				return entry.getValue().getValue();
			}
		}
		
		return null;
	}
	
	private Tab getTabForCircuit(Circuit circuit) {
		for(Entry<String, Pair<ComponentLauncherInfo, CircuitManager>> entry : circuitManagers.entrySet()) {
			if(entry.getValue().getValue().getCircuit() == circuit) {
				for(Tab tab : canvasTabPane.getTabs()) {
					if(tab.getText().equals(entry.getKey())) {
						return tab;
					}
				}
			}
		}
		
		return null;
	}
	
	public void switchToCircuit(Circuit circuit) {
		Tab tab = getTabForCircuit(circuit);
		if(tab != null) {
			canvasTabPane.getSelectionModel().select(tab);
		}
	}
	
	public void readdCircuit(CircuitManager manager, Tab tab, int index) {
		canvasTabPane.getTabs().add(Math.min(index, canvasTabPane.getTabs().size()), tab);
		circuitManagers.put(tab.getText(), new Pair<>(createCircuitLauncherInfo(tab.getText()), manager));
		
		canvasTabPane.getSelectionModel().select(tab);
		
		refreshCircuitsTab();
	}
	
	public void deleteCircuit(CircuitManager manager, boolean removeTab) {
		Tab tab = getTabForCircuit(manager.getCircuit());
		if(tab == null) {
			throw new IllegalStateException("Tab shouldn't be null.");
		}
		
		int idx = canvasTabPane.getTabs().indexOf(tab);
		if(idx == -1) throw new IllegalStateException("Tab should be in the tab pane.");
		
		if(removeTab) {
			canvasTabPane.getTabs().remove(tab);
		}
		
		editHistory.beginGroup();
		
		Pair<ComponentLauncherInfo, CircuitManager> removed = circuitManagers.remove(tab.getText());
		circuitModified(removed.getValue().getCircuit(), null, false);
		
		editHistory.addAction(EditAction.DELETE_CIRCUIT, manager, tab, idx);
		
		if(!removeTab && canvasTabPane.getTabs().size() == 1) {
			createCircuit("New circuit");
			canvasTabPane.getSelectionModel().select(0);
		} else {
			canvasTabPane.getSelectionModel().select(Math.max(0, idx - 1));
		}
		
		editHistory.endGroup();
		
		refreshCircuitsTab();
	}
	
	public void clearProperties() {
		setProperties("", null);
	}
	
	public void setProperties(ComponentPeer<?> componentPeer) {
		ComponentLauncherInfo info = componentManager.get(componentPeer.getClass());
		String name;
		if(info == null) {
			if(componentPeer.getClass() != SubcircuitPeer.class) {
				throw new IllegalStateException("How does this happen?");
			}
			
			name = componentPeer.getProperties().getProperty(SubcircuitPeer.SUBCIRCUIT).getStringValue();
		} else {
			name = info.name.getValue();
		}
		setProperties(name, componentPeer.getProperties());
	}
	
	public void setProperties(String componentName, Properties properties) {
		propertiesTable.getChildren().clear();
		
		if(properties != null) {
			componentLabel.setText(componentName);
			
			properties.forEach(new Consumer<Property<?>>() {
				@Override
				public void accept(Property<?> property) {
					acceptProperty(property);
				}
				
				// This is an interesting trick to force that all usage of "property" work on the same type.
				private <T> void acceptProperty(Property<T> property) {
					int size = propertiesTable.getChildren().size();
					
					Label name = new Label(property.name);
					GridPane.setHgrow(name, Priority.ALWAYS);
					name.setMaxWidth(Double.MAX_VALUE);
					name.setMinHeight(30);
					name.setBackground(
							new Background(new BackgroundFill((size / 2) % 2 == 0 ? Color.LIGHTGRAY
							                                                      : Color.WHITE, null, null)));
					
					Node node = property.validator.createGui(stage, property.value, newValue -> {
						Properties newProperties = new Properties(properties);
						newProperties.setValue(property, newValue);
						updateProperties(newProperties);
					});
					
					if(node != null) {
						Pane valuePane = new Pane(node);
						valuePane.setBackground(
								new Background(new BackgroundFill((size / 2) % 2 == 0 ? Color.LIGHTGRAY
								                                                      : Color.WHITE, null, null)));
						
						GridPane.setHgrow(valuePane, Priority.ALWAYS);
						propertiesTable.addRow(size, name, valuePane);
					}
				}
			});
		} else {
			componentLabel.setText("");
		}
	}
	
	private Properties getDefaultProperties() {
		Properties properties = new Properties();
		properties.setValue(Properties.BITSIZE, bitSizeSelect.getSelectionModel().getSelectedItem());
		properties.setValue(Properties.NUM_INPUTS, secondaryOptionSelect.getSelectionModel().getSelectedItem());
		return properties;
	}
	
	public void clearSelection() {
		if(buttonsToggleGroup.getSelectedToggle() != null) {
			buttonsToggleGroup.getSelectedToggle().setSelected(false);
		}
		
		modifiedSelection(null);
	}
	
	private void updateProperties(Properties properties) {
		if(selectedComponent == null) {
			modifiedSelection(null, properties);
		} else {
			properties = getDefaultProperties().union(selectedComponent.properties).union(properties);
			modifiedSelection(selectedComponent.creator, properties);
		}
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
			current.modifiedSelection(creator, properties);
		}
	}
	
	private ImageView setupImageView(Image image) {
		ImageView imageView = new ImageView(image);
		imageView.setSmooth(true);
		return imageView;
	}
	
	private ToggleButton setupButton(ToggleGroup group, ComponentLauncherInfo componentInfo) {
		ToggleButton button = new ToggleButton(componentInfo.name.getValue(), setupImageView(componentInfo.image));
		button.setAlignment(Pos.CENTER_LEFT);
		button.setToggleGroup(group);
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
		ScrollPane pane = new ScrollPane(new GridPane());
		pane.setFitToWidth(true);
		
		if(circuitButtonsTab == null) {
			circuitButtonsTab = new Tab("Circuits");
			circuitButtonsTab.setClosable(false);
			circuitButtonsTab.setContent(pane);
			buttonTabPane.getTabs().add(circuitButtonsTab);
		} else {
			circuitButtonsTab.setContent(pane);
		}
		
		circuitManagers.keySet().stream().sorted().forEach((name) -> {
			Pair<ComponentLauncherInfo, CircuitManager> circuitPair = circuitManagers.get(name);
			
			GridPane buttons = (GridPane)pane.getContent();
			ToggleButton toggleButton = setupButton(buttonsToggleGroup, circuitPair.getKey());
			buttons.addRow(buttons.getChildren().size(), toggleButton);
		});
	}
	
	private void updateTitle() {
		String name = "";
		if(saveFile != null) {
			name = " - " + saveFile.getName();
		}
		if(editHistory.editStackSize() != savedEditStackSize) {
			name += " *";
		}
		stage.setTitle("Circuit simulator" + name);
	}
	
	private ComponentCreator<?> getSubcircuitPeerCreator(String name) {
		return (props, x, y) -> {
			Properties properties = new Properties(props);
			properties.parseAndSetValue(SubcircuitPeer.SUBCIRCUIT, new PropertyCircuitValidator(this), name);
			return ComponentManager.forClass(SubcircuitPeer.class).createComponent(properties, x, y);
		};
	}
	
	private ComponentLauncherInfo createCircuitLauncherInfo(String name) {
		return new ComponentLauncherInfo(SubcircuitPeer.class,
		                                 new Pair<>("Circuits", name),
		                                 null,
		                                 new Properties(),
		                                 getSubcircuitPeerCreator(name));
	}
	
	public void renameCircuit(Tab tab, String newName) {
		String oldName = tab.getText();
		
		Pair<ComponentLauncherInfo, CircuitManager> removed = circuitManagers.get(oldName);
		Pair<ComponentLauncherInfo, CircuitManager> newPair =
				new Pair<>(createCircuitLauncherInfo(newName), removed.getValue());
		// use stream operators to replace mapping at the same index
		circuitManagers =
				circuitManagers.keySet().stream()
				               .collect(Collectors.toMap(s -> s.equals(oldName) ? newName : s,
				                                         s -> s.equals(oldName) ? newPair
				                                                                : circuitManagers.get(s),
				                                         (a, b) -> {
					                                         throw new IllegalStateException("Name already exists");
				                                         },
				                                         LinkedHashMap::new));
		refreshCircuitsTab();
		
		circuitManagers.values().forEach(componentPair -> {
			for(ComponentPeer<?> componentPeer : componentPair.getValue().getCircuitBoard().getComponents()) {
				if(componentPeer.getClass() == SubcircuitPeer.class
						   && ((Subcircuit)componentPeer.getComponent()).getSubcircuit()
								      == removed.getValue().getCircuit()) {
					componentPeer.getProperties().parseAndSetValue(SubcircuitPeer.SUBCIRCUIT, newName);
				}
			}
		});
		
		tab.setText(newName);
		
		editHistory.addAction(EditAction.RENAME_CIRCUIT, null, this, tab, oldName, newName);
	}
	
	private void updateCanvasSize(CircuitManager circuitManager) {
		OptionalInt maxX = Stream.concat(circuitManager.getCircuitBoard().getComponents().stream(),
		                                 circuitManager.getCircuitBoard().getLinks().stream().flatMap(
				                                 links -> links.getWires().stream()))
		                         .mapToInt(componentPeer -> componentPeer.getX() + componentPeer.getWidth())
		                         .max();
		
		int maxWidth = ((maxX.isPresent() ? maxX.getAsInt() : 0) + 5) * GuiUtils.BLOCK_SIZE;
		circuitManager.getCanvas().setWidth(
				maxWidth < circuitManager.getCanvasScrollPane().getWidth() ? circuitManager.getCanvasScrollPane()
				                                                                           .getWidth() : maxWidth);
		
		OptionalInt maxY = Stream.concat(circuitManager.getCircuitBoard().getComponents().stream(),
		                                 circuitManager.getCircuitBoard().getLinks().stream().flatMap(
				                                 links -> links.getWires().stream()))
		                         .mapToInt(componentPeer -> componentPeer.getY() + componentPeer.getHeight())
		                         .max();
		
		int maxHeight = ((maxY.isPresent() ? maxY.getAsInt() : 0) + 5) * GuiUtils.BLOCK_SIZE;
		circuitManager.getCanvas().setHeight(
				maxHeight < circuitManager.getCanvasScrollPane().getHeight() ? circuitManager.getCanvasScrollPane()
				                                                                             .getHeight() : maxHeight);
	}
	
	private void circuitModified(Circuit circuit, Component component, boolean added) {
		if(component != null && !(component instanceof Pin)) {
			return;
		}
		
		circuitManagers.values().forEach(componentPair -> {
			for(ComponentPeer<?> componentPeer :
					new HashSet<>(componentPair.getValue().getCircuitBoard().getComponents())) {
				if(componentPeer.getClass() == SubcircuitPeer.class) {
					SubcircuitPeer peer = (SubcircuitPeer)componentPeer;
					if(peer.getComponent().getSubcircuit() == circuit) {
						CircuitNode node =
								getSubcircuitStates(peer.getComponent(),
								                    componentPair.getValue().getCircuitBoard().getCurrentState());
						
						componentPair.getValue().getSelectedElements().remove(peer);
						
						if(component == null) {
							componentPair.getValue().mayThrow(
									() -> componentPair.getValue()
									                   .getCircuitBoard()
									                   .removeElements(Collections.singleton(peer)));
							
							resetSubcircuitStates(node);
						} else {
							SubcircuitPeer newSubcircuit =
									new SubcircuitPeer(componentPeer.getProperties(),
									                   componentPeer.getX(),
									                   componentPeer.getY());
							
							editHistory.disable();
							componentPair.getValue().mayThrow(
									() -> componentPair.getValue()
									                   .getCircuitBoard()
									                   .updateComponent(peer, newSubcircuit));
							editHistory.enable();
							
							node.subcircuit = newSubcircuit.getComponent();
							updateSubcircuitStates(node, componentPair.getValue()
							                                          .getCircuitBoard()
							                                          .getCurrentState());
						}
					}
				}
			}
		});
	}
	
	private class CircuitNode {
		private Subcircuit subcircuit;
		private CircuitState subcircuitState;
		private List<CircuitNode> children;
		
		CircuitNode(Subcircuit subcircuit, CircuitState subcircuitState) {
			this.subcircuit = subcircuit;
			this.subcircuitState = subcircuitState;
			children = new ArrayList<>();
		}
	}
	
	private CircuitNode getSubcircuitStates(Subcircuit subcircuit, CircuitState parentState) {
		CircuitState subcircuitState = subcircuit.getSubcircuitState(parentState);
		
		CircuitNode circuitNode = new CircuitNode(subcircuit, subcircuitState);
		
		for(Component component : subcircuit.getSubcircuit().getComponents()) {
			if(component instanceof Subcircuit) {
				circuitNode.children.add(getSubcircuitStates((Subcircuit)component, subcircuitState));
			}
		}
		
		return circuitNode;
	}
	
	private void updateSubcircuitStates(CircuitNode node, CircuitState parentState) {
		CircuitManager manager = getCircuitManager(node.subcircuit.getSubcircuit());
		CircuitState subState = node.subcircuit.getSubcircuitState(parentState);
		if(manager != null && manager.getCircuitBoard().getCurrentState() == node.subcircuitState) {
			manager.getCircuitBoard().setCurrentState(subState);
		}
		
		for(CircuitNode child : node.children) {
			updateSubcircuitStates(child, subState);
		}
	}
	
	private void resetSubcircuitStates(CircuitNode node) {
		CircuitManager manager = getCircuitManager(node.subcircuit.getSubcircuit());
		if(manager != null && manager.getCircuitBoard().getCurrentState() == node.subcircuitState) {
			manager.getCircuitBoard().setCurrentState(manager.getCircuit().getTopLevelState());
		}
		
		for(CircuitNode child : node.children) {
			resetSubcircuitStates(child);
		}
	}
	
	private void checkUnsavedChanges(Event event) {
		if(editHistory.editStackSize() != savedEditStackSize) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Unsaved changes");
			alert.setHeaderText("Unsaved changes");
			alert.setContentText("There are unsaved changes, do you want to save them?");
			
			ButtonType discard = new ButtonType("Discard", ButtonData.NO);
			alert.getButtonTypes().add(discard);
			
			Optional<ButtonType> result = alert.showAndWait();
			if(result.isPresent()) {
				if(result.get() == ButtonType.OK) {
					saveCircuits();
					if(saveFile == null && event != null) {
						event.consume();
					}
				} else if(result.get() == ButtonType.CANCEL && event != null) {
					event.consume();
				}
			}
		}
	}
	
	private void loadCircuits() {
		checkUnsavedChanges(null);
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose sim file");
		fileChooser.setInitialDirectory(lastSaveFile == null ? new File(System.getProperty("user.dir"))
		                                                     : lastSaveFile.getParentFile());
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Circuit Sim file", "*.sim"));
		File selectedFile = fileChooser.showOpenDialog(stage);
		if(selectedFile != null) {
			lastSaveFile = saveFile = selectedFile;
			
			try {
				long now = System.nanoTime();
				List<CircuitInfo> circuits = FileFormat.load(selectedFile);
				
				System.out.printf("Loaded file in %.3f ms\n", (System.nanoTime() - now) / 1e6);
				
				clearSelection();
				if(Clock.isRunning()) {
					toggleClock.fire();
				}
				circuitManagers.forEach((name, pair) -> pair.getValue().destroy());
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
						
						manager.mayThrow(
								() -> manager.getCircuitBoard().addComponent(
										creator.createComponent(component.properties, component.x, component.y)));
					}
					
					for(WireInfo wire : circuit.wires) {
						manager.mayThrow(
								() -> manager.getCircuitBoard()
								             .addWire(wire.x, wire.y, wire.length, wire.isHorizontal));
					}
				}
				
				if(circuits.size() == 0) {
					createCircuit("New circuit");
				}
			} catch(Exception exc) {
				circuitManagers.forEach((name, pair) -> pair.getValue().destroy());
				circuitManagers.clear();
				canvasTabPane.getTabs().clear();
				
				exc.printStackTrace();
				
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error loading circuits");
				alert.setHeaderText("Error loading circuits");
				alert.setContentText("Error when loading circuits file: " + exc.getMessage()
						                     + "\nPlease send the stack trace to a developer.");
				alert.showAndWait();
			} finally {
				editHistory.clear();
				savedEditStackSize = 0;
				updateTitle();
			}
		}
	}
	
	private void saveCircuits() {
		if(saveFile == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose sim file");
			fileChooser.setInitialDirectory(lastSaveFile == null ? new File(System.getProperty("user.dir"))
			                                                     : lastSaveFile.getParentFile());
			fileChooser.setInitialFileName("My circuit.sim");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Circuit Sim file", "*.sim"));
			saveFile = fileChooser.showSaveDialog(stage);
		}
		
		if(saveFile != null) {
			lastSaveFile = saveFile;
			
			List<CircuitInfo> circuits = new ArrayList<>();
			
			canvasTabPane.getTabs().forEach(tab -> {
				String name = tab.getText();
				
				CircuitManager manager = circuitManagers.get(name).getValue();
				
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
				savedEditStackSize = editHistory.editStackSize();
				updateTitle();
			} catch(Exception exc) {
				exc.printStackTrace();
				
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error saving circuit");
				alert.setHeaderText("Error saving circuit.");
				alert.setContentText("Error when saving the circuits: " + exc.getMessage());
				alert.showAndWait();
			}
		}
	}
	
	private CircuitManager createCircuit(String name) {
		Canvas canvas = new Canvas(800, 600);
		canvas.addEventHandler(MouseEvent.ANY, event -> canvas.requestFocus());
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
		
		canvas.requestFocus();
		
		ScrollPane canvasScrollPane = new ScrollPane(canvas);
		
		CircuitManager circuitManager = new CircuitManager(this, canvasScrollPane, simulator);
		circuitManager.getCircuit().addListener(this::circuitModified);
		
		canvasScrollPane.widthProperty().addListener(
				(observable, oldValue, newValue) -> this.updateCanvasSize(circuitManager));
		canvasScrollPane.heightProperty().addListener(
				(observable, oldValue, newValue) -> this.updateCanvasSize(circuitManager));
		
		String originalName = name;
		for(int count = 0; getCircuitManager(originalName) != null; count++) {
			originalName = name;
			if(count > 0) {
				originalName += count;
			}
		}
		
		name = originalName;
		
		Tab canvasTab = new Tab(name, canvasScrollPane);
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
						renameCircuit(canvasTab, lastTyped);
					}
					break;
				} catch(Exception exc) {
					exc.printStackTrace();
					
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Duplicate name");
					alert.setHeaderText("Duplicate name");
					alert.setContentText("Name already exists, please choose a new name.");
					alert.showAndWait();
				}
			}
		});
		MenuItem viewTopLevelState = new MenuItem("View top-level state");
		viewTopLevelState.setOnAction(event ->
				                              circuitManager.getCircuitBoard().setCurrentState(
						                              circuitManager.getCircuit().getTopLevelState()));
		canvasTab.setContextMenu(new ContextMenu(rename, viewTopLevelState));
		canvasTab.setOnCloseRequest(event -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Delete this circuit?");
			alert.setHeaderText("Delete this circuit?");
			alert.setContentText("Are you sure you want to delete this circuit?");
			
			Optional<ButtonType> result = alert.showAndWait();
			if(!result.isPresent() || result.get() != ButtonType.OK) {
				event.consume();
			} else {
				deleteCircuit(circuitManager, false);
			}
		});
		
		circuitManagers.put(canvasTab.getText(), new Pair<>(createCircuitLauncherInfo(name), circuitManager));
		canvasTabPane.getTabs().add(canvasTab);
		
		refreshCircuitsTab();
		
		editHistory.addAction(EditAction.CREATE_CIRCUIT, circuitManager, canvasTab, canvasTabPane.getTabs().size() -
				                                                                            1);
		
		return circuitManager;
	}
	
	@Override
	public void start(Stage stage) {
		this.stage = stage;
		
		editHistory = new EditHistory();
		editHistory.addListener((action, manager, params) -> {
			updateTitle();
			circuitManagers.values().stream().map(Pair::getValue).forEach(this::updateCanvasSize);
		});
		
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
		buttonTabPane.setSide(Side.TOP);
		
		propertiesTable = new GridPane();
		
		componentLabel = new Label();
		
		canvasTabPane = new TabPane();
		canvasTabPane.setPrefWidth(800);
		canvasTabPane.setPrefHeight(600);
		canvasTabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		canvasTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			CircuitManager oldManager = oldValue == null || !circuitManagers.containsKey(oldValue.getText())
			                            ? null : circuitManagers.get(oldValue.getText()).getValue();
			CircuitManager newManager = newValue == null || !circuitManagers.containsKey(newValue.getText())
			                            ? null : circuitManagers.get(newValue.getText()).getValue();
			if(oldManager != null && newManager != null) {
				newManager.setLastMousePosition(oldManager.getLastMousePosition());
				modifiedSelection(selectedComponent);
			}
		});
		
		buttonsToggleGroup = new ToggleGroup();
		Map<String, Tab> buttonTabs = new HashMap<>();
		
		componentManager.forEach(componentInfo -> {
			Tab tab;
			if(buttonTabs.containsKey(componentInfo.name.getKey())) {
				tab = buttonTabs.get(componentInfo.name.getKey());
			} else {
				tab = new Tab(componentInfo.name.getKey());
				tab.setClosable(false);
				
				ScrollPane pane = new ScrollPane(new GridPane());
				pane.setFitToWidth(true);
				
				tab.setContent(pane);
				buttonTabPane.getTabs().add(tab);
				buttonTabs.put(componentInfo.name.getKey(), tab);
			}
			
			GridPane buttons = (GridPane)((ScrollPane)tab.getContent()).getContent();
			
			ToggleButton toggleButton = setupButton(buttonsToggleGroup, componentInfo);
			buttons.addRow(buttons.getChildren().size(), toggleButton);
		});
		
		editHistory.disable();
		createCircuit("New circuit");
		editHistory.enable();
		
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("File");
		MenuItem load = new MenuItem("Load");
		load.setAccelerator(new KeyCharacterCombination("O", KeyCombination.CONTROL_DOWN));
		load.setOnAction(event -> loadCircuits());
		
		MenuItem save = new MenuItem("Save");
		save.setAccelerator(new KeyCharacterCombination("S", KeyCombination.CONTROL_DOWN));
		save.setOnAction(event -> saveCircuits());
		
		MenuItem saveAs = new MenuItem("Save as");
		saveAs.setAccelerator(
				new KeyCharacterCombination("S", KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		saveAs.setOnAction(event -> {
			lastSaveFile = saveFile;
			
			saveFile = null;
			saveCircuits();
			
			if(saveFile == null) {
				saveFile = lastSaveFile;
			}
			
			updateTitle();
		});
		
		fileMenu.getItems().addAll(load, save, saveAs);
		
		Menu editMenu = new Menu("Edit");
		MenuItem undo = new MenuItem("Undo");
		undo.setAccelerator(new KeyCharacterCombination("Z", KeyCombination.CONTROL_DOWN));
		undo.setOnAction(event -> {
			CircuitManager manager = editHistory.undo();
			if(manager != null) {
				manager.getSelectedElements().clear();
				switchToCircuit(manager.getCircuit());
			}
		});
		
		MenuItem redo = new MenuItem("Redo");
		redo.setAccelerator(new KeyCharacterCombination("Y", KeyCombination.CONTROL_DOWN));
		redo.setOnAction(event -> {
			CircuitManager manager = editHistory.redo();
			if(manager != null) {
				manager.getSelectedElements().clear();
				switchToCircuit(manager.getCircuit());
			}
		});
		
		MenuItem copy = new MenuItem("Copy");
		copy.setAccelerator(new KeyCharacterCombination("C", KeyCombination.CONTROL_DOWN));
		copy.setOnAction(event -> {
			CircuitManager manager = getCurrentCircuit();
			if(manager != null) {
				Set<GuiElement> selectedElements = manager.getSelectedElements();
				
				Set<ComponentInfo> components =
						selectedElements
								.stream()
								.filter(element -> element instanceof ComponentPeer<?>)
								.map(element -> (ComponentPeer<?>)element)
								.map(component ->
										     new ComponentInfo(component.getClass().getName(),
										                       component.getX(), component.getY(),
										                       component.getProperties())).collect(
								Collectors.toSet());
				
				Set<WireInfo> wires = selectedElements
						                      .stream()
						                      .filter(element -> element instanceof Wire)
						                      .map(element -> (Wire)element)
						                      .map(wire -> new WireInfo(wire.getX(), wire.getY(),
						                                                wire.getLength(), wire.isHorizontal()))
						                      .collect(Collectors.toSet());
				
				try {
					String data = FileFormat.stringify(
							Collections.singletonList(new CircuitInfo("Copy", components, wires)));
					
					Clipboard clipboard = Clipboard.getSystemClipboard();
					ClipboardContent content = new ClipboardContent();
					content.put(copyDataFormat, data);
					clipboard.setContent(content);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		
		MenuItem cut = new MenuItem("Cut");
		cut.setAccelerator(new KeyCharacterCombination("X", KeyCombination.CONTROL_DOWN));
		cut.setOnAction(event -> {
			CircuitManager manager = getCurrentCircuit();
			if(manager != null) {
				copy.fire();
				
				manager.mayThrow(() -> manager.getCircuitBoard().finalizeMove());
				
				Set<GuiElement> selectedElements = manager.getSelectedElements();
				manager.mayThrow(() -> manager.getCircuitBoard().removeElements(selectedElements));
				
				clearSelection();
			}
		});
		
		MenuItem paste = new MenuItem("Paste");
		paste.setAccelerator(new KeyCharacterCombination("V", KeyCombination.CONTROL_DOWN));
		paste.setOnAction(event -> {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			String data = (String)clipboard.getContent(copyDataFormat);
			
			if(data != null) {
				try {
					editHistory.beginGroup();
					
					List<CircuitInfo> parsed = FileFormat.parse(data);
					
					CircuitManager manager = getCurrentCircuit();
					if(manager != null) {
						outer:
						for(int i = 3; ; i += 3) {
							Set<GuiElement> elementsCreated = new HashSet<>();
							
							for(CircuitInfo circuit : parsed) {
								for(ComponentInfo component : circuit.components) {
									try {
										@SuppressWarnings("unchecked")
										Class<? extends ComponentPeer<?>> clazz =
												(Class<? extends ComponentPeer<?>>)Class.forName(component.className);
										
										ComponentCreator<?> creator;
										if(clazz == SubcircuitPeer.class) {
											creator = getSubcircuitPeerCreator(
													component.properties.getValueOrDefault(SubcircuitPeer.SUBCIRCUIT,
													                                       ""));
										} else {
											creator = ComponentManager.forClass(clazz);
										}
										
										ComponentPeer<?> created = creator.createComponent(component.properties,
										                                                   component.x + i,
										                                                   component.y + i);
										
										if(!manager.getCircuitBoard().isValidLocation(created)) {
											elementsCreated.clear();
											continue outer;
										}
										
										elementsCreated.add(created);
									} catch(Exception exc) {
										exc.printStackTrace();
									}
								}
							}
							
							elementsCreated.forEach(
									element -> manager.getCircuit().addComponent(
											((ComponentPeer<?>)element).getComponent()));
							
							for(CircuitInfo circuit : parsed) {
								for(WireInfo wire : circuit.wires) {
									elementsCreated.add(
											new Wire(null, wire.x + i, wire.y + i, wire.length, wire.isHorizontal));
								}
							}
							
							manager.setSelectedElements(elementsCreated);
							manager.mayThrow(() -> manager.getCircuitBoard().initMove(elementsCreated, false));
							break;
						}
					}
				} catch(Exception exc) {
					exc.printStackTrace();
				} finally {
					editHistory.endGroup();
				}
			}
		});
		
		MenuItem selectAll = new MenuItem("Select All");
		selectAll.setAccelerator(new KeyCharacterCombination("A", KeyCombination.CONTROL_DOWN));
		selectAll.setOnAction(event -> {
			CircuitManager manager = getCurrentCircuit();
			if(manager != null) {
				manager.setSelectedElements(
						Stream.concat(manager.getCircuitBoard().getComponents().stream(),
						              manager.getCircuitBoard().getLinks()
						                     .stream().flatMap(link -> link.getWires().stream()))
						      .collect(Collectors.toSet()));
			}
		});
		
		editMenu.getItems().addAll(undo, redo, copy, cut, paste, selectAll);
		
		Menu circuitsMenu = new Menu("Circuits");
		MenuItem newCircuit = new MenuItem("New circuit");
		newCircuit.setOnAction(event -> createCircuit("New circuit"));
		circuitsMenu.getItems().add(newCircuit);
		
		Menu clockMenu = new Menu("Clock");
		toggleClock = new MenuItem("Start clock");
		toggleClock.setAccelerator(new KeyCharacterCombination("K", KeyCombination.CONTROL_DOWN));
		toggleClock.setOnAction(event -> {
			if(toggleClock.getText().startsWith("Start")) {
				Clock.startClock(currentClockHz);
				toggleClock.setText("Stop clock");
			} else {
				Clock.stopClock();
				toggleClock.setText("Start clock");
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
		
		clockMenu.getItems().addAll(toggleClock, tickClock, frequenciesMenu);
		
		Menu helpMenu = new Menu("Help");
		MenuItem about = new MenuItem("About");
		about.setOnAction(event -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("About");
			alert.setContentText("Circuit Simulator created by Roi Atalla © 2017");
			alert.showAndWait();
		});
		helpMenu.getItems().addAll(about);
		
		menuBar.getMenus().addAll(fileMenu, editMenu, circuitsMenu, clockMenu, helpMenu);
		
		componentLabel.setFont(Font.font("Sans serif", 16));
		
		ScrollPane propertiesScrollPane = new ScrollPane(propertiesTable);
		propertiesScrollPane.setFitToWidth(true);
		
		VBox propertiesBox = new VBox(componentLabel, propertiesScrollPane);
		propertiesBox.setAlignment(Pos.TOP_CENTER);
		VBox.setVgrow(propertiesScrollPane, Priority.ALWAYS);
		
		SplitPane leftPaneSplit = new SplitPane(buttonTabPane, propertiesBox);
		leftPaneSplit.setOrientation(Orientation.VERTICAL);
		leftPaneSplit.setDividerPositions(0.65);
		leftPaneSplit.setPrefWidth(450);
		
		SplitPane.setResizableWithParent(buttonTabPane, Boolean.FALSE);
		
		overlayCanvas = new Canvas();
		overlayCanvas.setDisable(true);
		
		Pane pane = new Pane(overlayCanvas);
		pane.setDisable(true);
		
		overlayCanvas.widthProperty().bind(pane.widthProperty());
		overlayCanvas.heightProperty().bind(pane.heightProperty());
		
		StackPane canvasStackPane = new StackPane(canvasTabPane, pane);
		
		SplitPane canvasPropsSplit = new SplitPane(leftPaneSplit, canvasStackPane);
		canvasPropsSplit.setOrientation(Orientation.HORIZONTAL);
		canvasPropsSplit.setDividerPositions(0.35);
		
		SplitPane.setResizableWithParent(leftPaneSplit, Boolean.FALSE);
		
		ToolBar toolBar = new ToolBar();
		
		Function<Pair<String, String>, ToggleButton> createToolbarButton = pair -> {
			ComponentLauncherInfo info = componentManager.get(pair);
			ToggleButton button = new ToggleButton("", setupImageView(info.image));
			button.setTooltip(new Tooltip(pair.getValue()));
			button.setMinWidth(50);
			button.setMinHeight(50);
			button.setToggleGroup(buttonsToggleGroup);
			button.setOnAction(event -> modifiedSelection(info));
			return button;
		};
		
		ToggleButton inputPinButton = createToolbarButton.apply(new Pair<>("Wiring", "Input Pin"));
		ToggleButton outputPinButton = createToolbarButton.apply(new Pair<>("Wiring", "Output Pin"));
		ToggleButton andButton = createToolbarButton.apply(new Pair<>("Gates", "AND"));
		ToggleButton orButton = createToolbarButton.apply(new Pair<>("Gates", "OR"));
		ToggleButton notButton = createToolbarButton.apply(new Pair<>("Gates", "NOT"));
		ToggleButton xorButton = createToolbarButton.apply(new Pair<>("Gates", "XOR"));
		ToggleButton tunnelButton = createToolbarButton.apply(new Pair<>("Wiring", "Tunnel"));
		
		toolBar.getItems().addAll(inputPinButton, outputPinButton, andButton,
		                          orButton, notButton, xorButton, tunnelButton,
		                          new Label("Global bit size:"), bitSizeSelect,
		                          new Label("Global secondary:"), secondaryOptionSelect);
		
		VBox.setVgrow(canvasPropsSplit, Priority.ALWAYS);
		Scene scene = new Scene(new VBox(menuBar, toolBar, canvasPropsSplit));
		
		stage.setScene(scene);
		stage.setTitle("Circuit Simulator");
		stage.sizeToScene();
		stage.show();
		stage.centerOnScreen();
		
		stage.setOnCloseRequest(this::checkUnsavedChanges);
		
		new AnimationTimer() {
			private long lastRepaint;
			private int lastFrameCount;
			private int frameCount;
			
			@Override
			public void handle(long now) {
				Platform.runLater(() -> {
					if(now - lastRepaint >= 1e9) {
						lastFrameCount = frameCount;
						frameCount = 0;
						lastRepaint = now;
					}
					
					frameCount++;
					
					CircuitManager manager = getCurrentCircuit();
					if(manager != null) {
						manager.paint();
					}
					
					GraphicsContext graphics = overlayCanvas.getGraphicsContext2D();
					graphics.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
					
					graphics.save();
					graphics.setFont(Font.font("monospace", 10));
					graphics.setStroke(Color.BLACK);
					graphics.strokeText("FPS: " + lastFrameCount, 5, 50);
					
					if(manager != null) {
						String message = manager.getCurrentError();
						
						Bounds bounds = GuiUtils.getBounds(graphics.getFont(), message);
						graphics.setFont(Font.font("monospace", 13));
						graphics.setStroke(Color.BLACK);
						graphics.strokeText(message,
						                    (overlayCanvas.getWidth() - bounds.getWidth()) * 0.5,
						                    overlayCanvas.getHeight() - 50);
					}
					
					graphics.restore();
				});
			}
		}.start();
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
			case RIGHT: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.EAST);
				updateProperties(props);
				break;
			}
			case LEFT: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.WEST);
				updateProperties(props);
				break;
			}
			case UP: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.NORTH);
				updateProperties(props);
				break;
			}
			case DOWN: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.SOUTH);
				updateProperties(props);
				break;
			}
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
