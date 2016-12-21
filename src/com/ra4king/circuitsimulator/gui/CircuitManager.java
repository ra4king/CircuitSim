package com.ra4king.circuitsimulator.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Pin;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Roi Atalla
 */
public class CircuitManager {
	private Canvas canvas;
	private CircuitBoard circuitBoard;
	
	private Point2D lastMousePosition = new Point2D(0, 0);
	private ComponentPeer potentialComponent;
	private Circuit dummyCircuit = new Circuit(new Simulator());
	private CircuitState dummyCircuitState = new CircuitState(dummyCircuit) {
		@Override
		public synchronized void pushValue(Port port, WireValue value) {}
		
		@Override
		public WireValue getValue(Link link) {
			return new WireValue(link.getBitSize());
		}
	};
	
	private Connection startConnection, endConnection;
	private Point2D startPoint, draggedPoint;
	private boolean isDraggedHorizontally;
	
	private boolean ctrlDown;
	
	private Map<GuiElement, Point2D> selectedElementsMap = new HashMap<>();
	private boolean selecting;
	
	private ComponentCreator componentCreator;
	
	public CircuitManager(Canvas canvas, Simulator simulator) {
		this.canvas = canvas;
		circuitBoard = new CircuitBoard(simulator);
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
	
	public void modifiedSelection(ComponentCreator componentCreator) {
		this.componentCreator = componentCreator;
		
		dummyCircuit.getComponents().clear();
		
		if(componentCreator != null) {
			potentialComponent = componentCreator.createComponent(dummyCircuit, GuiUtils.getCircuitCoord(lastMousePosition.getX()), GuiUtils.getCircuitCoord(lastMousePosition.getY()));
			potentialComponent.setX(potentialComponent.getX() - potentialComponent.getWidth() / 2);
			potentialComponent.setY(potentialComponent.getY() - potentialComponent.getHeight() / 2);
		} else {
			potentialComponent = null;
		}
	}
	
	public void repaint() {
		if(Platform.isFxApplicationThread()) {
			paint(canvas.getGraphicsContext2D());
		} else {
			Platform.runLater(() -> paint(canvas.getGraphicsContext2D()));
		}
	}
	
	public void paint(GraphicsContext graphics) {
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
			
			if(draggedPoint != null) {
				int startX = startConnection.getScreenX() + startConnection.getScreenWidth() / 2;
				int startY = startConnection.getScreenY() + startConnection.getScreenHeight() / 2;
				int pointX = GuiUtils.getScreenCircuitCoord(draggedPoint.getX());
				int pointY = GuiUtils.getScreenCircuitCoord(draggedPoint.getY());
				graphics.setStroke(Color.BLACK);
				if(isDraggedHorizontally) {
					graphics.strokeLine(startX, startY, pointX, startY);
					graphics.strokeLine(pointX, startY, pointX, pointY);
				} else {
					graphics.strokeLine(startX, startY, startX, pointY);
					graphics.strokeLine(startX, pointY, pointX, pointY);
				}
			}
			
			graphics.restore();
		} else if(potentialComponent != null) {
			graphics.save();
			potentialComponent.paint(graphics, dummyCircuitState);
			graphics.restore();
			
			for(Connection connection : potentialComponent.getConnections()) {
				graphics.save();
				connection.paint(graphics, dummyCircuitState);
				graphics.restore();
			}
		} else if(!selecting && startPoint != null) {
			double startX = startPoint.getX() < draggedPoint.getX() ? startPoint.getX() : draggedPoint.getX();
			double startY = startPoint.getY() < draggedPoint.getY() ? startPoint.getY() : draggedPoint.getY();
			double width = Math.abs(draggedPoint.getX() - startPoint.getX());
			double height = Math.abs(draggedPoint.getY() - startPoint.getY());
			
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
		
		graphics.restore();
	}
	
	private void reset() {
		isDraggedHorizontally = false;
		startConnection = null;
		endConnection = null;
		startPoint = null;
		draggedPoint = null;
		selecting = false;
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
			case CONTROL:
				ctrlDown = e.getEventType() != KeyEvent.KEY_RELEASED;
				break;
			case NUMPAD0:
			case NUMPAD1:
			case DIGIT0:
			case DIGIT1:
				int value = e.getText().charAt(0) - '0';
				
				GuiElement selectedElem;
				if(selectedElementsMap.size() == 1 &&
						   (selectedElem = selectedElementsMap.keySet().iterator().next()) instanceof PinPeer) {
					PinPeer selectedPin = (PinPeer)selectedElem;
					WireValue currentValue =
							new WireValue(getCircuit().getTopLevelState()
									              .getMergedValue(selectedPin.getComponent().getPort(Pin.PORT)));
					
					for(int i = currentValue.getBitSize() - 1; i > 0; i--) {
						currentValue.setBit(i, currentValue.getBit(i - 1));
					}
					currentValue.setBit(0, value == 1 ? State.ONE : State.ZERO);
					selectedPin.getComponent().setValue(getCircuit().getTopLevelState(), currentValue);
					circuitBoard.runSim();
					break;
				}
			case BACK_SPACE:
			case DELETE:
				circuitBoard.removeElements(selectedElementsMap.keySet());
			case ESCAPE:
				selectedElementsMap.clear();
				reset();
				break;
		}
		
		repaint();
	}
	
