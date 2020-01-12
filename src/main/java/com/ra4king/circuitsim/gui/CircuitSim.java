package com.ra4king.circuitsim.gui;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonSyntaxException;
import com.ra4king.circuitsim.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsim.gui.ComponentManager.ComponentLauncherInfo;
import com.ra4king.circuitsim.gui.EditHistory.EditAction;
import com.ra4king.circuitsim.gui.LinkWires.Wire;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.Properties.PropertyCircuitValidator;
import com.ra4king.circuitsim.gui.file.FileFormat;
import com.ra4king.circuitsim.gui.file.FileFormat.CircuitFile;
import com.ra4king.circuitsim.gui.file.FileFormat.CircuitInfo;
import com.ra4king.circuitsim.gui.file.FileFormat.ComponentInfo;
import com.ra4king.circuitsim.gui.file.FileFormat.WireInfo;
import com.ra4king.circuitsim.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.ShortCircuitException;
import com.ra4king.circuitsim.simulator.SimulationException;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.components.Subcircuit;
import com.ra4king.circuitsim.simulator.components.wiring.Clock;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class CircuitSim extends Application {
	public static final String VERSION = "1.8.2";
	
	private static boolean mainCalled = false;
	private static AtomicBoolean versionChecked = new AtomicBoolean(false);
	
	public static void main(String[] args) {
		mainCalled = true;
		launch(args);
	}
	
	private DebugUtil debugUtil = new DebugUtil(this);
	
	private Stage stage;
	private Scene scene;
	private boolean openWindow = true;
	
	private Simulator simulator;
	private CheckMenuItem simulationEnabled;
	
	private MenuItem undo, redo;
	private CheckMenuItem clockEnabled;
	private Menu frequenciesMenu;
	private MenuItem help;
	
	private ToggleButton clickMode;
	private boolean clickedDirectly;
	
	private ComponentManager componentManager;
	private List<String> libraryPaths;
	
	private TabPane buttonTabPane;
	private ToggleGroup buttonsToggleGroup;
	private Runnable refreshComponentsTabs;
	
	private ComboBox<Integer> bitSizeSelect;
	private ComboBox<Double> scaleFactorSelect;
	private Label fpsLabel;
	private Label clockLabel;
	private Label messageLabel;
	
	private GridPane propertiesTable;
	private Label componentLabel;
	
	private Tab circuitButtonsTab;
	private TabPane canvasTabPane;
	private Map<String, Pair<ComponentLauncherInfo, CircuitManager>> circuitManagers;
	
	private ComponentLauncherInfo selectedComponent;
	
	private File saveFile, lastSaveFile;
	private boolean loadingFile;
	
	private static DataFormat copyDataFormat = new DataFormat("x-circuit-simulator");
	
	private EditHistory editHistory;
	private int savedEditStackSize;
	
	private Exception lastException;
	private long lastExceptionTime;
	private static final long SHOW_ERROR_DURATION = 3000;
	
	private volatile boolean needsRepaint = true;
	
	/**
	 * Throws an exception if instantiated directly
	 */
	public CircuitSim() {
		if(!mainCalled) {
			throw new IllegalStateException("Wrong constructor");
		}
	}
	
	/**
	 * Creates a new instance of a CircuitSimulator
	 *
	 * @param openWindow If a window should be opened
	 */
	public CircuitSim(boolean openWindow) {
		this.openWindow = openWindow;
		
		runFxSync(() -> {
			init();
			start(new Stage());
		});
	}
	
	public boolean isWindowOpen() {
		return openWindow;
	}
	
	private void runFxSync(Runnable runnable) {
		if(Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			final CountDownLatch latch = new CountDownLatch(1);
			try {
				Platform.runLater(() -> {
					try {
						runnable.run();
					} finally {
						latch.countDown();
					}
				});
			} catch(IllegalStateException exc) {
				if(latch.getCount() > 0) {
					// JavaFX Platform not initialized
					
					Runnable startup = () -> {
						try {
							runnable.run();
						} finally {
							latch.countDown();
						}
					};
					
					Platform.startup(startup);
				} else {
					throw exc;
				}
			}
			
			try {
				latch.await();
			} catch(InterruptedException exc) {
				throw new RuntimeException(exc);
			}
		}
	}
	
	/**
	 * Do not call this directly, called automatically
	 */
	@Override
	public void init() {
		if(simulator != null) {
			throw new IllegalStateException("Already initialized");
		}
		
		simulator = new Simulator();
		circuitManagers = new HashMap<>();
		Clock.addChangeListener(simulator, value -> runSim());
		
		editHistory = new EditHistory(this);
		editHistory.addListener((action, manager, params) -> {
			updateTitle();
			circuitManagers.values().stream().map(Pair::getValue).forEach(this::updateCanvasSize);
		});
		
		componentManager = new ComponentManager();
	}
	
	/**
	 * Get the Simulator instance.
	 *
	 * @return The Simulator instance used by this Circuit Simulator.
	 */
	public Simulator getSimulator() {
		return simulator;
	}
	
	/**
	 * Get the global EditHistory instance
	 *
	 * @return the EditHistory used in this Circuit Simulator instance
	 */
	public EditHistory getEditHistory() {
		return editHistory;
	}
	
	/**
	 * The stage (window) of this Circuit Simulator instance
	 *
	 * @return the Stage
	 */
	public Stage getStage() {
		return stage;
	}
	
	/**
	 * The scene of this Circuit Simulator instance.
	 *
	 * @return the Scene
	 */
	public Scene getScene() {
		return scene;
	}
	
	public DebugUtil getDebugUtil() {
		return debugUtil;
	}
	
	/**
	 * Sets the global click mode.
	 *
	 * @param selected If true, mouse clicking goes through to components; else, it goes to the circuit editor.
	 */
	public void setClickMode(boolean selected) {
		if(!clickedDirectly) {
			clickMode.setSelected(selected);
		}
	}
	
	/**
	 * Returns the current click mode.
	 *
	 * @return If true, mouse clicking goes through to components; else, it goes to the circuit editor.
	 */
	public boolean isClickMode() {
		return clickMode.isSelected();
	}
	
	public double getScaleFactor() {
		return scaleFactorSelect.getSelectionModel().getSelectedItem();
	}
	
	public double getScaleFactorInverted() {
		return 1.0 / getScaleFactor();
	}
	
	void zoomIn(double x, double y) {
		int selectedIndex = scaleFactorSelect.getSelectionModel().getSelectedIndex();
		if(selectedIndex < scaleFactorSelect.getItems().size()) {
			scaleFactorSelect.getSelectionModel().select(selectedIndex + 1);
			
			setScrollPosition(x, y);
		}
	}
	
	void zoomOut(double x, double y) {
		int selectedIndex = scaleFactorSelect.getSelectionModel().getSelectedIndex();
		if(selectedIndex > 0) {
			scaleFactorSelect.getSelectionModel().select(selectedIndex - 1);
			
			setScrollPosition(x, y);
		}
	}
	
	private void setScrollPosition(double x, double y) {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			ScrollPane scrollPane = manager.getCanvasScrollPane();
			Canvas canvas = manager.getCanvas();
			
			scrollPane.setHvalue(scrollPane.getHmax() * x / canvas.getWidth());
			scrollPane.setVvalue(scrollPane.getVmax() * y / canvas.getHeight());
		}
	}
	
	/**
	 * Get all circuits.
	 *
	 * @return Map from their names to their wrapping CircuitBoard in appearance order.
	 */
	public LinkedHashMap<String, CircuitBoard> getCircuitBoards() {
		return circuitManagers.keySet().stream()
		                      .collect(Collectors.toMap(name -> name,
		                                                name -> circuitManagers.get(name).getValue()
		                                                                       .getCircuitBoard(),
		                                                (v1, v2) -> v1,
		                                                LinkedHashMap::new));
	}
	
	/**
	 * Get the ComponentManager. New components may be registered to this instance.
	 *
	 * @return The ComponentManager of this Circuit Simulator instance.
	 */
	public ComponentManager getComponentManager() {
		return componentManager;
	}
	
	public boolean isSimulationEnabled() {
		return simulationEnabled.isSelected();
	}
	
	private void runSim() {
		try {
			if(isSimulationEnabled() && simulator.hasLinksToUpdate()) {
				needsRepaint = true;
				simulator.stepAll();
			}
		} catch(SimulationException exc) {
			setLastException(exc);
		} catch(Exception exc) {
			setLastException(exc);
			getDebugUtil().logException(exc);
		}
	}
	
	private String getCurrentError() {
		CircuitManager manager = getCurrentCircuit();
		
		if(lastException != null && SHOW_ERROR_DURATION < System.currentTimeMillis() - lastExceptionTime) {
			lastException = null;
		}
		
		Exception exc =
			lastException != null ? lastException : manager != null ? manager.getCurrentError() : null;
		
		return exc == null || exc.getMessage() == null
		       ? "" : (exc instanceof ShortCircuitException ? "Short circuit detected" : exc.getMessage());
	}
	
	private void setLastException(Exception lastException) {
		this.lastException = lastException;
		this.lastExceptionTime = System.currentTimeMillis();
	}
	
	private int getCurrentClockSpeed() {
		for(MenuItem menuItem : frequenciesMenu.getItems()) {
			RadioMenuItem clockItem = (RadioMenuItem)menuItem;
			if(clockItem.isSelected()) {
				String text = clockItem.getText();
				int space = text.indexOf(' ');
				if(space == -1) {
					throw new IllegalStateException("What did you do...");
				}
				
				return Integer.parseInt(text.substring(0, space));
			}
		}
		
		throw new IllegalStateException("This can't happen lol");
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
	
	public Map<String, CircuitManager> getCircuitManagers() {
		return circuitManagers.entrySet().stream()
		                      .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getValue()));
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
	
	private Tab getTabForCircuit(String name) {
		for(Tab tab : canvasTabPane.getTabs()) {
			if(tab.getText().equals(name)) {
				return tab;
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
	
	/**
	 * Selects the tab of the specified circuit and changes its current state to the specified state.
	 *
	 * @param circuit The circuit whose tab will be selected
	 * @param state   The state to set as the current state. May be null (no change to the current state).
	 */
	public void switchToCircuit(Circuit circuit, CircuitState state) {
		runFxSync(() -> {
			if(state != null) {
				CircuitManager manager = getCircuitManager(circuit);
				if(manager != null) {
					manager.getCircuitBoard().setCurrentState(state);
				}
			}
			
			Tab tab = getTabForCircuit(circuit);
			if(tab != null) {
				canvasTabPane.getSelectionModel().select(tab);
				needsRepaint = true;
			}
		});
	}
	
	void readdCircuit(CircuitManager manager, Tab tab, int index) {
		canvasTabPane.getTabs().add(Math.min(index, canvasTabPane.getTabs().size()), tab);
		circuitManagers.put(tab.getText(), new Pair<>(createSubcircuitLauncherInfo(tab.getText()), manager));
		manager.getCircuitBoard().setCurrentState(manager.getCircuit().getTopLevelState());
		
		canvasTabPane.getSelectionModel().select(tab);
		
		refreshCircuitsTab();
	}
	
	boolean confirmAndDeleteCircuit(CircuitManager circuitManager, boolean removeTab) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(stage);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.setTitle("Delete \"" + circuitManager.getName() + "\"?");
		alert.setHeaderText("Delete \"" + circuitManager.getName() + "\"?");
		alert.setContentText("Are you sure you want to delete this circuit?");
		
		Optional<ButtonType> result = alert.showAndWait();
		if(!result.isPresent() || result.get() != ButtonType.OK) {
			return false;
		} else {
			deleteCircuit(circuitManager, removeTab, true);
			return true;
		}
	}
	
	/**
	 * Delete the specified circuit.
	 *
	 * @param name The name of the circuit to delete.
	 */
	public void deleteCircuit(String name) {
		deleteCircuit(name, true);
	}
	
	public void deleteCircuit(String name, boolean addNewOnEmpty) {
		deleteCircuit(getCircuitManager(name), true, addNewOnEmpty);
	}
	
	void deleteCircuit(CircuitManager manager, boolean removeTab, boolean addNewOnEmpty) {
		runFxSync(() -> {
			clearSelection();
			
			Tab tab = getTabForCircuit(manager.getCircuit());
			if(tab == null) {
				throw new IllegalStateException("Tab shouldn't be null.");
			}
			
			int idx = canvasTabPane.getTabs().indexOf(tab);
			if(idx == -1) throw new IllegalStateException("Tab should be in the tab pane.");
			
			boolean isEmpty;
			
			if(removeTab) {
				canvasTabPane.getTabs().remove(tab);
				isEmpty = canvasTabPane.getTabs().isEmpty();
			} else {
				isEmpty = canvasTabPane.getTabs().size() == 1;
			}
			
			editHistory.beginGroup();
			
			Pair<ComponentLauncherInfo, CircuitManager> removed = circuitManagers.remove(tab.getText());
			circuitModified(removed.getValue().getCircuit(), null, false);
			
			editHistory.addAction(EditAction.DELETE_CIRCUIT, manager, tab, idx);
			
			if(addNewOnEmpty && isEmpty) {
				createCircuit("New circuit");
				canvasTabPane.getSelectionModel().select(0);
			}
			
			editHistory.endGroup();
			
			refreshCircuitsTab();
		});
	}
	
	void clearProperties() {
		setProperties("", null);
	}
	
	void setProperties(ComponentPeer<?> componentPeer) {
		String name;
		if(componentPeer.getClass() == SubcircuitPeer.class) {
			name = componentPeer.getProperties().getProperty(SubcircuitPeer.SUBCIRCUIT).getStringValue();
		} else {
			ComponentLauncherInfo info = componentManager.get(componentPeer.getClass(), componentPeer.getProperties());
			name = info.name.getValue();
		}
		setProperties(name, componentPeer.getProperties());
	}
	
	void setProperties(String componentName, Properties properties) {
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
					
					Label name = new Label(property.display);
					GridPane.setHgrow(name, Priority.ALWAYS);
					name.setMaxWidth(Double.MAX_VALUE);
					name.setMinHeight(30);
					name.setBackground(
						new Background(
							new BackgroundFill((size / 2) % 2 == 0 ? Color.LIGHTGRAY : Color.WHITE, null, null)));
					
					Node node =
						property.validator.createGui(
							stage,
							property.value,
							newValue ->
								Platform.runLater(() -> {
									Properties newProperties = new Properties(properties);
									newProperties.setValue(property, newValue);
									updateProperties(newProperties);
								}));
					
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
		return properties;
	}
	
	void clearSelection() {
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
	
	void refreshCircuitsTab() {
		if(loadingFile) return;
		
		Platform.runLater(() -> {
			ScrollPane pane = new ScrollPane(new GridPane());
			pane.setFitToWidth(true);
			
			if(circuitButtonsTab == null) {
				circuitButtonsTab = new Tab("Circuits");
				circuitButtonsTab.setClosable(false);
				circuitButtonsTab.setContent(pane);
				buttonTabPane.getTabs().add(circuitButtonsTab);
			} else {
				// Clear toggle groups, as they take up memory and don't get cleared automatically
				GridPane buttons = (GridPane)((ScrollPane)circuitButtonsTab.getContent()).getContent();
				buttons.getChildren().forEach(node -> {
					ToggleButton button = (ToggleButton)node;
					button.setToggleGroup(null);
				});
				buttons.getChildren().clear();
				
				circuitButtonsTab.setContent(pane);
			}
			
			// when requesting a tab to be closed, it still exists and thus its button could be created twice.
			Set<String> seen = new HashSet<>();
			
			canvasTabPane.getTabs().forEach(tab -> {
				String name = tab.getText();
				Pair<ComponentLauncherInfo, CircuitManager> circuitPair = circuitManagers.get(name);
				if(circuitPair == null || seen.contains(name)) return;
				seen.add(name);
				
				ComponentPeer<?> component = circuitPair.getKey().creator.createComponent(new Properties(), 0, 0);
				
				Canvas icon = new Canvas(component.getScreenWidth() + 10, component.getScreenHeight() + 10);
				GraphicsContext graphics = icon.getGraphicsContext2D();
				graphics.translate(5, 5);
				component.paint(icon.getGraphicsContext2D(), null);
				component.getConnections().forEach(connection -> connection.paint(icon.getGraphicsContext2D(), null));
				
				ToggleButton toggleButton = new ToggleButton(circuitPair.getKey().name.getValue(), icon);
				toggleButton.setAlignment(Pos.CENTER_LEFT);
				toggleButton.setToggleGroup(buttonsToggleGroup);
				toggleButton.setMinHeight(30);
				toggleButton.setMaxWidth(Double.MAX_VALUE);
				toggleButton.setOnAction(e -> {
					if(toggleButton.isSelected()) {
						modifiedSelection(circuitPair.getKey());
					} else {
						modifiedSelection(null);
					}
				});
				GridPane.setHgrow(toggleButton, Priority.ALWAYS);
				
				GridPane buttons = (GridPane)pane.getContent();
				buttons.addRow(buttons.getChildren().size(), toggleButton);
			});
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
		stage.setTitle("CircuitSim v" + VERSION + name);
	}
	
	private ComponentCreator<?> getSubcircuitPeerCreator(String name) {
		return (props, x, y) -> {
			Properties properties = new Properties(props);
			properties.parseAndSetValue(SubcircuitPeer.SUBCIRCUIT, new PropertyCircuitValidator(this), name);
			try {
				return new SubcircuitPeer(properties, x, y);
			} catch(SimulationException exc) {
				throw new SimulationException(
					"Error creating subcircuit for circuit '" + name + "'", exc);
			} catch(Exception exc) {
				throw new RuntimeException(
					"Error creating subcircuit for circuit '" + name + "':", exc);
			}
		};
	}
	
	private ComponentLauncherInfo createSubcircuitLauncherInfo(String name) {
		return new ComponentLauncherInfo(SubcircuitPeer.class,
		                                 new Pair<>("Circuits", name),
		                                 null,
		                                 new Properties(),
		                                 getSubcircuitPeerCreator(name));
	}
	
	/**
	 * Renames the circuit specified by name to the name specified by newName.
	 *
	 * @param name    The name of the existing circuit.
	 * @param newName The new name to rename to.
	 */
	public void renameCircuit(String name, String newName) {
		renameCircuit(getTabForCircuit(name), newName);
	}
	
	void renameCircuit(Tab tab, String newName) {
		runFxSync(() -> {
			if(circuitManagers.containsKey(newName)) {
				throw new IllegalArgumentException("Name already exists");
			}
			
			String oldName = tab.getText();
			
			Pair<ComponentLauncherInfo, CircuitManager> removed = circuitManagers.remove(oldName);
			Pair<ComponentLauncherInfo, CircuitManager> newPair =
				new Pair<>(createSubcircuitLauncherInfo(newName), removed.getValue());
			circuitManagers.put(newName, newPair);
			
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
			newPair.getValue().setName(newName);
			
			editHistory.addAction(EditAction.RENAME_CIRCUIT, null, this, tab, oldName, newName);
			
			refreshCircuitsTab();
		});
	}
	
	void updateCanvasSize(CircuitManager circuitManager) {
		OptionalInt maxX = Stream.concat(circuitManager.getSelectedElements().stream(),
		                                 Stream.concat(circuitManager.getCircuitBoard().getComponents().stream(),
		                                               circuitManager.getCircuitBoard().getLinks().stream().flatMap(
			                                               links -> links.getWires().stream())))
		                         .mapToInt(componentPeer -> componentPeer.getX() + componentPeer.getWidth())
		                         .max();
		
		double maxWidth = Math.min(5000, getScaleFactor() * (maxX.orElse(0) + 5) * GuiUtils.BLOCK_SIZE);
		circuitManager.getCanvas().setWidth(
			maxWidth < circuitManager.getCanvasScrollPane().getWidth()
			? circuitManager.getCanvasScrollPane().getWidth()
			: maxWidth);
		
		OptionalInt maxY = Stream.concat(circuitManager.getSelectedElements().stream(),
		                                 Stream.concat(circuitManager.getCircuitBoard().getComponents().stream(),
		                                               circuitManager.getCircuitBoard().getLinks().stream().flatMap(
			                                               links -> links.getWires().stream())))
		                         .mapToInt(componentPeer -> componentPeer.getY() + componentPeer.getHeight())
		                         .max();
		
		double maxHeight = Math.min(5000, getScaleFactor() * (maxY.orElse(0) + 5) * GuiUtils.BLOCK_SIZE);
		circuitManager.getCanvas().setHeight(
			maxHeight < circuitManager.getCanvasScrollPane().getHeight()
			? circuitManager.getCanvasScrollPane().getHeight()
			: maxHeight);
		
		needsRepaint = true;
	}
	
	/**
	 * if Component == null, the circuit was deleted
	 */
	void circuitModified(Circuit circuit, Component component, boolean added) {
		if(component == null || component instanceof Pin) {
			refreshCircuitsTab();
			
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
		} else if(component instanceof Subcircuit && !added) {
			Subcircuit subcircuit = (Subcircuit)component;
			
			CircuitNode node =
				getSubcircuitStates(subcircuit, getCircuitManager(circuit).getCircuitBoard().getCurrentState());
			resetSubcircuitStates(node);
		}
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
	
	private boolean checkUnsavedChanges() {
		clearSelection();
		
		if(editHistory.editStackSize() != savedEditStackSize) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.initOwner(stage);
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("Unsaved changes");
			alert.setHeaderText("Unsaved changes");
			alert.setContentText("There are unsaved changes, do you want to save them?");
			
			ButtonType discard = new ButtonType("Discard", ButtonData.NO);
			alert.getButtonTypes().add(discard);
			
			Optional<ButtonType> result = alert.showAndWait();
			if(result.isPresent()) {
				if(result.get() == ButtonType.OK) {
					saveCircuitsInternal();
					return saveFile == null;
				} else {
					return result.get() == ButtonType.CANCEL;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Clears and destroys all circuits. No tabs or circuits will exist after this.
	 */
	public void clearCircuits() {
		runFxSync(() -> {
			Clock.reset(simulator);
			clockEnabled.setSelected(false);
			
			editHistory.disable();
			circuitManagers.forEach((name, pair) -> pair.getValue().destroy());
			editHistory.enable();
			
			circuitManagers.clear();
			canvasTabPane.getTabs().clear();
			simulator.clear();
			
			editHistory.clear();
			savedEditStackSize = 0;
			
			saveFile = null;
			
			undo.setDisable(true);
			redo.setDisable(true);
			
			updateTitle();
			refreshCircuitsTab();
		});
	}
	
	void copySelectedComponents() {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			Set<GuiElement> selectedElements = manager.getSelectedElements();
			if(selectedElements.isEmpty()) {
				return;
			}
			
			List<ComponentInfo> components =
				selectedElements
					.stream()
					.filter(element -> element instanceof ComponentPeer<?>)
					.map(element -> (ComponentPeer<?>)element)
					.map(component ->
						     new ComponentInfo(component.getClass().getName(),
						                       component.getX(), component.getY(),
						                       component.getProperties()))
					.collect(Collectors.toList());
			
			List<WireInfo> wires = selectedElements
				                       .stream()
				                       .filter(element -> element instanceof Wire)
				                       .map(element -> (Wire)element)
				                       .map(wire -> new WireInfo(wire.getX(), wire.getY(),
				                                                 wire.getLength(), wire.isHorizontal()))
				                       .collect(Collectors.toList());
			
			try {
				String data = FileFormat.stringify(
					new CircuitFile(0, 0, null, Collections.singletonList(
						new CircuitInfo("Copy", components, wires))));
				
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				content.put(copyDataFormat, data);
				clipboard.setContent(content);
			} catch(Exception exc) {
				setLastException(exc);
				getDebugUtil().logException("Error while copying", exc);
			}
		}
	}
	
	void cutSelectedComponents() {
		CircuitManager manager = getCurrentCircuit();
		if(manager != null) {
			copySelectedComponents();
			
			manager.mayThrow(() -> manager.getCircuitBoard().finalizeMove());
			
			Set<GuiElement> selectedElements = manager.getSelectedElements();
			manager.mayThrow(() -> manager.getCircuitBoard().removeElements(selectedElements));
			
			clearSelection();
			
			needsRepaint = true;
		}
	}
	
	void pasteFromClipboard() {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		String data = (String)clipboard.getContent(copyDataFormat);
		
		if(data != null) {
			try {
				editHistory.beginGroup();
				
				CircuitFile parsed = FileFormat.parse(data);
				
				CircuitManager manager = getCurrentCircuit();
				if(manager != null) {
					outer:
					for(int i = 0; ; i += 3) { // start 0 in the case of Cut and Paste
						Set<GuiElement> elementsCreated = new HashSet<>();
						
						for(CircuitInfo circuit : parsed.circuits) {
							for(ComponentInfo component : circuit.components) {
								try {
									@SuppressWarnings("unchecked")
									Class<? extends ComponentPeer<?>> clazz =
										(Class<? extends ComponentPeer<?>>)Class.forName(component.name);
									
									Properties properties = new Properties();
									component.properties.forEach(
										(key, value) -> properties.setProperty(
											new Property<>(key, null, value)));
									
									ComponentCreator<?> creator;
									if(clazz == SubcircuitPeer.class) {
										creator = getSubcircuitPeerCreator(
											properties.getValueOrDefault(SubcircuitPeer.SUBCIRCUIT, ""));
									} else {
										creator = componentManager.get(clazz, properties).creator;
									}
									
									ComponentPeer<?> created = creator.createComponent(properties,
									                                                   component.x + i,
									                                                   component.y + i);
									
									if(!manager.getCircuitBoard().isValidLocation(created)) {
										elementsCreated.clear();
										continue outer;
									}
									
									elementsCreated.add(created);
								} catch(SimulationException exc) {
									exc.printStackTrace();
									setLastException(exc);
								} catch(Exception exc) {
									setLastException(exc);
									getDebugUtil().logException("Error loading component " + component.name, exc);
								}
							}
						}
						
						int offset = i;
						simulator.runSync(() -> {
							manager.getCircuitBoard().finalizeMove();
							
							editHistory.disable();
							elementsCreated.forEach(
								element -> manager.mayThrow(
									() -> manager.getCircuitBoard().addComponent((ComponentPeer<?>)element, false)));
							manager.getCircuitBoard().removeElements(elementsCreated, false);
							editHistory.enable();
							
							for(CircuitInfo circuit : parsed.circuits) {
								for(WireInfo wire : circuit.wires) {
									elementsCreated.add(
										new Wire(null,
										         wire.x + offset,
										         wire.y + offset,
										         wire.length,
										         wire.isHorizontal));
								}
							}
							
							manager.setSelectedElements(elementsCreated);
							manager.mayThrow(() -> manager.getCircuitBoard().initMove(elementsCreated, false));
						});
						
						break;
					}
				}
			} catch(SimulationException exc) {
				exc.printStackTrace();
				setLastException(exc);
			} catch(Exception exc) {
				setLastException(exc);
				getDebugUtil().logException("Error while pasting", exc);
			} finally {
				editHistory.endGroup();
				needsRepaint = true;
			}
		}
	}
	
	private static AtomicBoolean checkingForUpdate = new AtomicBoolean(false);
	
	private void checkForUpdate(boolean showOk) {
		if(checkingForUpdate.compareAndSet(false, true)) {
			Thread versionCheckThread = new Thread(() -> {
				try {
					URL url = new URL("https://www.roiatalla.com/public/CircuitSim/version.txt");
					
					String remoteVersion;
					
					try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
						remoteVersion = reader.readLine();
					} catch(IOException exc) {
						System.err.println("Error checking server for version.");
						exc.printStackTrace();
						
						if(showOk) {
							runFxSync(() -> {
								Alert alert = new Alert(AlertType.ERROR);
								alert.initOwner(stage);
								alert.initModality(Modality.APPLICATION_MODAL);
								alert.setTitle("Error");
								alert.setHeaderText("Error checking server");
								alert.setContentText("Error occurred when checking server for new update.\n" + exc);
								alert.show();
							});
						}
						
						return;
					}
					
					boolean updateAvailable = false;
					{
						String local = VERSION;
						
						int beta = local.indexOf('b');
						if(beta != -1) {
							local = local.substring(0, beta);
						}
						
						String[] parts = local.split("\\.");
						int localMajor = Integer.parseInt(parts[0]);
						int localMinor = Integer.parseInt(parts[1]);
						int localBugfix = Integer.parseInt(parts[2]);
						
						parts = remoteVersion.split("\\.");
						int remoteMajor = Integer.parseInt(parts[0]);
						int remoteMinor = Integer.parseInt(parts[1]);
						int remoteBugfix = Integer.parseInt(parts[2]);
						
						if(remoteMajor > localMajor) {
							updateAvailable = true;
						} else if(remoteMajor == localMajor) {
							
							if(remoteMinor > localMinor) {
								updateAvailable = true;
							} else if(remoteMinor == localMinor) {
								
								if(remoteBugfix > localBugfix) {
									updateAvailable = true;
								}
								if(beta != -1 && remoteBugfix == localBugfix) {
									updateAvailable = true;
								}
							}
						}
					}
					
					if(updateAvailable) {
						runFxSync(() -> {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.initOwner(stage);
							alert.initModality(Modality.APPLICATION_MODAL);
							alert.setTitle("New Version Available");
							alert.setHeaderText("New version available: CircuitSim v" + remoteVersion);
							alert.setContentText("Click Update to open a browser to the download location.");
							alert.getButtonTypes().set(0, new ButtonType("Update", ButtonData.OK_DONE));
							alert.getButtonTypes().add(ButtonType.CANCEL);
							Optional<ButtonType> result = alert.showAndWait();
							if(result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
								getHostServices().showDocument("https://www.roiatalla.com/public/CircuitSim/");
							}
						});
					} else if(showOk) {
						runFxSync(() -> {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.initOwner(stage);
							alert.initModality(Modality.APPLICATION_MODAL);
							alert.setTitle("No new updates");
							alert.setHeaderText("No new updates");
							alert.setContentText("You have the latest version and are good to go! :)");
							alert.show();
						});
					}
				} catch(Exception exc) {
					exc.printStackTrace();
				} finally {
					checkingForUpdate.set(false);
				}
			});
			versionCheckThread.setDaemon(true);
			versionCheckThread.setName("Version Check Thread");
			versionCheckThread.start();
		}
	}
	
	private void loadConfFile() {
		boolean showHelp = true;
		
		String home = System.getProperty("user.home");
		File file = new File(home, ".circuitsim");
		if(file.exists()) {
			boolean newWindow = getParameters() == null;
			
			try {
				List<String> lines = Files.readAllLines(file.toPath());
				for(String line : lines) {
					line = line.trim();
					if(line.charAt(0) == '#') continue;
					
					int comment = line.indexOf('#');
					if(comment != -1) {
						line = line.substring(0, comment).trim();
					}
					
					int idx = line.indexOf('=');
					if(idx == -1) continue;
					
					String key = line.substring(0, idx).trim();
					String value = line.substring(idx + 1).trim();
					
					switch(key) {
						case "WindowX":
							stage.setX(Integer.parseInt(value) + (newWindow ? 20 : 0));
							break;
						case "WindowY":
							stage.setY(Integer.parseInt(value) + (newWindow ? 20 : 0));
							break;
						case "WindowWidth":
							stage.setWidth(Integer.parseInt(value));
							break;
						case "WindowHeight":
							stage.setHeight(Integer.parseInt(value));
							break;
						case "IsMaximized":
							if(!newWindow) {
								stage.setMaximized(Boolean.parseBoolean(value));
							}
							break;
						case "Scale":
							scaleFactorSelect.setValue(Double.parseDouble(value));
							break;
						case "LastSavePath":
							lastSaveFile = new File(value);
							break;
						case "HelpShown":
							if(value.equals(VERSION)) {
								showHelp = false;
							}
							break;
					}
				}
			} catch(IOException exc) {
				exc.printStackTrace();
			} catch(Exception exc) {
				getDebugUtil().logException("Error loading configuration file: " + file, exc);
			}
		}
		
		if(openWindow && showHelp) {
			help.fire();
		}
	}
	
	private void saveConfFile() {
		if(!openWindow) return;
		
		String home = System.getProperty("user.home");
		File file = new File(home, ".circuitsim");
		
		List<String> conf = new ArrayList<>();
		if(stage.isMaximized()) {
			conf.add("IsMaximized=true");
		} else {
			conf.add("WindowX=" + (int)stage.getX());
			conf.add("WindowY=" + (int)stage.getY());
			conf.add("WindowWidth=" + (int)stage.getWidth());
			conf.add("WindowHeight=" + (int)stage.getHeight());
		}
		conf.add("Scale=" + scaleFactorSelect.getValue());
		conf.add("HelpShown=" + VERSION);
		if(lastSaveFile != null) {
			conf.add("LastSavePath=" + lastSaveFile.getAbsolutePath());
		}
		
		try {
			Files.write(file.toPath(), conf);
		} catch(IOException exc) {
			exc.printStackTrace();
		}
	}
	
	private void loadCircuitsInternal(File file) {
		String errorMessage = null;
		try {
			loadCircuits(file);
		} catch(ClassNotFoundException exc) {
			errorMessage = "Could not find class:\n" + exc.getMessage();
		} catch(JsonSyntaxException exc) {
			errorMessage = "Could not parse file:\n" + exc.getCause().getMessage();
		} catch(IOException | NullPointerException | IllegalArgumentException | IllegalStateException exc) {
			exc.printStackTrace();
			errorMessage = "Error: " + exc.getMessage();
		} catch(Exception exc) {
			exc.printStackTrace();
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			exc.printStackTrace(new PrintStream(stream));
			errorMessage = stream.toString();
		}
		
		if(errorMessage != null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(stage);
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("Error loading circuits");
			alert.setHeaderText("Error loading circuits");
			alert.getDialogPane().setContent(new TextArea(errorMessage));
			alert.showAndWait();
		}
	}
	
	private Exception excThrown;
	
	/**
	 * Load the circuits from the specified File. This File is saved for reuse with saveCircuits().
	 * If null is passed in, a FileChooser dialog pops up to select a file.
	 *
	 * @param file The File instance to load the circuits from.
	 */
	public void loadCircuits(File file) throws Exception {
		CountDownLatch loadFileLatch = new CountDownLatch(1);
		
		runFxSync(() -> {
			File f = file;
			
			if(f == null) {
				File initialDirectory =
					lastSaveFile == null
						|| lastSaveFile.getParentFile() == null
						|| !lastSaveFile.getParentFile().isDirectory()
					? new File(System.getProperty("user.dir"))
					: lastSaveFile.getParentFile();
				
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Choose sim file");
				fileChooser.setInitialDirectory(initialDirectory);
				fileChooser.getExtensionFilters()
				           .addAll(new ExtensionFilter("Circuit Sim file", "*.sim"),
				                   new ExtensionFilter("All files", "*"));
				f = fileChooser.showOpenDialog(stage);
			}
			
			if(f != null) {
				ProgressBar bar = new ProgressBar();
				
				Dialog<ButtonType> dialog = new Dialog<>();
				dialog.initOwner(stage);
				dialog.initModality(Modality.WINDOW_MODAL);
				dialog.setTitle("Loading " + f.getName() + "...");
				dialog.setHeaderText("Loading " + f.getName() + "...");
				dialog.setContentText("Parsing file...");
				dialog.setGraphic(bar);
				
				lastSaveFile = f;
				
				Thread loadThread = new Thread(() -> {
					try {
						loadingFile = true;
						
						editHistory.disable();
						
						CircuitFile circuitFile = FileFormat.load(lastSaveFile);
						
						if(circuitFile.circuits == null) {
							throw new NullPointerException("File missing circuits");
						}
						
						clearCircuits();
						
						if(circuitFile.libraryPaths != null) {
							for(String libraryPath : circuitFile.libraryPaths) {
								File libraryFile = new File(libraryPath);
								if(libraryFile.isFile()) {
									Platform.runLater(
										() -> dialog.setContentText("Loading library " + libraryFile.getName()));
									runFxSync(() -> loadLibrary(libraryFile));
								} else {
									throw new IllegalArgumentException("Library does not exist: " + libraryPath);
								}
							}
						}
						
						Platform.runLater(() -> {
							bar.setProgress(0.1);
							dialog.setContentText("Creating circuits...");
						});
						
						int totalComponents = 0;
						
						for(CircuitInfo circuit : circuitFile.circuits) {
							if(circuitManagers.containsKey(circuit.name)) {
								throw new IllegalStateException("Duplicate circuit names not allowed.");
							}
							
							createCircuit(circuit.name);
							
							if(circuit.components == null) {
								throw new NullPointerException("Circuit " + circuit.name + " missing components");
							}
							
							if(circuit.wires == null) {
								throw new NullPointerException("Circuit " + circuit.name + " missing wires");
							}
							
							totalComponents += circuit.components.size() + circuit.wires.size();
						}
						
						Platform.runLater(() -> dialog.setContentText("Creating components..."));
						
						Queue<Runnable> runnables = new ArrayDeque<>();
						
						final CountDownLatch latch = new CountDownLatch(totalComponents + 1);
						
						double increment = (1.0 - bar.getProgress()) / totalComponents;
						
						for(CircuitInfo circuit : circuitFile.circuits) {
							CircuitManager manager = getCircuitManager(circuit.name);
							
							for(ComponentInfo component : circuit.components) {
								try {
									@SuppressWarnings("unchecked")
									Class<? extends ComponentPeer<?>> clazz =
										(Class<? extends ComponentPeer<?>>)Class.forName(component.name);
									
									Properties properties = new Properties();
									if(component.properties != null) {
										component.properties.forEach(
											(key, value) -> properties.setProperty(new Property<>(key, null, value)));
									}
									
									ComponentCreator<?> creator;
									if(clazz == SubcircuitPeer.class) {
										creator = getSubcircuitPeerCreator(
											properties.getValueOrDefault(SubcircuitPeer.SUBCIRCUIT, ""));
									} else {
										creator = componentManager.get(clazz, properties).creator;
									}
									
									runnables.add(() -> {
										manager.mayThrow(
											() -> manager.getCircuitBoard().addComponent(
												creator.createComponent(properties, component.x, component.y)));
										bar.setProgress(bar.getProgress() + increment);
										latch.countDown();
									});
								} catch(SimulationException exc) {
									exc.printStackTrace();
									excThrown = exc;
									latch.countDown();
								} catch(Exception exc) {
									excThrown = exc;
									getDebugUtil().logException("Error loading component " + component.name, exc);
									latch.countDown();
								}
							}
							
							for(WireInfo wire : circuit.wires) {
								runnables.add(() -> {
									manager.mayThrow(
										() -> manager.getCircuitBoard()
										             .addWire(wire.x, wire.y, wire.length, wire.isHorizontal));
									bar.setProgress(bar.getProgress() + increment);
									latch.countDown();
								});
							}
						}
						
						int comps = totalComponents;
						Thread tasksThread = new Thread(() -> {
							final int maxRunLater = Math.max(comps / 20, 50);
							
							while(!runnables.isEmpty()) {
								int left = Math.min(runnables.size(), maxRunLater);
								
								CountDownLatch l = new CountDownLatch(left);
								
								for(int i = 0; i < left; i++) {
									Runnable r = runnables.poll();
									Platform.runLater(() -> {
										try {
											r.run();
										} finally {
											l.countDown();
										}
									});
								}
								
								try {
									l.await();
								} catch(Exception exc) {
									// ignore
								}
							}
							
							Platform.runLater(() -> {
								circuitManagers.values().stream().map(Pair::getValue).forEach(this::updateCanvasSize);
								
								for(MenuItem freq : frequenciesMenu.getItems()) {
									if(freq.getText().startsWith(String.valueOf(circuitFile.clockSpeed))) {
										((RadioMenuItem)freq).setSelected(true);
										break;
									}
								}
								
								if(circuitFile.globalBitSize >= 1 && circuitFile.globalBitSize <= 32) {
									bitSizeSelect.getSelectionModel().select((Integer)circuitFile.globalBitSize);
								}
								
								latch.countDown();
							});
						});
						tasksThread.setName("LoadCircuits Tasks Thread");
						tasksThread.start();
						
						latch.await();
						
						saveFile = lastSaveFile;
					} catch(Exception exc) {
						clearCircuits();
						excThrown = exc;
					} finally {
						if(circuitManagers.size() == 0) {
							createCircuit("New circuit");
						}
						
						editHistory.enable();
						loadingFile = false;
						runFxSync(() -> {
							updateTitle();
							refreshCircuitsTab();
							
							dialog.setResult(ButtonType.OK);
							dialog.close();
							
							loadFileLatch.countDown();
						});
					}
				});
				loadThread.setName("LoadCircuits");
				loadThread.start();
				
				if(openWindow) {
					dialog.showAndWait();
				}
			} else {
				loadFileLatch.countDown();
			}
		});
		
		try {
			loadFileLatch.await();
		} catch(Exception exc) {
			// don't care
		}
		
		saveConfFile();
		
		if(excThrown != null) {
			Exception toThrow = excThrown;
			excThrown = null;
			throw toThrow;
		}
	}
	
	private void loadLibrary(File file) {
		try(JarFile jarFile = new JarFile(file)) {
			Enumeration<JarEntry> e = jarFile.entries();
			
			URLClassLoader cl = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			
			while(e.hasMoreElements()) {
				JarEntry je = e.nextElement();
				if(je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				
				try {
					String className = je.getName().substring(0, je.getName().length() - 6);
					className = className.replace('/', '.');
					Class<?> c = cl.loadClass(className);
					
					if(ComponentPeer.class.isAssignableFrom(c)) {
						@SuppressWarnings("unchecked")
						Class<? extends ComponentPeer<?>> cc = (Class<? extends ComponentPeer<?>>)c;
						componentManager.register(cc);
					}
				} catch(Throwable t) {
					t.printStackTrace();
					
					Alert alert = new Alert(AlertType.ERROR);
					alert.initOwner(stage);
					alert.initModality(Modality.WINDOW_MODAL);
					alert.setTitle("Error loading class");
					alert.setHeaderText("Error loading class");
					alert.setContentText("Error when loading class: " + t.getMessage());
					alert.getButtonTypes().add(ButtonType.CANCEL);
					Optional<ButtonType> buttonType = alert.showAndWait();
					if(buttonType.isPresent() && buttonType.get() == ButtonType.CANCEL) {
						break;
					}
				}
			}
			
			if(libraryPaths == null) {
				libraryPaths = new ArrayList<>();
			}
			
			Path currDir = Paths.get(System.getProperty("user.dir"));
			Path libraryDir = file.toPath().toAbsolutePath();
			
			Path relativePath = currDir.relativize(libraryDir);
			String relative = relativePath.toString();
			if(!libraryPaths.contains(relative)) {
				libraryPaths.add(relative);
			}
			
			refreshComponentsTabs.run();
		} catch(Exception exc) {
			exc.printStackTrace();
			
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(stage);
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("Error opening library");
			alert.setHeaderText("Error opening library");
			alert.setContentText("Error when opening library: " + exc.getMessage());
			alert.showAndWait();
		}
	}
	
	/**
	 * Get the last saved file.
	 *
	 * @return The last saved file selected in loadCircuits or saveCircuits.
	 */
	public File getSaveFile() {
		return saveFile;
	}
	
	private void saveCircuitsInternal() {
		try {
			saveCircuits();
		} catch(Exception exc) {
			exc.printStackTrace();
			
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(stage);
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("Error");
			alert.setHeaderText("Error saving circuit.");
			alert.setContentText("Error when saving the circuit: " + exc.getMessage());
			alert.showAndWait();
		}
	}
	
	/**
	 * Save the circuits to the saved file. If none, behaves just like saveCircuits(null).
	 */
	public void saveCircuits() throws Exception {
		saveCircuits(saveFile);
	}
	
	/**
	 * Save the circuits to the specified File. This File is saved for reuse with saveCircuits().
	 * If null is passed in, a FileChooser dialog pops up to select a file.
	 *
	 * @param file The File instance to save the circuits to.
	 */
	public void saveCircuits(File file) throws Exception {
		runFxSync(() -> {
			File f = file;
			
			if(f == null) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Choose sim file");
				fileChooser.setInitialDirectory(lastSaveFile == null ? new File(System.getProperty("user.dir"))
				                                                     : lastSaveFile.getParentFile());
				fileChooser.setInitialFileName("My circuit.sim");
				fileChooser.getExtensionFilters().add(new ExtensionFilter("Circuit Sim file", "*.sim"));
				f = fileChooser.showSaveDialog(stage);
			}
			
			if(f != null) {
				lastSaveFile = f;
				
				List<CircuitInfo> circuits = new ArrayList<>();
				
				canvasTabPane.getTabs().forEach(tab -> {
					String name = tab.getText();
					
					CircuitManager manager = circuitManagers.get(name).getValue();
					
					List<ComponentInfo> components =
						manager.getCircuitBoard()
						       .getComponents().stream()
						       .map(component -> new ComponentInfo(component.getClass().getName(),
						                                           component.getX(),
						                                           component.getY(),
						                                           component.getProperties()))
						       .sorted(Comparator.comparingInt(Object::hashCode))
						       .collect(Collectors.toList());
					List<WireInfo> wires = manager.getCircuitBoard()
					                              .getLinks().stream()
					                              .flatMap(linkWires -> linkWires.getWires().stream())
					                              .map(wire -> new WireInfo(wire.getX(),
					                                                        wire.getY(),
					                                                        wire.getLength(),
					                                                        wire.isHorizontal()))
					                              .sorted(Comparator.comparingInt(Object::hashCode))
					                              .collect(Collectors.toList());
					
					circuits.add(new CircuitInfo(name, components, wires));
				});
				
				try {
					FileFormat.save(f, new CircuitFile(bitSizeSelect.getSelectionModel().getSelectedItem(),
					                                   getCurrentClockSpeed(),
					                                   libraryPaths,
					                                   circuits));
					savedEditStackSize = editHistory.editStackSize();
					saveFile = f;
					
					updateTitle();
				} catch(Exception exc) {
					exc.printStackTrace();
					excThrown = exc;
				}
			}
		});
		
		saveConfFile();
		
		if(excThrown != null) {
			Exception toThrow = excThrown;
			excThrown = null;
			throw toThrow;
		}
	}
	
	/**
	 * Create a Circuit, adding a new tab at the end and a button in the Circuits components tab.
	 *
	 * @param name The name of the circuit and tab.
	 */
	public void createCircuit(String name) {
		if(name == null || name.isEmpty()) {
			throw new NullPointerException("Name cannot be null or empty");
		}
		
		runFxSync(() -> {
			String n = name;
			
			Canvas canvas = new Canvas(800, 600);
			canvas.setFocusTraversable(true);
			
			ScrollPane canvasScrollPane = new ScrollPane(canvas);
			canvasScrollPane.setFocusTraversable(true);
			
			CircuitManager circuitManager = new CircuitManager(n, this, canvasScrollPane, simulator);
			circuitManager.getCircuit().addListener(this::circuitModified);
			
			canvas.addEventHandler(MouseEvent.ANY, e -> canvas.requestFocus());
			canvas.addEventHandler(MouseEvent.MOUSE_MOVED, circuitManager::mouseMoved);
			canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
				circuitManager.mouseDragged(e);
				updateCanvasSize(circuitManager);
			});
			canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
				circuitManager.mousePressed(e);
				e.consume();
			});
			canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, circuitManager::mouseReleased);
			canvas.addEventHandler(ScrollEvent.SCROLL, circuitManager::mouseWheelScrolled);
			canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, circuitManager::mouseEntered);
			canvas.addEventHandler(MouseEvent.MOUSE_EXITED, circuitManager::mouseExited);
			canvas.addEventHandler(KeyEvent.KEY_PRESSED, circuitManager::keyPressed);
			canvas.addEventHandler(KeyEvent.KEY_TYPED, circuitManager::keyTyped);
			canvas.addEventHandler(KeyEvent.KEY_RELEASED, circuitManager::keyReleased);
			canvas.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if(newValue) {
					circuitManager.focusGained();
				} else {
					circuitManager.focusLost();
				}
			});
			
			canvasScrollPane.widthProperty().addListener(
				(observable, oldValue, newValue) -> this.updateCanvasSize(circuitManager));
			canvasScrollPane.heightProperty().addListener(
				(observable, oldValue, newValue) -> this.updateCanvasSize(circuitManager));
			
			String originalName = n;
			for(int count = 0; getCircuitManager(originalName) != null; count++) {
				originalName = n;
				if(count > 0) {
					originalName += count;
				}
			}
			
			n = originalName;
			
			circuitManager.setName(n);
			
			Tab canvasTab = new Tab(n, canvasScrollPane);
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
							clearSelection();
						}
						break;
					} catch(Exception exc) {
						exc.printStackTrace();
						
						Alert alert = new Alert(AlertType.ERROR);
						alert.initOwner(stage);
						alert.initModality(Modality.WINDOW_MODAL);
						alert.setTitle("Duplicate name");
						alert.setHeaderText("Duplicate name");
						alert.setContentText("Name already exists, please choose a new name.");
						alert.showAndWait();
					}
				}
			});
			MenuItem viewTopLevelState = new MenuItem("View top-level state");
			viewTopLevelState.setOnAction(
				event -> circuitManager.getCircuitBoard().setCurrentState(
					circuitManager.getCircuit().getTopLevelState()));
			
			MenuItem moveLeft = new MenuItem("Move left");
			moveLeft.setOnAction(event -> {
				ObservableList<Tab> tabs = canvasTabPane.getTabs();
				int idx = tabs.indexOf(canvasTab);
				if(idx > 0) {
					tabs.remove(idx);
					tabs.add(idx - 1, canvasTab);
					canvasTabPane.getSelectionModel().select(canvasTab);
					
					editHistory.addAction(EditAction.MOVE_CIRCUIT, circuitManager, tabs, canvasTab, idx, idx - 1);
					
					refreshCircuitsTab();
				}
			});
			
			MenuItem moveRight = new MenuItem("Move right");
			moveRight.setOnAction(event -> {
				ObservableList<Tab> tabs = canvasTabPane.getTabs();
				int idx = tabs.indexOf(canvasTab);
				if(idx >= 0 && idx < tabs.size() - 1) {
					tabs.remove(idx);
					tabs.add(idx + 1, canvasTab);
					canvasTabPane.getSelectionModel().select(canvasTab);
					
					editHistory.addAction(EditAction.MOVE_CIRCUIT, circuitManager, tabs, canvasTab, idx, idx + 1);
					
					refreshCircuitsTab();
				}
			});
			
			canvasTab.setContextMenu(new ContextMenu(rename, viewTopLevelState, moveLeft, moveRight));
			canvasTab.setOnCloseRequest(event -> {
				if(!confirmAndDeleteCircuit(circuitManager, false)) {
					event.consume();
				}
			});
			
			circuitManagers.put(canvasTab.getText(), new Pair<>(createSubcircuitLauncherInfo(n), circuitManager));
			canvasTabPane.getTabs().add(canvasTab);
			
			refreshCircuitsTab();
			
			editHistory.addAction(EditAction.CREATE_CIRCUIT, circuitManager, canvasTab,
			                      canvasTabPane.getTabs().size() - 1);
			
			canvas.requestFocus();
		});
	}
	
	/**
	 * Do not call this directly, called automatically
	 *
	 * @param stage The Stage instance to create this Circuit Simulator in
	 */
	@Override
	public void start(Stage stage) {
		if(this.stage != null) {
			throw new IllegalStateException("Already started");
		}
		
		this.stage = stage;
		
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/Icon.png")));
		
		bitSizeSelect = new ComboBox<>();
		for(int i = 1; i <= 32; i++) {
			bitSizeSelect.getItems().add(i);
		}
		bitSizeSelect.setValue(1);
		bitSizeSelect.getSelectionModel()
		             .selectedItemProperty()
		             .addListener((observable, oldValue, newValue) -> modifiedSelection(selectedComponent));
		
		scaleFactorSelect = new ComboBox<>();
		for(int i = 3; i <= 10; i++) {
			scaleFactorSelect.getItems().add(i / 10.0);
		}
		for(int i = 1; i <= 16; i++) {
			scaleFactorSelect.getItems().add(1 + i * 0.25);
		}
		scaleFactorSelect.setValue(1.0);
		scaleFactorSelect.getSelectionModel()
		                 .selectedItemProperty()
		                 .addListener((observable, oldValue, newValue) -> {
			                 needsRepaint = true;
			                 for(Pair<ComponentLauncherInfo, CircuitManager> pair :
				                 circuitManagers.values()) {
				                 updateCanvasSize(pair.getValue());
			                 }
		                 });
		
		buttonTabPane = new TabPane();
		buttonTabPane.setSide(Side.TOP);
		
		propertiesTable = new GridPane();
		
		componentLabel = new Label();
		componentLabel.setFont(GuiUtils.getFont(16));
		
		canvasTabPane = new TabPane();
		canvasTabPane.setPrefWidth(800);
		canvasTabPane.setPrefHeight(600);
		canvasTabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		canvasTabPane.widthProperty().addListener((observable, oldValue, newValue) -> needsRepaint = true);
		canvasTabPane.heightProperty().addListener((observable, oldValue, newValue) -> needsRepaint = true);
		canvasTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			CircuitManager oldManager = oldValue == null || !circuitManagers.containsKey(oldValue.getText())
			                            ? null : circuitManagers.get(oldValue.getText()).getValue();
			CircuitManager newManager = newValue == null || !circuitManagers.containsKey(newValue.getText())
			                            ? null : circuitManagers.get(newValue.getText()).getValue();
			if(oldManager != null && newManager != null) {
				newManager.setLastMousePosition(oldManager.getLastMousePosition());
				modifiedSelection(selectedComponent);
				
				needsRepaint = true;
			}
		});
		
		buttonsToggleGroup = new ToggleGroup();
		Map<String, Tab> buttonTabs = new HashMap<>();
		
		refreshComponentsTabs = () -> {
			buttonTabPane.getTabs().clear();
			buttonTabs.clear();
			
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
			
			circuitButtonsTab = null;
			refreshCircuitsTab();
		};
		
		refreshComponentsTabs.run();
		
		editHistory.disable();
		createCircuit("New circuit");
		editHistory.enable();
		
		// FILE Menu
		MenuItem newInstance = new MenuItem("New");
		newInstance.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
		newInstance.setOnAction(event -> {
			saveConfFile();
			new CircuitSim(true);
		});
		
		MenuItem clear = new MenuItem("Clear");
		clear.setOnAction(event -> {
			if(!checkUnsavedChanges()) {
				clearCircuits();
				editHistory.disable();
				createCircuit("New circuit");
				editHistory.enable();
			}
		});
		
		MenuItem load = new MenuItem("Load");
		load.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
		load.setOnAction(event -> {
			if(checkUnsavedChanges()) {
				return;
			}
			
			loadCircuitsInternal(null);
		});
		
		MenuItem save = new MenuItem("Save");
		save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		save.setOnAction(event -> saveCircuitsInternal());
		
		MenuItem saveAs = new MenuItem("Save as");
		saveAs.setAccelerator(
			new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		saveAs.setOnAction(event -> {
			lastSaveFile = saveFile;
			
			saveFile = null;
			saveCircuitsInternal();
			
			if(saveFile == null) {
				saveFile = lastSaveFile;
			}
			
			updateTitle();
		});
		
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(event -> {
			if(!checkUnsavedChanges()) {
				closeWindow();
			}
		});
		
		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(newInstance, clear, new SeparatorMenuItem(),
		                           load, save, saveAs, new SeparatorMenuItem(),
		                           exit);
		
		// EDIT Menu
		undo = new MenuItem("Undo");
		undo.setDisable(true);
		undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
		undo.setOnAction(event -> {
			CircuitManager manager = getCurrentCircuit();
			if(manager != null) {
				manager.setSelectedElements(Collections.emptySet());
			}
			
			manager = editHistory.undo();
			if(manager != null) {
				manager.setSelectedElements(Collections.emptySet());
				switchToCircuit(manager.getCircuit(), null);
			}
		});
		
		editHistory.addListener((action, manager, params) -> undo.setDisable(editHistory.editStackSize() == 0));
		
		redo = new MenuItem("Redo");
		redo.setDisable(true);
		redo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN));
		redo.setOnAction(event -> {
			CircuitManager manager = editHistory.redo();
			if(manager != null) {
				manager.getSelectedElements().clear();
				switchToCircuit(manager.getCircuit(), null);
			}
		});
		
		editHistory.addListener((action, manager, params) -> redo.setDisable(editHistory.redoStackSize() == 0));
		
		MenuItem copy = new MenuItem("Copy");
		copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
		copy.setOnAction(event -> copySelectedComponents());
		
		MenuItem cut = new MenuItem("Cut");
		cut.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
		cut.setOnAction(event -> cutSelectedComponents());
		
		MenuItem paste = new MenuItem("Paste");
		paste.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
		paste.setOnAction(event -> pasteFromClipboard());
		
		MenuItem selectAll = new MenuItem("Select All");
		selectAll.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN));
		selectAll.setOnAction(event -> {
			CircuitManager manager = getCurrentCircuit();
			if(manager != null) {
				manager.setSelectedElements(
					Stream.concat(manager.getCircuitBoard().getComponents().stream(),
					              manager.getCircuitBoard().getLinks()
					                     .stream().flatMap(link -> link.getWires().stream()))
					      .collect(Collectors.toSet()));
				needsRepaint = true;
			}
		});
		
		Menu editMenu = new Menu("Edit");
		editMenu.getItems().addAll(undo, redo, new SeparatorMenuItem(),
		                           copy, cut, paste, new SeparatorMenuItem(),
		                           selectAll);
		
		// COMPONENTS Menu
		MenuItem loadLibrary = new MenuItem("Load library");
		loadLibrary.setOnAction(event -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choose library file");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Java Archive", "*.jar"));
			File file = fileChooser.showOpenDialog(stage);
			if(file != null) {
				loadLibrary(file);
			}
		});
		
		Menu componentsMenu = new Menu("Components");
		componentsMenu.getItems().addAll(loadLibrary);
		
		// CIRCUITS Menu
		MenuItem newCircuit = new MenuItem("New circuit");
		newCircuit.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN));
		newCircuit.setOnAction(event -> createCircuit("New circuit"));
		
		MenuItem deleteCircuit = new MenuItem("Delete circuit");
		deleteCircuit.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN));
		deleteCircuit.setOnAction(event -> {
			CircuitManager currentCircuit = getCurrentCircuit();
			if(currentCircuit != null) {
				confirmAndDeleteCircuit(currentCircuit, true);
			}
		});
		
		Menu circuitsMenu = new Menu("Circuits");
		circuitsMenu.getItems().addAll(newCircuit, deleteCircuit);
		
		// SIMULATION Menu
		MenuItem stepSimulation = new MenuItem("Step Simulation");
		stepSimulation.setDisable(true);
		stepSimulation.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));
		stepSimulation.setOnAction(event -> {
			try {
				simulator.step();
			} catch(Exception exc) {
				setLastException(exc);
			} finally {
				needsRepaint = true;
			}
		});
		
		simulationEnabled = new CheckMenuItem("Simulation Enabled");
		simulationEnabled.setSelected(true);
		simulationEnabled.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
		simulationEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
			runSim();
			
			stepSimulation.setDisable(newValue);
			clockEnabled.setDisable(!newValue);
			clockEnabled.setSelected(false);
		});
		
		MenuItem reset = new MenuItem("Reset simulation");
		reset.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
		reset.setOnAction(event -> {
			Clock.reset(simulator);
			clockEnabled.setSelected(false);
			simulator.reset();
			
			for(Pair<ComponentLauncherInfo, CircuitManager> pair : circuitManagers.values()) {
				pair.getValue().getCircuitBoard().setCurrentState(pair.getValue().getCircuit().getTopLevelState());
			}
			
			runSim();
		});
		
		MenuItem tickClock = new MenuItem("Tick clock");
		tickClock.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN));
		tickClock.setOnAction(event -> Clock.tick(simulator));
		
		clockEnabled = new CheckMenuItem("Clock Enabled");
		clockEnabled.setAccelerator(new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN));
		clockEnabled.selectedProperty().addListener((observable, oldValue, newValue) -> {
			tickClock.setDisable(newValue);
			
			if(newValue) {
				Clock.startClock(simulator, getCurrentClockSpeed());
			} else {
				Clock.stopClock(simulator);
			}
		});
		
		frequenciesMenu = new Menu("Frequency");
		ToggleGroup freqToggleGroup = new ToggleGroup();
		for(int i = 0; i <= 14; i++) {
			RadioMenuItem freq = new RadioMenuItem((1 << i) + " Hz");
			freq.setToggleGroup(freqToggleGroup);
			freq.setSelected(i == 0);
			final int j = i;
			freq.setOnAction(event -> {
				if(Clock.isRunning(simulator)) {
					Clock.startClock(simulator, 1 << j);
				}
			});
			frequenciesMenu.getItems().add(freq);
		}
		
		Menu simulationMenu = new Menu("Simulation");
		simulationMenu.getItems().addAll(simulationEnabled, stepSimulation, reset, new SeparatorMenuItem(),
		                                 clockEnabled, tickClock, frequenciesMenu);
		
		// HELP Menu
		Menu helpMenu = new Menu("Help");
		help = new MenuItem("Help");
		help.setOnAction(event -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(stage);
			alert.initModality(Modality.NONE);
			alert.setTitle("Help");
			alert.setHeaderText("CircuitSim v" + VERSION + ", created by Roi Atalla © 2018");
			
			String msg = "";
			msg += "• Right clicking works! Try it on components, subcircuits, and circuit tabs.\n\n";
			msg += "• Double clicking on a subcircuit will automatically go to its circuit tab as a child state.\n\n";
			msg += "• Holding Shift will enable Click Mode which will click through to components.\n\n";
			msg += "• Holding Shift after dragging a new wire will delete existing wires.\n\n";
			msg += "• Holding Ctrl while dragging a new wire allows release of the mouse, and continuing the wire on "
				       + "click.\n\n";
			msg += "• Holding Ctrl while selecting components and wires will include them in the selection group.\n\n";
			msg += "• Holding Ctrl while dragging components will disable preserving connections.\n\n";
			msg += "• Holding Ctrl while placing a new component will keep the component selected.\n\n";
			
			alert.setContentText(msg);
			alert.show();
			alert.setResizable(true);
			alert.setWidth(600);
			alert.setHeight(440);
		});
		MenuItem checkUpdate = new MenuItem("Check for update");
		checkUpdate.setOnAction(event -> checkForUpdate(true));
		MenuItem about = new MenuItem("About");
		about.setOnAction(event -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.initOwner(stage);
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("About");
			alert.setHeaderText("CircuitSim v" + VERSION + ", created by Roi Atalla © 2018");
			alert.setContentText("Third party tools:\n• GSON by Google");
			alert.show();
		});
		helpMenu.getItems().addAll(help, checkUpdate, about);
		
		MenuBar menuBar = new MenuBar(fileMenu, editMenu, componentsMenu, circuitsMenu, simulationMenu, helpMenu);
		menuBar.setUseSystemMenuBar(true);
		
		ScrollPane propertiesScrollPane = new ScrollPane(propertiesTable);
		propertiesScrollPane.setFitToWidth(true);
		
		VBox propertiesBox = new VBox(componentLabel, propertiesScrollPane);
		propertiesBox.setAlignment(Pos.TOP_CENTER);
		VBox.setVgrow(propertiesScrollPane, Priority.ALWAYS);
		
		SplitPane leftPaneSplit = new SplitPane(buttonTabPane, propertiesBox);
		leftPaneSplit.setOrientation(Orientation.VERTICAL);
		leftPaneSplit.setPrefWidth(500);
		leftPaneSplit.setMinWidth(150);
		
		SplitPane.setResizableWithParent(buttonTabPane, Boolean.FALSE);
		
		fpsLabel = new Label();
		fpsLabel.setMinWidth(100);
		fpsLabel.setFont(GuiUtils.getFont(13));
		fpsLabel.setAlignment(Pos.CENTER_LEFT);
		
		clockLabel = new Label();
		clockLabel.setMinWidth(100);
		clockLabel.setAlignment(Pos.CENTER_LEFT);
		clockLabel.setFont(GuiUtils.getFont(13));
		
		messageLabel = new Label();
		messageLabel.setTextFill(Color.RED);
		messageLabel.setFont(GuiUtils.getFont(20, true));
		
		Pane blank1 = new Pane();
		HBox.setHgrow(blank1, Priority.ALWAYS);
		
		Pane blank2 = new Pane();
		HBox.setHgrow(blank2, Priority.ALWAYS);
		
		HBox statusBar = new HBox(fpsLabel, clockLabel, blank1, messageLabel, blank2);
		VBox canvasTabBox = new VBox(canvasTabPane, statusBar);
		VBox.setVgrow(canvasTabPane, Priority.ALWAYS);
		
		SplitPane canvasPropsSplit = new SplitPane(leftPaneSplit, canvasTabBox);
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
			button.setOnAction(event -> {
				if(button.isSelected()) {
					modifiedSelection(info);
				} else {
					modifiedSelection(null);
				}
			});
			return button;
		};
		
		ToggleButton inputPinButton = createToolbarButton.apply(new Pair<>("Wiring", "Input Pin"));
		ToggleButton outputPinButton = createToolbarButton.apply(new Pair<>("Wiring", "Output Pin"));
		ToggleButton andButton = createToolbarButton.apply(new Pair<>("Gates", "AND"));
		ToggleButton orButton = createToolbarButton.apply(new Pair<>("Gates", "OR"));
		ToggleButton notButton = createToolbarButton.apply(new Pair<>("Gates", "NOT"));
		ToggleButton xorButton = createToolbarButton.apply(new Pair<>("Gates", "XOR"));
		ToggleButton tunnelButton = createToolbarButton.apply(new Pair<>("Wiring", "Tunnel"));
		ToggleButton textButton = createToolbarButton.apply(new Pair<>("Misc", "Text"));
		
		clickMode = new ToggleButton("Click Mode (Shift)");
		clickMode.setTooltip(new Tooltip("Clicking will sticky this mode"));
		clickMode.setOnAction(event -> clickedDirectly = clickMode.isSelected());
		clickMode.selectedProperty().addListener(
			(observable, oldValue, newValue) -> scene.setCursor(newValue ? Cursor.HAND : Cursor.DEFAULT));
		
		Pane blank = new Pane();
		HBox.setHgrow(blank, Priority.ALWAYS);
		
		toolBar.getItems().addAll(clickMode, new Separator(Orientation.VERTICAL),
		                          inputPinButton, outputPinButton, andButton,
		                          orButton, notButton, xorButton, tunnelButton, textButton,
		                          new Separator(Orientation.VERTICAL),
		                          new Label("Global bit size:"), bitSizeSelect,
		                          blank,
		                          new Label("Scale:"), scaleFactorSelect);
		
		VBox.setVgrow(canvasPropsSplit, Priority.ALWAYS);
		scene = new Scene(new VBox(menuBar, toolBar, canvasPropsSplit));
		scene.setCursor(Cursor.DEFAULT);
		
		updateTitle();
		stage.setScene(scene);
		stage.sizeToScene();
		stage.centerOnScreen();
		
		if(openWindow) {
			showWindow();
			
			loadConfFile();
			saveConfFile();
			
			Parameters parameters = getParameters();
			if(parameters != null && !parameters.getRaw().isEmpty()) {
				List<String> args = parameters.getRaw();
				loadCircuitsInternal(new File(args.get(0)));
				
				if(args.size() > 1) {
					for(int i = 1; i < args.size(); i++) {
						new CircuitSim(true).loadCircuitsInternal(new File(args.get(i)));
					}
				}
			}
			
			if(mainCalled && versionChecked.compareAndSet(false, true)) {
				checkForUpdate(false);
			}
		}
	}
	
	private AnimationTimer currentTimer;
	
	public void showWindow() {
		runFxSync(() -> {
			if(stage.isShowing()) return;
			
			stage.show();
			stage.setOnCloseRequest(event -> {
				if(checkUnsavedChanges()) {
					event.consume();
				} else {
					saveConfFile();
				}
			});
			
			(currentTimer = new AnimationTimer() {
				private long lastRepaint;
				private int lastFrameCount;
				private int frameCount;
				
				@Override
				public void handle(long now) {
					if(now - lastRepaint >= 1e9) {
						lastFrameCount = frameCount;
						frameCount = 0;
						lastRepaint = now;
						
						fpsLabel.setText("FPS: " + lastFrameCount);
						clockLabel.setText(Clock.isRunning(simulator)
						                   ? "Clock: " + (Clock.getLastTickCount(simulator) >> 1) + " Hz"
						                   : "");
					}
					
					frameCount++;
					
					runSim();
					
					CircuitManager manager = getCurrentCircuit();
					if(manager != null) {
						if((needsRepaint || manager.needsRepaint())) {
							needsRepaint = false;
							manager.paint();
						}
						
						if(!loadingFile) {
							String message = getCurrentError();
							
							if(!message.isEmpty() && Clock.isRunning(simulator)) {
								clockEnabled.setSelected(false);
							}
							
							if(!messageLabel.getText().equals(message)) {
								messageLabel.setText(message);
							}
						} else if(!messageLabel.getText().isEmpty()) {
							messageLabel.setText("");
						}
					}
				}
			}).start();
		});
	}
	
	public void closeWindow() {
		runFxSync(() -> {
			stage.close();
			if(currentTimer != null) {
				currentTimer.stop();
				currentTimer = null;
			}
		});
	}
}
