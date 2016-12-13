package com.ra4king.circuitsimulator.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.ShortCircuitException;
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
	private Circuit circuit;
	
	private Set<ComponentPeer<?>> componentPeers;
	private Set<LinkWires> allLinkWires = new HashSet<>();
	private Set<LinkWires> badLinks;
	
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
	
	private Set<GuiElement> selectedElements = new HashSet<>();
	
	private ComponentCreator componentCreator;
	
	public CircuitManager(Canvas canvas, Simulator simulator) {
		this.canvas = canvas;
		circuit = new Circuit(simulator);
		
		componentPeers = new HashSet<>();
	}
	
	public Circuit getCircuit() {
		return circuit;
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
	
	public void runSim() {
		if((badLinks =
				    allLinkWires.stream().filter(link -> !link.isLinkGood()).collect(Collectors.toSet())).size() != 0) {
			return;
		}
		
		try {
			circuit.getSimulator().stepAll();
		}
		catch(ShortCircuitException exc) {
			exc.printStackTrace();
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
		
		graphics.setFont(Font.font("monospace", 13));
		
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
		
		for(ComponentPeer<?> peer : componentPeers) {
			graphics.save();
			peer.paint(graphics, circuit.getTopLevelState());
			graphics.restore();
			
			for(Connection connection : peer.getConnections()) {
				graphics.save();
				connection.paint(graphics, circuit.getTopLevelState());
				graphics.restore();
			}
		}
		
		allLinkWires.forEach(linkWire -> {
			graphics.save();
			linkWire.paint(graphics, circuit.getTopLevelState());
			graphics.restore();
			
			for(Wire wire : linkWire.getWires()) {
				graphics.save();
				wire.paint(graphics, circuit.getTopLevelState());
				graphics.restore();
				
				List<Connection> connections = wire.getConnections();
				
				graphics.save();
				connections.get(0).paint(graphics, circuit.getTopLevelState());
				graphics.restore();
				
				graphics.save();
				connections.get(connections.size() - 1).paint(graphics, circuit.getTopLevelState());
				graphics.restore();
			}
		});
		
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
		} else if(startPoint != null) {
			double startX = startPoint.getX() < draggedPoint.getX() ? startPoint.getX() : draggedPoint.getX();
			double startY = startPoint.getY() < draggedPoint.getY() ? startPoint.getY() : draggedPoint.getY();
			double width = Math.abs(draggedPoint.getX() - startPoint.getX());
			double height = Math.abs(draggedPoint.getY() - startPoint.getY());
			
			graphics.setStroke(Color.GREEN.darker());
			graphics.strokeRect(startX, startY, width, height);
		}
		
		if(badLinks != null) {
			for(LinkWires linkWires : badLinks) {
				Stream.concat(linkWires.getPorts().stream(), linkWires.getBadPorts().stream()).forEach(port -> {
					graphics.setStroke(Color.BLACK);
					graphics.strokeText(
							String.valueOf(port.getLink().getBitSize()), port.getScreenX() + 11, port.getScreenY() + 21);
					
					graphics.setStroke(Color.ORANGE);
					graphics.strokeOval(port.getScreenX() - 2, port.getScreenY() - 2, 10, 10);
					graphics.strokeText(
							String.valueOf(port.getLink().getBitSize()), port.getScreenX() + 10, port.getScreenY() + 20);
				});
			}
		}
		
		for(GuiElement selectedElement : selectedElements) {
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
	
	public void keyPressed(KeyEvent e) {
		switch(e.getCode()) {
			case NUMPAD0:
			case NUMPAD1:
			case DIGIT0:
			case DIGIT1:
				int value = e.getText().charAt(0) - '0';
				
				GuiElement selectedElem;
				if(selectedElements.size() == 1 &&
						   (selectedElem = selectedElements.iterator().next()) instanceof PinPeer) {
					PinPeer selectedPin = (PinPeer)selectedElem;
					WireValue currentValue =
							new WireValue(circuit.getTopLevelState()
									              .getMergedValue(selectedPin.getComponent().getPort(Pin.PORT)));
					
					for(int i = currentValue.getBitSize() - 1; i > 0; i--) {
						currentValue.setBit(i, currentValue.getBit(i - 1));
					}
					currentValue.setBit(0, value == 1 ? State.ONE : State.ZERO);
					selectedPin.getComponent().setValue(circuit.getTopLevelState(), currentValue);
					runSim();
					break;
				}
			case BACK_SPACE:
			case DELETE:
				for(GuiElement selectedElement : selectedElements) {
					List<Connection> connections = selectedElement.getConnections();
					if(selectedElement instanceof ComponentPeer<?>) {
						for(Connection connection : connections) {
							PortConnection portConnection = (PortConnection)connection;
							LinkWires linkWires = portConnection.getLinkWires();
							if(linkWires != null) {
								linkWires.removePort(portConnection);
								if(linkWires.isEmpty()) {
									linkWires.clear();
									allLinkWires.remove(linkWires);
								}
							}
						}
						componentPeers.remove(selectedElement);
						circuit.removeComponent(((ComponentPeer)selectedElement).getComponent());
					} else if(selectedElement instanceof Wire) {
						Wire wire = (Wire)selectedElement;
						LinkWires linkWires = wire.getLinkWires();
						allLinkWires.remove(linkWires);
						allLinkWires.addAll(linkWires.removeWire(wire));
					}
				}
				runSim();
			case ESCAPE:
				selectedElements.clear();
				startConnection = null;
				endConnection = null;
				startPoint = null;
				draggedPoint = null;
				break;
		}
		
		repaint();
	}
	
	public void mousePressed(MouseEvent e) {
		if(startConnection != null) {
			draggedPoint = new Point2D(e.getX(), e.getY());
		} else if(potentialComponent != null) {
			for(ComponentPeer<?> component : componentPeers) {
				if(component.intersects(potentialComponent)) {
					return;
				}
			}
			
			if(componentCreator != null) {
				ComponentPeer<?> peer = componentCreator.createComponent(circuit, potentialComponent.getX(), potentialComponent.getY());
				
				for(Connection connection : peer.getConnections()) {
					Connection attached = findConnection(connection.getX(), connection.getY());
					if(attached != null) {
						LinkWires linkWires = attached.getLinkWires();
						if(attached instanceof WireConnection) {
							linkWires.addPort((PortConnection)connection);
						} else {
							if(linkWires == null) {
								linkWires = new LinkWires();
								linkWires.addPort((PortConnection)connection);
								linkWires.addPort((PortConnection)attached);
								allLinkWires.add(linkWires);
							} else if(allLinkWires.remove(linkWires)) {
								handleConnection(connection, linkWires);
							}
						}
					}
				}
				
				componentPeers.add(peer);
				
				runSim();
			}
		} else {
			startPoint = new Point2D(e.getX(), e.getY());
			draggedPoint = new Point2D(e.getX(), e.getY());
			
			Optional<GuiElement> clickedComponent =
					Stream.concat(componentPeers.stream(), allLinkWires
							                                       .stream()
							                                       .flatMap(link -> link.getWires().stream()))
							.filter(peer -> peer.containsScreenCoord((int)e.getX(), (int)e.getY()))
							.findAny();
			if(clickedComponent.isPresent()) {
				GuiElement selectedElement = clickedComponent.get();
				selectedElements.clear();
				selectedElements.add(selectedElement);
				if(selectedElement instanceof PinPeer && ((PinPeer)selectedElement).isInput()) {
					Pin pin = ((PinPeer)selectedElement).getComponent();
					WireValue value = circuit.getTopLevelState().getLastPushedValue(pin.getPort(Pin.PORT));
					if(value.getBitSize() == 1) {
						pin.setValue(circuit.getTopLevelState(),
								new WireValue(1, value.getBit(0) == State.ONE ? State.ZERO : State.ONE));
					}
					runSim();
				}
			} else {
				selectedElements.clear();
			}
		}
		
		repaint();
	}
	
	private void handleConnection(Connection connection, LinkWires linkWires) {
		if(connection instanceof PortConnection) {
			linkWires.addPort((PortConnection)connection);
		} else if(connection instanceof WireConnection) {
			LinkWires selectedLink = ((Wire)connection.getParent()).getLinkWires();
			allLinkWires.remove(selectedLink);
			linkWires.merge(selectedLink);
		}
		
		allLinkWires.add(linkWires);
	}
	
	private void addWire(LinkWires linkWires, int x, int y, int length, boolean horizontal) {
		Connection lastConnection = findConnection(x, y);
		handleConnection(lastConnection, linkWires);
		
		int sign = length / Math.abs(length);
		for(int i = sign; Math.abs(i) <= Math.abs(length); i += sign) {
			int xOff = horizontal ? i: 0;
			int yOff = horizontal ? 0 : i;
			Connection currConnection = findConnection(x + xOff, y + yOff);
			if(currConnection != null) {
				if(lastConnection != null) {
					int len = horizontal ? currConnection.getX() - lastConnection.getX()
							          : currConnection.getY() - lastConnection.getY();
					linkWires.addWire(new Wire(linkWires, lastConnection.getX(), lastConnection.getY(), len, horizontal));
				}
				
				handleConnection(currConnection, linkWires);
				lastConnection = currConnection;
			} else if(i == length) {
				int len = horizontal ? x + xOff - lastConnection.getX()
						          : y + yOff - lastConnection.getY();
				linkWires.addWire(new Wire(linkWires, lastConnection.getX(), lastConnection.getY(), len, horizontal));
			}
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(draggedPoint != null && startConnection != null) {
			LinkWires link = new LinkWires();
			
			int endMidX = endConnection == null
					              ? GuiUtils.getCircuitCoord(draggedPoint.getX())
					              : endConnection.getX();
			int endMidY = endConnection == null
					              ? GuiUtils.getCircuitCoord(draggedPoint.getY())
					              : endConnection.getY();
			
			if(endMidX - startConnection.getX() != 0 && endMidY - startConnection.getY() != 0) {
				if(isDraggedHorizontally) {
					addWire(link, startConnection.getX(), startConnection.getY(), endMidX - startConnection.getX(), true);
					addWire(link, endMidX, startConnection.getY(), endMidY - startConnection.getY(), false);
				} else {
					addWire(link, startConnection.getX(), startConnection.getY(), endMidY - startConnection.getY(), false);
					addWire(link, startConnection.getX(), endMidY, endMidX - startConnection.getX(), true);
				}
			} else if(endMidX - startConnection.getX() != 0) {
				addWire(link, startConnection.getX(), startConnection.getY(), endMidX - startConnection.getX(), true);
			} else if(endMidY - startConnection.getY() != 0) {
				addWire(link, endMidX, startConnection.getY(), endMidY - startConnection.getY(), false);
			}
			
			runSim();
		}
		
		startConnection = null;
		endConnection = null;
		startPoint = null;
		draggedPoint = null;
		mouseMoved(e);
		repaint();
	}
	
	public void mouseDragged(MouseEvent e) {
		if(startPoint != null) {
			int startX = (int)(startPoint.getX() < draggedPoint.getX() ? startPoint.getX() : draggedPoint.getX());
			int startY = (int)(startPoint.getY() < draggedPoint.getY() ? startPoint.getY() : draggedPoint.getY());
			int width = (int)Math.abs(draggedPoint.getX() - startPoint.getX());
			int height = (int)Math.abs(draggedPoint.getY() - startPoint.getY());
			
			selectedElements =
					Stream.concat(componentPeers.stream(),
							allLinkWires.stream().flatMap(link -> link.getWires().stream()))
							.filter(peer -> peer.intersectsScreenCoord(startX, startY, width, height)).collect(Collectors.toSet());
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
			
			draggedPoint = new Point2D(e.getX(), e.getY());
			endConnection = findConnection(
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
		
		Connection selected = findConnection(GuiUtils.getCircuitCoord(e.getX()), GuiUtils.getCircuitCoord(e.getY()));
		
		if(selected != startConnection) {
			startConnection = selected;
			repaint = true;
		}
		
		if(repaint) repaint();
	}
	
	private Connection findConnection(int x, int y) {
		Optional<Connection> optionalSelected =
				Stream.concat(
						allLinkWires.stream()
								.flatMap(link -> link.getWires().stream())
								.flatMap(wire -> wire.getConnections().stream()),
						componentPeers.stream().flatMap(peer -> peer.getConnections().stream()))
						.filter(c -> c.getX() == x && c.getY() == y).findAny();
		
		return optionalSelected.orElse(null);
	}
}