	public void mousePressed(MouseEvent e) {
		if(startConnection != null) {
			draggedPoint = new Point2D(e.getX(), e.getY());
		} else if(potentialComponent != null) {
			if(componentCreator != null) {
				circuitBoard.createComponent(componentCreator, potentialComponent.getX(), potentialComponent.getY());
			}
		} else {
			startPoint = new Point2D(e.getX(), e.getY());
			draggedPoint = startPoint;
			lastDx = 0;
			lastDy = 0;
			
			Optional<GuiElement> clickedComponent =
					Stream.concat(circuitBoard.getComponents().stream(), circuitBoard.getLinks()
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
					circuitBoard.runSim();
				}
			} else {
				selecting = false;
				selectedElementsMap.clear();
			}
		}
		
		repaint();
	}
	
	public void mouseReleased(MouseEvent e) {
		if(draggedPoint != null && startConnection != null) {
			int endMidX = endConnection == null
					              ? GuiUtils.getCircuitCoord(draggedPoint.getX())
					              : endConnection.getX();
			int endMidY = endConnection == null
					              ? GuiUtils.getCircuitCoord(draggedPoint.getY())
					              : endConnection.getY();
			
			if(endMidX - startConnection.getX() != 0 && endMidY - startConnection.getY() != 0) {
				if(isDraggedHorizontally) {
					circuitBoard.addWire(startConnection.getX(), startConnection.getY(), endMidX - startConnection.getX(), true);
					circuitBoard.addWire(endMidX, startConnection.getY(), endMidY - startConnection.getY(), false);
				} else {
					circuitBoard.addWire(startConnection.getX(), startConnection.getY(), endMidY - startConnection.getY(), false);
					circuitBoard.addWire(startConnection.getX(), endMidY, endMidX - startConnection.getX(), true);
				}
			} else if(endMidX - startConnection.getX() != 0) {
				circuitBoard.addWire(startConnection.getX(), startConnection.getY(), endMidX - startConnection.getX(), true);
			} else if(endMidY - startConnection.getY() != 0) {
				circuitBoard.addWire(endMidX, startConnection.getY(), endMidY - startConnection.getY(), false);
			}
		}
		
		reset();
		mouseMoved(e);
		repaint();
	}
	
	private int lastDx;
	private int lastDy;
	
	public void mouseDragged(MouseEvent e) {
		Point2D curPos = new Point2D(e.getX(), e.getY());
		
		if(selecting && !selectedElementsMap.isEmpty()) {
			Point2D diff = curPos.subtract(startPoint).multiply(1.0 / GuiUtils.BLOCK_SIZE);
			int dx = (int)diff.getX() - lastDx;
			int dy = (int)diff.getY() - lastDy;
			
			if(dx != 0 || dy != 0) {
				circuitBoard.moveElements(selectedElementsMap.keySet(), dx, dy);
				lastDx += dx;
				lastDy += dy;
			}
		}
		else if(startPoint != null) {
			int startX = (int)(startPoint.getX() < draggedPoint.getX() ? startPoint.getX() : draggedPoint.getX());
			int startY = (int)(startPoint.getY() < draggedPoint.getY() ? startPoint.getY() : draggedPoint.getY());
			int width = (int)Math.abs(draggedPoint.getX() - startPoint.getX());
			int height = (int)Math.abs(draggedPoint.getY() - startPoint.getY());
			
			selectedElementsMap = Stream.concat(circuitBoard.getComponents().stream(),
							circuitBoard.getLinks().stream().flatMap(link -> link.getWires().stream()))
							.filter(peer -> peer.intersectsScreenCoord(startX, startY, width, height))
							.collect(Collectors.toMap(peer -> peer, peer -> new Point2D(peer.getX(), peer.getY())));
		}
		
		if(draggedPoint != null) {
			if(startConnection != null) {
				int currDiffX = GuiUtils.getCircuitCoord(e.getX()) - startConnection.getX();
				int prevDiffX = GuiUtils.getCircuitCoord(draggedPoint.getX()) - startConnection.getX();
				int currDiffY = GuiUtils.getCircuitCoord(e.getY()) - startConnection.getY();
				int prevDiffY = GuiUtils.getCircuitCoord(draggedPoint.getY()) - startConnection.getY();
				
				if(currDiffX == 0 || prevDiffX == 0 ||
						   currDiffX / Math.abs(currDiffX) != prevDiffX / Math.abs(prevDiffX)) {
					isDraggedHorizontally = false;
				}
				
				if(currDiffY == 0 || prevDiffY == 0 ||
						   currDiffY / Math.abs(currDiffY) != prevDiffY / Math.abs(prevDiffY)) {
					isDraggedHorizontally = true;
				}
			}
			
			draggedPoint = curPos;
			endConnection = circuitBoard.findConnection(
					GuiUtils.getCircuitCoord(draggedPoint.getX()),
					GuiUtils.getCircuitCoord(draggedPoint.getY()));
		}
		
		repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		boolean repaint = false;
		lastMousePosition = new Point2D(e.getX(), e.getY());
		if(potentialComponent != null) {
			potentialComponent.setX(GuiUtils.getCircuitCoord(e.getX()) - potentialComponent.getWidth() / 2);
			potentialComponent.setY(GuiUtils.getCircuitCoord(e.getY()) - potentialComponent.getHeight() / 2);
			repaint = true;
		}
		
		Connection selected = circuitBoard.findConnection(GuiUtils.getCircuitCoord(e.getX()), GuiUtils.getCircuitCoord(e.getY()));
		
		if(selected != startConnection) {
			startConnection = selected;
			repaint = true;
		}
		
		if(repaint) repaint();
	}
}
