package com.ra4king.circuitsimulator.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.gui.peers.ClockPeer;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.ShortCircuitException;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.components.Pin;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Roi Atalla
 */
public class CircuitManager {
	private enum SelectingState {
		IDLE,
		HIGHLIGHT_DRAGGED,
		ELEMENT_SELECTED,
		CONNECTION_SELECTED,
		ELEMENT_DRAGGED,
		CONNECTION_DRAGGED,
		PLACING_COMPONENT,
	}
	
	private SelectingState currentState = SelectingState.IDLE;
	
	private final CircuitSimulator simulatorWindow;
	private final ScrollPane canvasScrollPane;
	private final CircuitBoard circuitBoard;
	
	private ContextMenu menu;
	
	private Point2D lastMousePosition = new Point2D(0, 0);
	private Point2D lastMousePressed = new Point2D(0, 0);
	
	private boolean isMouseInsideCanvas;
	private boolean isDraggedHorizontally;
	private boolean ctrlDown;
	
	private Circuit dummyCircuit = new Circuit(new Simulator());
	private ComponentPeer<?> potentialComponent;
	private ComponentCreator componentCreator;
	private Properties properties;
	
	private Connection startConnection, endConnection;
	
	private Map<GuiElement, Point2D> selectedElementsMap = new HashMap<>();
	
	private Exception lastException;
	private long lastErrorTime;
	private static final long SHOW_ERROR_DURATION = 3000;
	
	private boolean needsRepaint;
	
	public CircuitManager(CircuitSimulator simulatorWindow, ScrollPane canvasScrollPane, Simulator simulator) {
		this.simulatorWindow = simulatorWindow;
		this.canvasScrollPane = canvasScrollPane;
		circuitBoard = new CircuitBoard(this, simulator, simulatorWindow.getEditHistory());
		
		getCanvas().setOnContextMenuRequested(event -> {
			menu = new ContextMenu();
			
			MenuItem delete = new MenuItem("Delete");
			delete.setOnAction(event1 -> {
				mayThrow(() -> circuitBoard.removeElements(selectedElementsMap.keySet()));
				setSelectedElements(Collections.emptySet());
				reset();
			});
			
			if(selectedElementsMap.size() == 0) {
				Optional<ComponentPeer<?>> any = circuitBoard.getComponents().stream().filter(
						component -> component.containsScreenCoord((int)event.getX(), (int)event.getY())).findAny();
				if(any.isPresent()) {
					menu.getItems().add(delete);
					menu.getItems().addAll(any.get().getContextMenuItems(this));
				}
			} else if(selectedElementsMap.size() == 1) {
				menu.getItems().add(delete);
				menu.getItems().addAll(selectedElementsMap.keySet().iterator().next().getContextMenuItems(this));
			} else {
				menu.getItems().add(delete);
			}
			
			if(menu.getItems().size() > 0) {
				menu.show(getCanvas(), event.getScreenX(), event.getScreenY());
			}
		});
	}
	
	private void reset() {
		currentState = SelectingState.IDLE;
		
		setSelectedElements(Collections.emptySet());
		simulatorWindow.clearSelection();
		dummyCircuit.clearComponents();
		potentialComponent = null;
		isDraggedHorizontally = false;
		startConnection = null;
		endConnection = null;
		
		needsRepaint = true;
	}
	
	public void destroy() {
		circuitBoard.destroy();
	}
	
	public CircuitSimulator getSimulatorWindow() {
		return simulatorWindow;
	}
	
	public ScrollPane getCanvasScrollPane() {
		return canvasScrollPane;
	}
	
	public Canvas getCanvas() {
		return (Canvas)canvasScrollPane.getContent();
	}
	
	public Circuit getCircuit() {
		return circuitBoard.getCircuit();
	}
	
	public CircuitBoard getCircuitBoard() {
		return circuitBoard;
	}
	
	public String getCurrentError() {
		if(lastException != null && SHOW_ERROR_DURATION < System.currentTimeMillis() - lastErrorTime) {
			lastException = null;
		}
		
		if(lastException == null && circuitBoard.getLastException() == null) {
			return "";
		}
		
		Exception exc = lastException == null ? circuitBoard.getLastException() : lastException;
		return exc instanceof ShortCircuitException ? "Short circuit detected" : exc.getMessage();
	}
	
