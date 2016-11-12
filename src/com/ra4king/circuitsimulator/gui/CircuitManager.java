package com.ra4king.circuitsimulator.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.peers.AdderPeer;
import com.ra4king.circuitsimulator.gui.peers.ClockPeer;
import com.ra4king.circuitsimulator.gui.peers.ControlledBufferPeer;
import com.ra4king.circuitsimulator.gui.peers.GatePeer;
import com.ra4king.circuitsimulator.gui.peers.MultiplexerPeer;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.gui.peers.RAMPeer;
import com.ra4king.circuitsimulator.gui.peers.RegisterPeer;
import com.ra4king.circuitsimulator.gui.peers.SplitterPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.ShortCircuitException;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Adder;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.components.ControlledBuffer;
import com.ra4king.circuitsimulator.simulator.components.Multiplexer;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.RAM;
import com.ra4king.circuitsimulator.simulator.components.Register;
import com.ra4king.circuitsimulator.simulator.components.Splitter;
import com.ra4king.circuitsimulator.simulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NorGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NotGate;
import com.ra4king.circuitsimulator.simulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

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
	private Set<LinkWires> allLinkWires;
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
	
	private int componentMode = 0;
	private int bitSize = 1;
	private int secondaryOption = 0;
	
	public CircuitManager(Canvas canvas, Simulator simulator) {
		this.canvas = canvas;
		circuit = new Circuit(simulator);
		
		componentPeers = new HashSet<>();
		allLinkWires = new HashSet<>();
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public Point2D getLastMousePosition() {
		return lastMousePosition;
	}
	
	public void setLastMousePosition(Point2D lastMousePosition) {
		this.lastMousePosition = lastMousePosition;
	}
	
	public void modifiedSelection(int componentMode, int bitSize, int secondaryOption) {
		this.componentMode = componentMode;
		this.bitSize = bitSize;
		this.secondaryOption = secondaryOption;
		
		dummyCircuit.getComponents().clear();
		potentialComponent = createComponent(dummyCircuit, (int)lastMousePosition.getX(), (int)lastMousePosition.getY());
		if(potentialComponent != null) {
			potentialComponent.setX(GuiUtils.getNearestCoord(potentialComponent.getX() - potentialComponent.getWidth() / 2));
			potentialComponent.setY(GuiUtils.getNearestCoord(potentialComponent.getY() - potentialComponent.getHeight() / 2));
		}
	}
	
	private ComponentPeer<?> createComponent(Circuit circuit, int x, int y) {
		switch(componentMode) {
			case 1:
				return new GatePeer(circuit.addComponent(new AndGate("", bitSize, Math.max(2, secondaryOption))), x, y);
			case 2:
				return new PinPeer(circuit.addComponent(new Pin("", bitSize, true)), x, y);
			case 3:
				return new PinPeer(circuit.addComponent(new Pin("", bitSize, false)), x, y);
			case 4:
				return new GatePeer(circuit.addComponent(new OrGate("", bitSize, Math.max(2, secondaryOption))), x, y);
			case 5:
				return new GatePeer(circuit.addComponent(new NorGate("", bitSize, Math.max(2, secondaryOption))), x, y);
			case 6:
				return new GatePeer(circuit.addComponent(new XorGate("", bitSize, Math.max(2, secondaryOption))), x, y);
			case 7:
				return new GatePeer(circuit.addComponent(new NotGate("", bitSize)), x, y);
			case 8:
				return new ControlledBufferPeer(circuit.addComponent(new ControlledBuffer("", bitSize)), x, y);
			case 9:
				return new ClockPeer(circuit.addComponent(new Clock("")), x, y);
			case 10:
				return new RegisterPeer(circuit.addComponent(new Register("", bitSize)), x, y);
			case 11:
				return new AdderPeer(circuit.addComponent(new Adder("", bitSize)), x, y);
			case 12:
				return new SplitterPeer(circuit.addComponent(new Splitter("", bitSize, secondaryOption)), x, y);
			case 13:
				return new MultiplexerPeer(circuit.addComponent(new Multiplexer("", bitSize, secondaryOption)), x, y);
			case 14:
				return new RAMPeer(circuit.addComponent(new RAM("", bitSize, secondaryOption)), x, y);
		}
		
		return null;
	}
	
	public void runSim() {
		if((badLinks = allLinkWires.stream().filter(link -> !link.isLinkGood()).collect(Collectors.toSet())).size() != 0) {
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
			graphics.strokeOval(startConnection.getX() - 2, startConnection.getY() - 2, 10, 10);
			
			if(endConnection != null) {
				graphics.strokeOval(endConnection.getX() - 2, endConnection.getY() - 2, 10, 10);
			}
			
			if(draggedPoint != null) {
				int pointX = GuiUtils.getNearestCoord(draggedPoint.getX());
				int pointY = GuiUtils.getNearestCoord(draggedPoint.getY());
				graphics.setStroke(Color.BLACK);
				int selectedMidX = startConnection.getX() + startConnection.getWidth() / 2;
				int selectedMidY = startConnection.getY() + startConnection.getHeight() / 2;
				if(isDraggedHorizontally) {
					graphics.strokeLine(selectedMidX, selectedMidY, pointX, selectedMidY);
					graphics.strokeLine(pointX, selectedMidY, pointX, pointY);
				} else {
					graphics.strokeLine(selectedMidX, selectedMidY, selectedMidX, pointY);
					graphics.strokeLine(selectedMidX, pointY, pointX, pointY);
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
					graphics.strokeText(String.valueOf(port.getLink().getBitSize()), port.getX() + 11, port.getY() + 
							                                                                                   21);
					
					graphics.setStroke(Color.ORANGE);
					graphics.strokeOval(port.getX() - 2, port.getY() - 2, 10, 10);
					graphics.strokeText(String.valueOf(port.getLink().getBitSize()), port.getX() + 10, port.getY() + 
							                                                                                   20);
				});
			}
		}
		
		for(GuiElement selectedElement : selectedElements) {
			graphics.setStroke(Color.RED);
			if(selectedElement instanceof Wire) {
				graphics.strokeRect(selectedElement.getX() - 2, selectedElement.getY() - 2,
						selectedElement.getWidth(), selectedElement.getHeight());
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
				if(selectedElements.size() == 1 && (selectedElem = selectedElements.iterator().next()) instanceof
						                                   PinPeer) {
					PinPeer selectedPin = (PinPeer)selectedElem;
					WireValue currentValue = new WireValue(circuit.getTopLevelState()
							                                       .getMergedValue(selectedPin
									                                                       .getComponent()
									                                                       .getPort(Pin.PORT)));
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
			
			ComponentPeer<?> peer = createComponent(circuit, potentialComponent.getX(), potentialComponent.getY());
			
			if(peer != null) {
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
			}
			
			runSim();
		} else {
			startPoint = new Point2D(e.getX(), e.getY());
			draggedPoint = new Point2D(e.getX(), e.getY());
			
			Optional<GuiElement> clickedComponent = Stream.concat(componentPeers.stream(), allLinkWires.stream().flatMap(link -> link.getWires().stream()))
					                                        .filter(peer -> peer.contains((int)e.getX(), (int)e.getY()))
					                                        .findAny();
			if(clickedComponent.isPresent()) {
				GuiElement selectedElement = clickedComponent.get();
				selectedElements.clear();
				selectedElements.add(selectedElement);
				if(selectedElement instanceof PinPeer && ((PinPeer)selectedElement).isInput()) {
					Pin pin = ((PinPeer)selectedElement).getComponent();
					WireValue value = circuit.getTopLevelState().getLastPushedValue(pin.getPort(Pin.PORT));
					if(value.getBitSize() == 1) {
						pin.setValue(circuit.getTopLevelState(), new WireValue(1, value.getBit(0) == State.ONE ? State.ZERO : State.ONE));
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
	
	public void mouseReleased(MouseEvent e) {
		if(draggedPoint != null && startConnection != null) {
			LinkWires link = new LinkWires();
			
			boolean createLink = true;
			
			int selectedMidX = startConnection.getX() + startConnection.getWidth() / 2;
			int selectedMidY = startConnection.getY() + startConnection.getHeight() / 2;
			int endMidX = endConnection == null ? GuiUtils.getNearestCoord(draggedPoint.getX()) : endConnection.getX() + endConnection.getWidth() / 2;
			int endMidY = endConnection == null ? GuiUtils.getNearestCoord(draggedPoint.getY()) : endConnection.getY() + endConnection.getHeight() / 2;
			
			if(endMidX - selectedMidX != 0 && endMidY - selectedMidY != 0) {
				if(isDraggedHorizontally) {
					link.addWire(new Wire(link, selectedMidX, selectedMidY, endMidX - selectedMidX, true));
					link.addWire(new Wire(link, endMidX, selectedMidY, endMidY - selectedMidY, false));
				} else {
					link.addWire(new Wire(link, selectedMidX, selectedMidY, endMidY - selectedMidY, false));
					link.addWire(new Wire(link, selectedMidX, endMidY, endMidX - selectedMidX, true));
				}
			} else if(Math.abs(endMidX - selectedMidX) >= GuiUtils.BLOCK_SIZE) {
				link.addWire(new Wire(link, selectedMidX, selectedMidY, endMidX - selectedMidX, true));
			} else if(Math.abs(endMidY - selectedMidY) >= GuiUtils.BLOCK_SIZE) {
				link.addWire(new Wire(link, endMidX, selectedMidY, endMidY - selectedMidY, false));
			} else {
				createLink = false;
			}
			
			if(createLink) {
				handleConnection(startConnection, link);
				
				if(endConnection != null) {
					handleConnection(endConnection, link);
				}
				
				runSim();
			}
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
							.filter(peer -> peer.intersects(startX, startY, width, height)).collect(Collectors.toSet());
		}
		
		if(draggedPoint != null) {
			if(startConnection != null) {
				int currDiffX = (GuiUtils.getNearestCoord(e.getX()) - startConnection.getX())/GuiUtils.BLOCK_SIZE;
				int prevDiffX = (int)(draggedPoint.getX() - startConnection.getX())/GuiUtils.BLOCK_SIZE;
				int currDiffY = (GuiUtils.getNearestCoord(e.getY()) - startConnection.getY())/GuiUtils.BLOCK_SIZE;
				int prevDiffY = (int)(draggedPoint.getY() - startConnection.getY())/GuiUtils.BLOCK_SIZE;
				
				if(currDiffX == 0 || prevDiffX == 0 || currDiffX / Math.abs(currDiffX) != prevDiffX / Math.abs(prevDiffX)) {
					isDraggedHorizontally = false;
				}
				
				if(currDiffY == 0 || prevDiffY == 0 || currDiffY / Math.abs(currDiffY) != prevDiffY / Math.abs(prevDiffY)) {
					isDraggedHorizontally = true;
				}
			}
			
			draggedPoint = new Point2D(e.getX(), e.getY());
			endConnection = findConnection(GuiUtils.getNearestCoord(draggedPoint.getX()), GuiUtils.getNearestCoord(draggedPoint.getY()));
		}
		
		repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		boolean repaint = false;
		lastMousePosition = new Point2D(GuiUtils.getNearestCoord(e.getX()), GuiUtils.getNearestCoord(e.getY()));
		if(potentialComponent != null) {
			potentialComponent.setX(GuiUtils.getNearestCoord(e.getX() - potentialComponent.getWidth() / 2));
			potentialComponent.setY(GuiUtils.getNearestCoord(e.getY() - potentialComponent.getHeight() / 2));
			repaint = true;
		}
		
		Connection selected = findConnection((int)e.getX(), (int)e.getY());
		
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
						.filter(c -> c.contains(x, y)).findAny();
		
		if(optionalSelected.isPresent()) {
			return optionalSelected.get();
		} else {
			return null;
		}
	}
}
