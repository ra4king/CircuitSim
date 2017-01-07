package com.ra4king.circuitsimulator.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.peers.ClockPeer;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.components.Pin;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Roi Atalla
 */
public class CircuitManager {
	private final CircuitSimulator simulatorWindow;
	private final Canvas canvas;
	private final CircuitBoard circuitBoard;
	
	private Point2D lastMousePosition = new Point2D(0, 0);
	private ComponentPeer<?> potentialComponent;
	private Circuit dummyCircuit = new Circuit(new Simulator());
	private CircuitState dummyCircuitState = new CircuitState(dummyCircuit);
	
	private boolean mouseInside;
	
	private Connection startConnection, endConnection;
	private Point2D startPoint, curDraggedPoint, draggedDelta = new Point2D(0, 0);
	private boolean isDraggedHorizontally;
	
	private boolean ctrlDown;
	
	private Map<GuiElement, Point2D> selectedElementsMap = new HashMap<>();
	private boolean selecting;
	
	private ComponentCreator componentCreator;
	private Properties properties;
	
	private String message;
	private long messageSetTime;
	private static final int MESSAGE_POST_DURATION = 5000;
	
	public CircuitManager(CircuitSimulator simulatorWindow, Canvas canvas, Simulator simulator) {
		this.simulatorWindow = simulatorWindow;
		this.canvas = canvas;
		circuitBoard = new CircuitBoard(simulator);
		
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				Platform.runLater(() -> paint(canvas.getGraphicsContext2D()));
			}
		}.start();
	}
	
	public Circuit getCircuit() {
		return circuitBoard.getCircuit();
	}
	
	public CircuitBoard getCircuitBoard() {
		return circuitBoard;
	}
	
	public Point2D getLastMousePosition() {
		return lastMousePosition;
	}
	
	public void setLastMousePosition(Point2D lastMousePosition) {
		this.lastMousePosition = lastMousePosition;
	}
	
	private Properties getCommonSelectedProperties() {
		return selectedElementsMap.keySet().stream()
		                          .filter(element -> element instanceof ComponentPeer<?>)
		                          .map(element -> ((ComponentPeer<?>)element).getProperties())
		                          .reduce(Properties::intersect).orElse(null);
	}
	
	public Properties modifiedSelection(ComponentCreator componentCreator, Properties properties) {
		this.componentCreator = componentCreator;
		this.properties = properties;
		
		if(componentCreator != null) {
			dummyCircuit.clearComponents();
			
			potentialComponent = componentCreator.createComponent(properties,
			                                                      GuiUtils.getCircuitCoord(lastMousePosition.getX()),
			                                                      GuiUtils.getCircuitCoord(lastMousePosition.getY()));
			mayThrow(() -> dummyCircuit.addComponent(potentialComponent.getComponent()));
			potentialComponent.setX(potentialComponent.getX() - potentialComponent.getWidth() / 2);
			potentialComponent.setY(potentialComponent.getY() - potentialComponent.getHeight() / 2);
			return potentialComponent.getProperties();
		} else if(properties != null && !properties.isEmpty() && !selectedElementsMap.isEmpty()) {
			potentialComponent = null;
			
			Set<ComponentPeer<?>> components =
					selectedElementsMap.keySet().stream()
					                   .filter(element -> element instanceof ComponentPeer<?>)
					                   .map(element -> (ComponentPeer<?>)element)
					                   .collect(Collectors.toSet());
			mayThrow(() -> circuitBoard.removeElements(components));
			
			Set<ComponentPeer<?>> newComponents =
					components.stream().map(
							component ->
									(ComponentPeer<?>)ComponentManager.forClass(component.getClass())
									                                  .createComponent(
											                                  component.getProperties()
											                                           .mergeIfExists(properties),
											                                  component.getX(),
											                                  component.getY()))
					          .collect(Collectors.toSet());
			
			newComponents.forEach(component -> mayThrow(() -> circuitBoard.addComponent(component)));
			
			selectedElementsMap = Stream.concat(
					selectedElementsMap.keySet().stream().filter(element -> !(element instanceof ComponentPeer<?>)),
					newComponents.stream())
			                            .collect(Collectors.toMap(element -> element,
			                                                      element -> new Point2D(element.getX(),
			                                                                             element.getY())));
			
			
			return getCommonSelectedProperties();
		}
		
		potentialComponent = null;
		return new Properties();
	}
	
	private long lastRepaint = System.nanoTime();
	private int frameCount;
	
	public void paint(GraphicsContext graphics) {
		long now = System.nanoTime();
		if(now - lastRepaint >= 1e9) {
			System.out.println("FPS: " + frameCount);
			frameCount = 0;
			lastRepaint = now;
		}
		
		frameCount++;
		
		graphics.save();
		
		graphics.setFont(Font.font("monospace", 15));
		
		graphics.save();
		graphics.setFill(Color.LIGHTGRAY);
		graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		graphics.setFill(Color.BLACK);
		for(int i = 0; i < canvas.getWidth(); i += GuiUtils.BLOCK_SIZE) {
			for(int j = 0; j < canvas.getHeight(); j += GuiUtils.BLOCK_SIZE) {
				graphics.fillRect(i, j, 1, 1);
			}
		}
		
		graphics.restore();
		
		circuitBoard.paint(graphics);
		
		if(startConnection != null) {
			graphics.save();
			
			graphics.setLineWidth(2);
			graphics.setStroke(Color.GREEN);
			graphics.strokeOval(startConnection.getScreenX() - 2, startConnection.getScreenY() - 2, 10, 10);
			
			if(endConnection != null) {
				graphics.strokeOval(endConnection.getScreenX() - 2, endConnection.getScreenY() - 2, 10, 10);
			}
			
			if(curDraggedPoint != null) {
				int startX = startConnection.getScreenX() + startConnection.getScreenWidth() / 2;
				int startY = startConnection.getScreenY() + startConnection.getScreenHeight() / 2;
				int pointX = GuiUtils.getScreenCircuitCoord(curDraggedPoint.getX());
				int pointY = GuiUtils.getScreenCircuitCoord(curDraggedPoint.getY());
				graphics.setStroke(Color.BLACK);
				if(isDraggedHorizontally) {
					graphics.strokeLine(startX, startY, pointX, startY);
					graphics.strokeLine(pointX, startY, pointX, pointY);
				} else {
					graphics.strokeLine(startX, startY, startX, pointY);
					graphics.strokeLine(startX, pointY, pointX, pointY);
				}
			} else if(startConnection instanceof PortConnection) {
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
		} else if(potentialComponent != null && mouseInside) {
			graphics.save();
			potentialComponent.paint(graphics, dummyCircuitState);
			graphics.restore();
			
			for(Connection connection : potentialComponent.getConnections()) {
				graphics.save();
				connection.paint(graphics, dummyCircuitState);
				graphics.restore();
			}
		} else if(!selecting && startPoint != null) {
			double startX = startPoint.getX() < curDraggedPoint.getX() ? startPoint.getX() : curDraggedPoint.getX();
			double startY = startPoint.getY() < curDraggedPoint.getY() ? startPoint.getY() : curDraggedPoint.getY();
			double width = Math.abs(curDraggedPoint.getX() - startPoint.getX());
			double height = Math.abs(curDraggedPoint.getY() - startPoint.getY());
			
			graphics.setStroke(Color.GREEN.darker());
			graphics.strokeRect(startX, startY, width, height);
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
		
		if(System.nanoTime() - messageSetTime < MESSAGE_POST_DURATION * 1000000L) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), message);
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(message, (canvas.getWidth() - bounds.getWidth()) * 0.5, canvas.getHeight() - 50);
		}
		
		graphics.restore();
	}
	
	private interface ThrowableRunnable {
		void run() throws Exception;
	}
	
	private void mayThrow(ThrowableRunnable runnable) {
		try {
			runnable.run();
			messageSetTime = 0;
		} catch(Exception exc) {
			exc.printStackTrace();
			message = exc.getMessage();
			messageSetTime = System.nanoTime();
		}
	}
	
	private void reset() {
		simulatorWindow.clearSelection();
		potentialComponent = null;
		isDraggedHorizontally = false;
		startConnection = null;
		endConnection = null;
		startPoint = null;
		curDraggedPoint = null;
		selecting = false;
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
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
							new WireValue(getCircuit().getTopLevelState()
							                          .getLastPushedValue(
									                          selectedPin.getComponent().getPort(Pin.PORT)));
					
					for(int i = currentValue.getBitSize() - 1; i > 0; i--) {
						currentValue.setBit(i, currentValue.getBit(i - 1));
					}
					currentValue.setBit(0, value == 1 ? State.ONE : State.ZERO);
					selectedPin.getComponent().setValue(getCircuit().getTopLevelState(), currentValue);
					mayThrow(circuitBoard::runSim);
				}
				break;
			case BACK_SPACE:
			case DELETE:
				mayThrow(() -> circuitBoard.removeElements(selectedElementsMap.keySet()));
				selectedElementsMap.clear();
				simulatorWindow.setProperties(null);
			case ESCAPE:
				if(!selectedElementsMap.isEmpty()) {
					if(draggedDelta.getX() != 0 || draggedDelta.getY() != 0) {
						mayThrow(() -> circuitBoard.moveElements(-(int)draggedDelta.getX(),
						                                         -(int)draggedDelta.getY()));
						mayThrow(circuitBoard::finalizeMove);
						draggedDelta = new Point2D(0, 0);
					}
					selectedElementsMap.clear();
					simulatorWindow.setProperties(null);
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
	
	public void mousePressed(MouseEvent e) {
		if(startConnection != null) {
			curDraggedPoint = new Point2D(e.getX(), e.getY());
			potentialComponent = null;
		} else if(potentialComponent != null) {
			if(componentCreator != null) {
				mayThrow(() -> circuitBoard.addComponent(
						componentCreator.createComponent(properties,
						                                 potentialComponent.getX(),
						                                 potentialComponent.getY())));
			}
		} else {
			reset();
			
			startPoint = new Point2D(e.getX(), e.getY());
			curDraggedPoint = startPoint;
			draggedDelta = new Point2D(0, 0);
			
			Optional<GuiElement> clickedComponent =
					Stream.concat(circuitBoard.getComponents().stream(),
					              circuitBoard.getLinks()
					                          .stream()
					                          .flatMap(link -> link.getWires().stream()))
					      .filter(peer -> peer.containsScreenCoord((int)e.getX(), (int)e.getY()))
					      .findAny();
			if(clickedComponent.isPresent()) {
				selecting = true;
				GuiElement selectedElement = clickedComponent.get();
				
				if(!ctrlDown && selectedElementsMap.size() == 1) {
					selectedElementsMap.clear();
				}
				
				selectedElementsMap.put(selectedElement, new Point2D(selectedElement.getX(), selectedElement.getY()));
				
				if(selectedElement instanceof PinPeer && ((PinPeer)selectedElement).isInput()) {
					((PinPeer)selectedElement).clicked(getCircuit().getTopLevelState(), (int)e.getX(), (int)e.getY());
					mayThrow(circuitBoard::runSim);
				} else if(selectedElement instanceof ClockPeer) {
					Clock.tick();
				}
			} else if(!ctrlDown) {
				selecting = false;
				selectedElementsMap.clear();
			}
			
			simulatorWindow.setProperties(getCommonSelectedProperties());
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(selecting && !selectedElementsMap.isEmpty()) {
			mayThrow(circuitBoard::finalizeMove);
			draggedDelta = new Point2D(0, 0);
		}
		
		if(curDraggedPoint != null && startConnection != null) {
			int endMidX = endConnection == null
			              ? GuiUtils.getCircuitCoord(curDraggedPoint.getX())
			              : endConnection.getX();
			int endMidY = endConnection == null
			              ? GuiUtils.getCircuitCoord(curDraggedPoint.getY())
			              : endConnection.getY();
			
			if(endMidX - startConnection.getX() != 0 && endMidY - startConnection.getY() != 0) {
				if(isDraggedHorizontally) {
					mayThrow(() -> circuitBoard.addWire(startConnection.getX(), startConnection.getY(),
					                                    endMidX - startConnection.getX(), true));
					mayThrow(() -> circuitBoard.addWire(endMidX, startConnection.getY(),
					                                    endMidY - startConnection.getY(), false));
				} else {
					mayThrow(() -> circuitBoard.addWire(startConnection.getX(), startConnection.getY(),
					                                    endMidY - startConnection.getY(), false));
					mayThrow(() -> circuitBoard.addWire(startConnection.getX(), endMidY,
					                                    endMidX - startConnection.getX(), true));
				}
			} else if(endMidX - startConnection.getX() != 0) {
				mayThrow(() -> circuitBoard.addWire(startConnection.getX(), startConnection.getY(),
				                                    endMidX - startConnection.getX(), true));
			} else if(endMidY - startConnection.getY() != 0) {
				mayThrow(() -> circuitBoard.addWire(endMidX, startConnection.getY(), endMidY - startConnection.getY(),
				                                    false));
			} else {
				Set<Connection> connections = circuitBoard.getConnections(startConnection.getX(),
				                                                          startConnection.getY());
				if(!ctrlDown) {
					selectedElementsMap.clear();
				}
				
				selecting = true;
				selectedElementsMap.putAll(
						connections.stream()
						           .collect(Collectors.toMap(Connection::getParent,
						                                     conn -> new Point2D(conn.getParent().getX(),
						                                                         conn.getParent().getY()))));
				
				simulatorWindow.setProperties(getCommonSelectedProperties());
			}
		}
		
		startConnection = null;
		endConnection = null;
		startPoint = null;
		curDraggedPoint = null;
		
		mouseMoved(e);
	}
	
	public void mouseDragged(MouseEvent e) {
		Point2D curPos = new Point2D(e.getX(), e.getY());
		
		if(selecting && !selectedElementsMap.isEmpty()) {
			Point2D diff = curPos.subtract(startPoint).multiply(1.0 / GuiUtils.BLOCK_SIZE);
			int dx = (int)(diff.getX() - draggedDelta.getX());
			int dy = (int)(diff.getY() - draggedDelta.getY());
			
			if(dx != 0 || dy != 0) {
				if(draggedDelta.getX() == 0 && draggedDelta.getY() == 0) {
					mayThrow(() -> circuitBoard.initMove(selectedElementsMap.keySet()));
				}
				
				mayThrow(() -> circuitBoard.moveElements(dx, dy));
				draggedDelta = draggedDelta.add(dx, dy);
			}
		} else if(startPoint != null) {
			int startX = (int)(startPoint.getX() < curDraggedPoint.getX() ? startPoint.getX()
			                                                              : curDraggedPoint.getX());
			int startY = (int)(startPoint.getY() < curDraggedPoint.getY() ? startPoint.getY()
			                                                              : curDraggedPoint.getY());
			int width = (int)Math.abs(curDraggedPoint.getX() - startPoint.getX());
			int height = (int)Math.abs(curDraggedPoint.getY() - startPoint.getY());
			
			if(!ctrlDown) {
				selectedElementsMap.clear();
			}
			
			selectedElementsMap.putAll(Stream.concat(circuitBoard.getComponents().stream(),
			                                         circuitBoard.getLinks()
			                                                     .stream()
			                                                     .flatMap(link -> link.getWires().stream()))
			                                 .filter(peer -> peer.isWithinScreenCoord(startX, startY, width, height))
			                                 .collect(
					                                 Collectors.toMap(peer -> peer,
					                                                  peer -> new Point2D(peer.getX(), peer.getY()))));
			
			simulatorWindow.setProperties(getCommonSelectedProperties());
		}
		
		if(curDraggedPoint != null) {
			if(startConnection != null) {
				int currDiffX = GuiUtils.getCircuitCoord(e.getX()) - startConnection.getX();
				int prevDiffX = GuiUtils.getCircuitCoord(curDraggedPoint.getX()) - startConnection.getX();
				int currDiffY = GuiUtils.getCircuitCoord(e.getY()) - startConnection.getY();
				int prevDiffY = GuiUtils.getCircuitCoord(curDraggedPoint.getY()) - startConnection.getY();
				
				if(currDiffX == 0 || prevDiffX == 0 ||
						   currDiffX / Math.abs(currDiffX) != prevDiffX / Math.abs(prevDiffX)) {
					isDraggedHorizontally = false;
				}
				
				if(currDiffY == 0 || prevDiffY == 0 ||
						   currDiffY / Math.abs(currDiffY) != prevDiffY / Math.abs(prevDiffY)) {
					isDraggedHorizontally = true;
				}
			}
			
			curDraggedPoint = curPos;
			endConnection = circuitBoard.findConnection(
					GuiUtils.getCircuitCoord(curDraggedPoint.getX()),
					GuiUtils.getCircuitCoord(curDraggedPoint.getY()));
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		lastMousePosition = new Point2D(e.getX(), e.getY());
		if(potentialComponent != null) {
			potentialComponent.setX(GuiUtils.getCircuitCoord(e.getX()) - potentialComponent.getWidth() / 2);
			potentialComponent.setY(GuiUtils.getCircuitCoord(e.getY()) - potentialComponent.getHeight() / 2);
		}
		
		if(selectedElementsMap.isEmpty()) {
			Set<Connection> selectedConns = circuitBoard.getConnections(GuiUtils.getCircuitCoord(e.getX()),
			                                                            GuiUtils.getCircuitCoord(e.getY()));
			
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
			
			if(selected != startConnection) {
				startConnection = selected;
			}
		} else {
			startConnection = null;
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		mouseInside = true;
	}
	
	public void mouseExited(MouseEvent e) {
		mouseInside = false;
		ctrlDown = false;
	}
}