	public Point2D getLastMousePosition() {
		return lastMousePosition;
	}
	
	public void setLastMousePosition(Point2D lastMousePosition) {
		this.lastMousePosition = lastMousePosition;
	}
	
	public Set<GuiElement> getSelectedElements() {
		return selectedElementsMap.keySet();
	}
	
	public void setSelectedElements(Set<GuiElement> elements) {
		mayThrow(circuitBoard::finalizeMove);
		selectedElementsMap = elements.stream().collect(
				Collectors.toMap(peer -> peer,
				                 peer -> new Point2D(peer.getX(),
				                                     peer.getY())));
		updateSelectedProperties();
	}
	
	public boolean needsRepaint() {
		return needsRepaint;
	}
	
	private Properties getCommonSelectedProperties() {
		return selectedElementsMap.keySet().stream()
		                          .filter(element -> element instanceof ComponentPeer<?>)
		                          .map(element -> ((ComponentPeer<?>)element).getProperties())
		                          .reduce(Properties::intersect).orElse(null);
	}
	
	private void updateSelectedProperties() {
		long componentCount = selectedElementsMap.keySet().stream()
		                                         .filter(element -> element instanceof ComponentPeer<?>)
		                                         .count();
		if(componentCount == 1) {
			Optional<? extends ComponentPeer<?>> peer = selectedElementsMap
					                                            .keySet().stream()
					                                            .filter(element -> element instanceof ComponentPeer<?>)
					                                            .map(element -> ((ComponentPeer<?>)element))
					                                            .findAny();
			peer.ifPresent(simulatorWindow::setProperties);
		} else {
			simulatorWindow.setProperties("Multiple selections", getCommonSelectedProperties());
		}
	}
	
	public void modifiedSelection(ComponentCreator componentCreator, Properties properties) {
		this.componentCreator = componentCreator;
		this.properties = properties;
		
		needsRepaint = true;
		
		if(currentState != SelectingState.IDLE && currentState != SelectingState.PLACING_COMPONENT) {
			reset();
		}
		
		if(componentCreator != null) {
			setSelectedElements(Collections.emptySet());
			
			currentState = SelectingState.PLACING_COMPONENT;
			
			potentialComponent = componentCreator.createComponent(properties,
			                                                      GuiUtils.getCircuitCoord(lastMousePosition.getX()),
			                                                      GuiUtils.getCircuitCoord(lastMousePosition.getY()));
			mayThrow(() -> dummyCircuit.addComponent(potentialComponent.getComponent()));
			potentialComponent.setX(potentialComponent.getX() - potentialComponent.getWidth() / 2);
			potentialComponent.setY(potentialComponent.getY() - potentialComponent.getHeight() / 2);
			simulatorWindow.setProperties(potentialComponent);
			return;
		}
		
		if(properties != null && !properties.isEmpty() && !selectedElementsMap.isEmpty()) {
			Map<ComponentPeer<?>, ComponentPeer<?>> newComponents =
					selectedElementsMap
							.keySet().stream()
							.filter(element -> element instanceof ComponentPeer<?>)
							.map(element -> (ComponentPeer<?>)element)
							.collect(Collectors.toMap(
									component -> component,
									component -> (ComponentPeer<?>)ComponentManager
											                               .forClass(component.getClass())
											                               .createComponent(
													                               component.getProperties()
													                                        .mergeIfExists(properties),
													                               component.getX(),
													                               component.getY())));
			
			simulatorWindow.getEditHistory().beginGroup();
			newComponents.forEach((oldComponent, newComponent) ->
					                      mayThrow(() -> circuitBoard.updateComponent(oldComponent, newComponent)));
			simulatorWindow.getEditHistory().endGroup();
			
			setSelectedElements(Stream.concat(
					selectedElementsMap.keySet().stream().filter(element -> !(element instanceof ComponentPeer<?>)),
					newComponents.values().stream())
			                          .collect(Collectors.toSet()));
			return;
		}
		
		setSelectedElements(Collections.emptySet());
	}
	
	public void paint() {
		needsRepaint = false;
		
		GraphicsContext graphics = getCanvas().getGraphicsContext2D();
		
		graphics.save();
		
		graphics.setFont(Font.font("monospace", 13));
		
		graphics.save();
		graphics.setFill(Color.LIGHTGRAY);
		graphics.fillRect(0, 0, getCanvas().getWidth(), getCanvas().getHeight());
		
		graphics.setFill(Color.BLACK);
		for(int i = 0; i < getCanvas().getWidth(); i += GuiUtils.BLOCK_SIZE) {
			for(int j = 0; j < getCanvas().getHeight(); j += GuiUtils.BLOCK_SIZE) {
				graphics.fillRect(i, j, 1, 1);
			}
		}
		
		graphics.restore();
		
		circuitBoard.paint(graphics);
		
		switch(currentState) {
			case IDLE:
			case CONNECTION_SELECTED:
				if(startConnection != null) {
					graphics.save();
					
					graphics.setLineWidth(2);
					graphics.setStroke(Color.GREEN);
					graphics.strokeOval(startConnection.getScreenX() - 2, startConnection.getScreenY() - 2, 10, 10);
					
					if(endConnection != null) {
						graphics.strokeOval(endConnection.getScreenX() - 2, endConnection.getScreenY() - 2, 10, 10);
					}
					
					if(startConnection instanceof PortConnection) {
						PortConnection portConnection = (PortConnection)startConnection;
						String name = portConnection.getName();
						if(!name.isEmpty()) {
							Text text = new Text(name);
							text.setFont(graphics.getFont());
							Bounds bounds = text.getLayoutBounds();
							
							double x = startConnection.getScreenX() - bounds.getWidth() / 2 - 3;
							double y = startConnection.getScreenY() + 30;
							double width = bounds.getWidth() + 6;
							double height = bounds.getHeight() + 3;
							
							graphics.setLineWidth(1);
							graphics.setStroke(Color.BLACK);
							graphics.setFill(Color.ORANGE.brighter());
							graphics.fillRect(x, y, width, height);
							graphics.strokeRect(x, y, width, height);
							
							graphics.strokeText(name, x + 3, y + height - 5);
						}
					}
					
					graphics.restore();
				}
				break;
			case CONNECTION_DRAGGED: {
				graphics.save();
				
				graphics.setLineWidth(2);
				graphics.setStroke(Color.GREEN);
				graphics.strokeOval(startConnection.getScreenX() - 2, startConnection.getScreenY() - 2, 10, 10);
				
				if(endConnection != null) {
					graphics.strokeOval(endConnection.getScreenX() - 2, endConnection.getScreenY() - 2, 10, 10);
				}
				
				int startX = startConnection.getScreenX() + startConnection.getScreenWidth() / 2;
				int startY = startConnection.getScreenY() + startConnection.getScreenHeight() / 2;
				int pointX = GuiUtils.getScreenCircuitCoord(lastMousePosition.getX());
				int pointY = GuiUtils.getScreenCircuitCoord(lastMousePosition.getY());
				graphics.setStroke(Color.BLACK);
				if(isDraggedHorizontally) {
					graphics.strokeLine(startX, startY, pointX, startY);
					graphics.strokeLine(pointX, startY, pointX, pointY);
				} else {
					graphics.strokeLine(startX, startY, startX, pointY);
					graphics.strokeLine(startX, pointY, pointX, pointY);
				}
				
				graphics.restore();
				break;
			}
			case PLACING_COMPONENT: {
				if(potentialComponent != null && isMouseInsideCanvas) {
					graphics.save();
					potentialComponent.paint(graphics, dummyCircuit.getTopLevelState());
					graphics.restore();
					
					for(Connection connection : potentialComponent.getConnections()) {
						graphics.save();
						connection.paint(graphics, dummyCircuit.getTopLevelState());
						graphics.restore();
					}
				}
				break;
			}
			case HIGHLIGHT_DRAGGED: {
				double startX = lastMousePressed.getX() < lastMousePosition.getX()
				                ? lastMousePressed.getX()
				                : lastMousePosition.getX();
				double startY = lastMousePressed.getY() < lastMousePosition.getY()
				                ? lastMousePressed.getY()
				                : lastMousePosition.getY();
				double width = Math.abs(lastMousePosition.getX() - lastMousePressed.getX());
				double height = Math.abs(lastMousePosition.getY() - lastMousePressed.getY());
				
				graphics.setStroke(Color.GREEN.darker());
				graphics.strokeRect(startX, startY, width, height);
				break;
			}
		}
		
		for(GuiElement selectedElement : selectedElementsMap.keySet()) {
			graphics.setStroke(Color.RED);
			if(selectedElement instanceof Wire) {
				graphics.strokeRect(selectedElement.getScreenX() - 1, selectedElement.getScreenY() - 1,
				                    selectedElement.getScreenWidth() + 2, selectedElement.getScreenHeight() + 2);
			} else {
				GuiUtils.drawShape(graphics::strokeRect, selectedElement);
			}
		}
		
		graphics.restore();
	}
	
	interface ThrowableRunnable {
		void run() throws Exception;
	}
	
	boolean mayThrow(ThrowableRunnable runnable) {
		try {
			runnable.run();
			
			if(lastException != null && SHOW_ERROR_DURATION < System.currentTimeMillis() - lastErrorTime) {
				lastException = null;
			}
			
			return false;
		} catch(Exception exc) {
			exc.printStackTrace();
			lastException = exc;
			lastErrorTime = System.currentTimeMillis();
			return true;
		}
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
			case RIGHT: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.EAST);
				modifiedSelection(componentCreator, props);
				break;
			}
			case LEFT: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.WEST);
				modifiedSelection(componentCreator, props);
				break;
			}
			case UP: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.NORTH);
				modifiedSelection(componentCreator, props);
				break;
			}
			case DOWN: {
				Properties props = new Properties();
				props.setValue(Properties.DIRECTION, Direction.SOUTH);
				modifiedSelection(componentCreator, props);
				break;
			}
			case CONTROL:
				ctrlDown = true;
				break;
			case NUMPAD0:
			case NUMPAD1:
			case DIGIT0:
			case DIGIT1:
				int value = e.getText().charAt(0) - '0';
				
				GuiElement selectedElem;
				if(selectedElementsMap.size() == 1
						   && (selectedElem = selectedElementsMap.keySet().iterator().next()) instanceof PinPeer
						   && ((PinPeer)selectedElem).isInput()) {
					PinPeer selectedPin = (PinPeer)selectedElem;
					WireValue currentValue =
							new WireValue(circuitBoard.getCurrentState()
							                          .getLastPushedValue(
									                          selectedPin.getComponent().getPort(Pin.PORT)));
					
					for(int i = currentValue.getBitSize() - 1; i > 0; i--) {
						currentValue.setBit(i, currentValue.getBit(i - 1));
					}
					currentValue.setBit(0, value == 1 ? State.ONE : State.ZERO);
					selectedPin.getComponent().setValue(circuitBoard.getCurrentState(), currentValue);
					circuitBoard.runSim();
					needsRepaint = true;
				}
				break;
			case BACK_SPACE:
			case DELETE:
				mayThrow(() -> circuitBoard.removeElements(selectedElementsMap.keySet()));
			case ESCAPE:
				if(currentState == SelectingState.ELEMENT_DRAGGED) {
					mayThrow(() -> circuitBoard.moveElements(0, 0));
				}
				
				reset();
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) {
		switch(e.getCode()) {
			case CONTROL:
				ctrlDown = false;
				break;
		}
	}
	
	private void addCurrentWire() {
		int endMidX = endConnection == null
		              ? GuiUtils.getCircuitCoord(lastMousePosition.getX())
		              : endConnection.getX();
		int endMidY = endConnection == null
		              ? GuiUtils.getCircuitCoord(lastMousePosition.getY())
		              : endConnection.getY();
		
		if(endMidX - startConnection.getX() != 0 && endMidY - startConnection.getY() != 0) {
			simulatorWindow.getEditHistory().beginGroup();
			if(isDraggedHorizontally) {
				mayThrow(() -> circuitBoard.addWire(startConnection.getX(), startConnection.getY(),
				                                    endMidX - startConnection.getX(), true));
				mayThrow(() -> circuitBoard.addWire(endMidX, startConnection.getY(),
				                                    endMidY - startConnection.getY(),
				                                    false));
			} else {
				mayThrow(() -> circuitBoard.addWire(startConnection.getX(), startConnection.getY(),
				                                    endMidY - startConnection.getY(), false));
				mayThrow(() -> circuitBoard.addWire(startConnection.getX(), endMidY,
				                                    endMidX - startConnection.getX(), true));
			}
			simulatorWindow.getEditHistory().endGroup();
		} else if(endMidX - startConnection.getX() != 0) {
			mayThrow(() -> circuitBoard.addWire(startConnection.getX(), startConnection.getY(),
			                                    endMidX - startConnection.getX(), true));
		} else if(endMidY - startConnection.getY() != 0) {
			mayThrow(() -> circuitBoard.addWire(endMidX, startConnection.getY(),
			                                    endMidY - startConnection.getY(),
			                                    false));
		} else {
			Set<Connection> connections = circuitBoard.getConnections(startConnection.getX(),
			                                                          startConnection.getY());
			
			setSelectedElements(Stream.concat(ctrlDown ? getSelectedElements().stream() : Stream.empty(),
			                                  connections.stream().map(Connection::getParent))
			                          .collect(Collectors.toSet()));
		}
	}
	
	private void checkStartConnection() {
		if(currentState != SelectingState.CONNECTION_DRAGGED) {
			Set<Connection> selectedConns = circuitBoard.getConnections(
					GuiUtils.getCircuitCoord(lastMousePosition.getX()),
					GuiUtils.getCircuitCoord(lastMousePosition.getY()));
			
			Connection selected = null;
			
			for(Connection connection : selectedConns) {
				if(connection instanceof PortConnection) {
					selected = connection;
					break;
				}
			}
			
			if(selected == null && !selectedConns.isEmpty()) {
				selected = selectedConns.iterator().next();
			}
			
			startConnection = selected;
			
			needsRepaint = true;
		}
	}
	
	private void checkEndConnection(Point2D prevMousePosition) {
		if(currentState == SelectingState.CONNECTION_DRAGGED) {
			int currDiffX = GuiUtils.getCircuitCoord(lastMousePosition.getX()) - startConnection.getX();
			int prevDiffX = GuiUtils.getCircuitCoord(prevMousePosition.getX()) - startConnection.getX();
			int currDiffY = GuiUtils.getCircuitCoord(lastMousePosition.getY()) - startConnection.getY();
			int prevDiffY = GuiUtils.getCircuitCoord(prevMousePosition.getY()) - startConnection.getY();
			
			if(currDiffX == 0 || prevDiffX == 0 ||
					   currDiffX / Math.abs(currDiffX) != prevDiffX / Math.abs(prevDiffX)) {
				isDraggedHorizontally = false;
			}
			
			if(currDiffY == 0 || prevDiffY == 0 ||
					   currDiffY / Math.abs(currDiffY) != prevDiffY / Math.abs(prevDiffY)) {
				isDraggedHorizontally = true;
			}
			
			endConnection = circuitBoard.findConnection(GuiUtils.getCircuitCoord(lastMousePosition.getX()),
			                                            GuiUtils.getCircuitCoord(lastMousePosition.getY()));
			
			needsRepaint = true;
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if(menu != null) {
			menu.hide();
		}
		
		if(e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		
		lastMousePosition = new Point2D(e.getX(), e.getY());
		lastMousePressed = new Point2D(e.getX(), e.getY());
		
		// System.out.println("Mouse Pressed before: " + currentState);
		
		switch(currentState) {
			case ELEMENT_DRAGGED:
			case CONNECTION_SELECTED:
			case HIGHLIGHT_DRAGGED:
				throw new IllegalStateException("How?!");
			
			case IDLE:
			case ELEMENT_SELECTED:
				if(startConnection != null) {
					if(ctrlDown) {
						currentState = SelectingState.CONNECTION_DRAGGED;
					} else {
						currentState = SelectingState.CONNECTION_SELECTED;
					}
				} else {
					Optional<GuiElement> clickedComponent =
							Stream.concat(Stream.concat(circuitBoard.getComponents().stream(),
							                            circuitBoard.getLinks()
							                                        .stream()
							                                        .flatMap(link -> link.getWires().stream())),
							              getSelectedElements().stream())
							      .filter(peer -> peer.containsScreenCoord((int)e.getX(), (int)e.getY()))
							      .findAny();
					if(clickedComponent.isPresent()) {
						GuiElement selectedElement = clickedComponent.get();
						
						if(e.getClickCount() == 2 && selectedElement instanceof SubcircuitPeer) {
							reset();
							((SubcircuitPeer)selectedElement).switchToSubcircuit(this);
						} else if(ctrlDown) {
							Set<GuiElement> elements = new HashSet<>(getSelectedElements());
							elements.add(selectedElement);
							setSelectedElements(elements);
						} else if(!getSelectedElements().contains(selectedElement)) {
							setSelectedElements(Collections.singleton(selectedElement));
						}
						
						if(currentState == SelectingState.IDLE) {
							currentState = SelectingState.ELEMENT_SELECTED;
						}
					} else if(!ctrlDown) {
						reset();
					}
				}
				break;
			
			case CONNECTION_DRAGGED:
				addCurrentWire();
				if(ctrlDown) {
					Set<Connection> selectedConns = circuitBoard.getConnections(GuiUtils.getCircuitCoord(e.getX()),
					                                                            GuiUtils.getCircuitCoord(e.getY()));
					if(!selectedConns.isEmpty()) {
						startConnection = selectedConns.iterator().next();
					}
				} else {
					currentState = SelectingState.IDLE;
					startConnection = null;
					endConnection = null;
				}
				break;
			
			case PLACING_COMPONENT:
				ComponentPeer<?> newComponent = componentCreator.createComponent(properties,
				                                                                 potentialComponent.getX(),
				                                                                 potentialComponent.getY());
				
				mayThrow(() -> circuitBoard.addComponent(newComponent));
				reset();
				if(circuitBoard.getComponents().contains(newComponent)) {
					setSelectedElements(Collections.singleton(newComponent));
				}
				
				currentState = SelectingState.PLACING_COMPONENT;
				break;
		}
		
		// System.out.println("Mouse Pressed after: " + currentState);
		
		needsRepaint = true;
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		
		lastMousePosition = new Point2D(e.getX(), e.getY());
		
		// System.out.println("Mouse Released before: " + currentState);
		
		switch(currentState) {
			case IDLE:
			case ELEMENT_SELECTED:
				Optional<GuiElement> clickedComponent =
						Stream.concat(circuitBoard.getComponents().stream(),
						              circuitBoard.getLinks()
						                          .stream()
						                          .flatMap(link -> link.getWires().stream()))
						      .filter(peer -> peer.containsScreenCoord((int)e.getX(), (int)e.getY()))
						      .findAny();
				if(clickedComponent.isPresent()) {
					GuiElement selectedElement = clickedComponent.get();
					
					if(circuitBoard.getCurrentState() == getCircuit().getTopLevelState() &&
							   selectedElement instanceof PinPeer && ((PinPeer)selectedElement).isInput()) {
						((PinPeer)selectedElement).clicked(circuitBoard.getCurrentState(),
						                                   (int)lastMousePosition.getX(),
						                                   (int)lastMousePosition.getY());
						circuitBoard.runSim();
					} else if(selectedElement instanceof ClockPeer) {
						Clock.tick();
					}
				}
			case ELEMENT_DRAGGED:
				mayThrow(circuitBoard::finalizeMove);
				currentState = SelectingState.IDLE;
				break;
			
			case CONNECTION_SELECTED: {
				Set<Connection> connections = circuitBoard.getConnections(startConnection.getX(),
				                                                          startConnection.getY());
				
				setSelectedElements(Stream.concat(ctrlDown ? getSelectedElements().stream() : Stream.empty(),
				                                  connections.stream().map(Connection::getParent))
				                          .collect(Collectors.toSet()));
				currentState = SelectingState.IDLE;
				break;
			}
			
			case CONNECTION_DRAGGED: {
				if(!ctrlDown) {
					addCurrentWire();
					currentState = SelectingState.IDLE;
					startConnection = null;
					endConnection = null;
				}
				break;
			}
			
			case HIGHLIGHT_DRAGGED:
			case PLACING_COMPONENT:
				currentState = SelectingState.IDLE;
				break;
		}
		
		// System.out.println("Mouse Released after: " + currentState);
		
		checkStartConnection();
		
		needsRepaint = true;
	}
	
	public void mouseDragged(MouseEvent e) {
		if(e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		
		Point2D prevMousePosition = lastMousePosition;
		lastMousePosition = new Point2D(e.getX(), e.getY());
		
		// System.out.println("Mouse Dragged before: " + currentState);
		
		switch(currentState) {
			case IDLE:
			case HIGHLIGHT_DRAGGED:
				currentState = SelectingState.HIGHLIGHT_DRAGGED;
				
				int startX = (int)(lastMousePressed.getX() < lastMousePosition.getX() ? lastMousePressed.getX()
				                                                                      : lastMousePosition.getX());
				int startY = (int)(lastMousePressed.getY() < lastMousePosition.getY() ? lastMousePressed.getY()
				                                                                      : lastMousePosition.getY());
				int width = (int)Math.abs(lastMousePosition.getX() - lastMousePressed.getX());
				int height = (int)Math.abs(lastMousePosition.getY() - lastMousePressed.getY());
				
				if(!ctrlDown) {
					selectedElementsMap.clear();
				}
				
				setSelectedElements(Stream.concat(
						getSelectedElements().stream(),
						Stream.concat(circuitBoard.getComponents().stream(),
						              circuitBoard.getLinks().stream()
						                          .flatMap(link -> link.getWires().stream()))
						      .filter(peer -> peer.isWithinScreenCoord(startX, startY, width, height)))
				                          .collect(Collectors.toSet()));
				break;
			
			case ELEMENT_SELECTED:
			case ELEMENT_DRAGGED:
			case PLACING_COMPONENT:
				int dx = GuiUtils.getCircuitCoord(lastMousePosition.getX() - lastMousePressed.getX());
				int dy = GuiUtils.getCircuitCoord(lastMousePosition.getY() - lastMousePressed.getY());
				
				if(dx != 0 || dy != 0 || currentState == SelectingState.ELEMENT_DRAGGED) {
					currentState = SelectingState.ELEMENT_DRAGGED;
					
					if(!circuitBoard.isMoving()) {
						mayThrow(() -> circuitBoard.initMove(getSelectedElements()));
					}
					
					mayThrow(() -> circuitBoard.moveElements(dx, dy));
				}
				break;
			
			case CONNECTION_SELECTED:
			case CONNECTION_DRAGGED:
				currentState = SelectingState.CONNECTION_DRAGGED;
				checkEndConnection(prevMousePosition);
				break;
		}
		
		// System.out.println("Mouse Dragged after: " + currentState);
		
		checkStartConnection();
		
		needsRepaint = true;
	}
	
	public void mouseMoved(MouseEvent e) {
		Point2D prevMousePosition = lastMousePosition;
		lastMousePosition = new Point2D(e.getX(), e.getY());
		
		if(potentialComponent != null) {
			potentialComponent.setX(GuiUtils.getCircuitCoord(e.getX()) - potentialComponent.getWidth() / 2);
			potentialComponent.setY(GuiUtils.getCircuitCoord(e.getY()) - potentialComponent.getHeight() / 2);
		}
		
		checkStartConnection();
		checkEndConnection(prevMousePosition);
		
		needsRepaint = true;
	}
	
	public void mouseEntered(MouseEvent e) {
		isMouseInsideCanvas = true;
	}
	
	public void mouseExited(MouseEvent e) {
		isMouseInsideCanvas = false;
		ctrlDown = false;
	}
}
