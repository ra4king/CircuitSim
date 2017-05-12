package com.ra4king.circuitsimulator.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.gui.EditHistory.EditAction;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Simulator;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class CircuitBoard {
	private CircuitManager circuitManager;
	private Circuit circuit;
	private CircuitState currentState;
	
	private Set<ComponentPeer<?>> components;
	private Set<LinkWires> links;
	private Set<LinkWires> badLinks;
	
	private Set<GuiElement> moveElements;
	private int moveDeltaX, moveDeltaY;
	
	private Map<Pair<Integer, Integer>, Set<Connection>> connectionsMap;
	
	private EditHistory editHistory;
	
	public CircuitBoard(CircuitManager circuitManager, Simulator simulator, EditHistory editHistory) {
		this.circuitManager = circuitManager;
		
		circuit = new Circuit(simulator);
		currentState = circuit.getTopLevelState();
		
		this.editHistory = editHistory;
		
		components = new HashSet<>();
		links = new HashSet<>();
		
		connectionsMap = new HashMap<>();
	}
	
	public void destroy() {
		try {
			removeElements(
					Stream.concat(components.stream(),
					              links.stream()
					                   .flatMap(links -> links.getWires().stream())).collect(Collectors.toSet()));
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		
		circuit.getSimulator().removeCircuit(circuit);
	}
	
	public Circuit getCircuit() {
		return circuit;
	}
	
	public CircuitState getCurrentState() {
		return currentState;
	}
	
	public void setCurrentState(CircuitState state) {
		if(currentState == null) {
			throw new NullPointerException("CircuitState cannot be null");
		}
		
		currentState = state;
	}
	
	public Set<ComponentPeer<?>> getComponents() {
		return components;
	}
	
	public Set<LinkWires> getLinks() {
		return links;
	}
	
	public void runSim() throws Exception {
		if(Platform.isFxApplicationThread()) {
			if((badLinks = links.stream().filter(link -> !link.isLinkValid()).collect(
					Collectors.toSet())).size() != 0) {
				throw badLinks.iterator().next().getLastException();
			}
			
			circuit.getSimulator().stepAll();
		} else {
			throw new IllegalStateException("Not running on FX thread!");
		}
	}
	
	public boolean isValidLocation(ComponentPeer<?> component) {
		for(ComponentPeer<?> c : components) {
			if(c != component && c.getX() == component.getX() && c.getY() == component.getY()) {
				return false;
			}
		}
		
		return true;
	}
	
	public void addComponent(ComponentPeer<?> component) throws Exception {
		if(!isValidLocation(component)) {
			throw new IllegalStateException("Cannot place component here.");
		}
		
		// Component must be added before added to the circuit as listeners will be triggered to recreate Subcircuits
		components.add(component);
		
		try {
			circuit.addComponent(component.getComponent());
		} catch(Exception exc) {
			components.remove(component);
			throw exc;
		}
		
		try {
			editHistory.beginGroup();
			editHistory.addAction(EditAction.ADD_COMPONENT, circuitManager, component);
			
			Set<Wire> toReAdd = new HashSet<>();
			
			for(Connection connection : component.getConnections()) {
				Set<Connection> connections = getConnections(connection.getX(), connection.getY());
				if(connections != null) {
					for(Connection attached : connections) {
						LinkWires linkWires = attached.getLinkWires();
						linkWires.addPort((PortConnection)connection);
						links.add(linkWires);
						
						if(attached instanceof WireConnection) {
							Wire wire = (Wire)attached.getParent();
							if(attached != wire.getStartConnection() && attached != wire.getEndConnection()) {
								toReAdd.add(wire);
							}
						}
					}
				}
				
				addConnection(connection);
			}
			
			for(Wire wire : toReAdd) {
				removeWire(wire);
				try {
					addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			}
			
			rejoinWires();
			
			runSim();
		} finally {
			editHistory.endGroup();
		}
	}
	
	public void updateComponent(ComponentPeer<?> oldComponent, ComponentPeer<?> newComponent) throws Exception {
		try {
			editHistory.disable();
			
			removeComponent(oldComponent);
			
			try {
				circuit.updateComponent(oldComponent.getComponent(), newComponent.getComponent(),
				                        () -> components.add(newComponent));
			} catch(Exception exc) {
				components.remove(newComponent);
				throw exc;
			}
			
			addComponent(newComponent);
		} finally {
			editHistory.enable();
			editHistory.addAction(EditAction.UPDATE_COMPONENT, circuitManager, oldComponent, newComponent);
		}
	}
	
	public void initMove(Set<GuiElement> elements) throws Exception {
		if(moveElements != null) {
			try {
				finalizeMove();
			} catch(Exception exc) {
				exc.printStackTrace();
			}
		}
		
		try {
			editHistory.disable();
			moveElements = elements;
			removeElements(elements, false);
		} finally {
			editHistory.enable();
		}
	}
	
	public void moveElements(int dx, int dy) {
		for(GuiElement element : moveElements) {
			element.setX(element.getX() + dx);
			element.setY(element.getY() + dy);
		}
		
		moveDeltaX += dx;
		moveDeltaY += dy;
		
		// TODO: Add wires to attach connections
	}
	
	public void finalizeMove() throws Exception {
		if(moveElements == null) {
			return;
		}
		
		editHistory.disable();
		
		for(GuiElement element : moveElements) {
			if(element instanceof ComponentPeer<?> && !isValidLocation((ComponentPeer<?>)element)) {
				for(GuiElement element1 : moveElements) {
					element1.setX(element1.getX() - moveDeltaX);
					element1.setY(element1.getY() - moveDeltaY);
				}
				
				break;
			}
		}
		
		Exception toThrow = null;
		for(GuiElement element : moveElements) {
			if(element instanceof ComponentPeer<?>) {
				try {
					addComponent((ComponentPeer<?>)element);
				} catch(Exception exc) {
					toThrow = exc;
				}
			} else if(element instanceof Wire) {
				Wire wire = (Wire)element;
				try {
					addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
				} catch(Exception exc) {
					toThrow = exc;
				}
			}
		}
		
		editHistory.enable();
		editHistory.addAction(EditAction.MOVE_ELEMENTS, circuitManager,
		                      new HashSet<>(moveElements), moveDeltaX, moveDeltaY);
		
		moveElements = null;
		moveDeltaX = 0;
		moveDeltaY = 0;
		
		runSim();
		
		if(toThrow != null) {
			throw toThrow;
		}
	}
	
	public void removeElements(Set<? extends GuiElement> elements) throws Exception {
		removeElements(elements, true);
	}
	
	private void removeElements(Set<? extends GuiElement> elements, boolean removeFromCircuit) throws Exception {
		try {
			editHistory.beginGroup();
			
			Map<LinkWires, Set<Wire>> wiresToRemove = new HashMap<>();
			
			for(GuiElement element : elements) {
				if(element instanceof ComponentPeer<?>) {
					removeComponent((ComponentPeer<?>)element);
					if(removeFromCircuit) {
						circuit.removeComponent(((ComponentPeer<?>)element).getComponent());
					}
				} else if(element instanceof Wire) {
					Wire wire = (Wire)element;
					if(!links.contains(wire.getLinkWires())) {
						outer:
						for(LinkWires link : links) {
							for(Wire w : link.getWires()) {
								if(w.equals(wire)) {
									wire = w;
									break outer;
								}
							}
						}
						
						if(wire == element) {
							continue;
						}
					}
					
					wire.getConnections().forEach(this::removeConnection);
					
					LinkWires linkWires = wire.getLinkWires();
					if(linkWires == null) {
						continue;
					}
					
					Set<Wire> set = wiresToRemove.containsKey(linkWires)
					                ? wiresToRemove.get(linkWires)
					                : new HashSet<>();
					set.add(wire);
					wiresToRemove.put(linkWires, set);
					
					editHistory.addAction(EditAction.REMOVE_WIRE, circuitManager, wire);
				}
			}
			
			wiresToRemove.forEach((linkWires, wires) -> {
				links.remove(linkWires);
				links.addAll(linkWires.splitWires(wires));
			});
			
			rejoinWires();
			
			runSim();
		} finally {
			editHistory.endGroup();
		}
	}
	
	public void addWire(int x, int y, int length, boolean horizontal) throws Exception {
		try {
			editHistory.beginGroup();
			
			LinkWires linkWires = new LinkWires();
			
			Set<Wire> wiresToAdd = new HashSet<>();
			
			// these are wires that would be split in half
			Map<Wire, Connection> toSplit = new HashMap<>();
			
			{
				Set<Connection> connections = getConnections(x, y);
				if(connections != null) {
					for(Connection connection : connections) {
						handleConnection(connection, linkWires);
						
						GuiElement parent = connection.getParent();
						if(connection instanceof WireConnection) {
							Wire wire = (Wire)parent;
							if(connection != wire.getStartConnection() && connection != wire.getEndConnection()) {
								toSplit.put(wire, connection);
							}
						}
					}
				}
			}
			
			int lastX = x;
			int lastY = y;
			
			int sign = length / Math.abs(length);
			for(int i = sign; Math.abs(i) <= Math.abs(length); i += sign) {
				int xOff = horizontal ? i : 0;
				int yOff = horizontal ? 0 : i;
				Connection currConnection = findConnection(x + xOff, y + yOff);
				
				if(currConnection != null && (i == length ||
						                              currConnection instanceof PortConnection ||
						                              currConnection == ((Wire)currConnection.getParent())
								                                                .getStartConnection() ||
						                              currConnection == ((Wire)currConnection.getParent())
								                                                .getEndConnection())) {
					int len = horizontal ? currConnection.getX() - lastX
					                     : currConnection.getY() - lastY;
					Wire wire = new Wire(linkWires, lastX, lastY, len, horizontal);
					Wire surrounding = wireAlreadyExists(wire);
					if(surrounding == null) {
						wiresToAdd.add(wire);
					}
					
					Set<Connection> connections = i == length ? getConnections(x + xOff, y + yOff)
					                                          : Collections.singleton(currConnection);
					
					for(Connection connection : connections) {
						GuiElement parent = connection.getParent();
						if(connection instanceof WireConnection) {
							Wire connWire = (Wire)parent;
							if(connection != connWire.getStartConnection() &&
									   connection != connWire.getEndConnection()) {
								toSplit.put((Wire)parent, connection);
							}
						}
						
						handleConnection(connection, linkWires);
					}
					
					lastX = currConnection.getX();
					lastY = currConnection.getY();
				} else if(i == length) {
					int len = horizontal ? x + xOff - lastX
					                     : y + yOff - lastY;
					Wire wire = new Wire(linkWires, lastX, lastY, len, horizontal);
					Wire surrounding = wireAlreadyExists(wire);
					if(surrounding == null) {
						wiresToAdd.add(wire);
					}
					
					lastX = x + xOff;
					lastY = y + yOff;
				}
			}
			
			for(Wire wire : wiresToAdd) {
				addWire(linkWires, wire);
			}
			
			toSplit.forEach(this::splitWire);
			
			rejoinWires();
			
			runSim();
		} finally {
			editHistory.endGroup();
		}
	}
	
	private Wire wireAlreadyExists(Wire wire) {
		Set<Connection> connections = connectionsMap.get(new Pair<>(wire.getX(), wire.getY()));
		if(connections == null || connections.isEmpty()) {
			return null;
		}
		
		for(Connection connection : connections) {
			if(connection instanceof WireConnection) {
				Wire existing = (Wire)connection.getParent();
				if(wire.isWithin(existing)) {
					return existing;
				}
			}
		}
		
		return null;
	}
	
	private void splitWire(Wire wire, Connection connection) {
		LinkWires links = wire.getLinkWires();
		removeWire(wire);
		
		int len = connection.getX() == wire.getX() ? connection.getY() - wire.getY()
		                                           : connection.getX() - wire.getX();
		addWire(links, new Wire(links, wire.getX(), wire.getY(), len, wire.isHorizontal()));
		addWire(links,
		        new Wire(links, connection.getX(), connection.getY(), wire.getLength() - len, wire.isHorizontal()));
	}
	
	private void addWire(LinkWires linkWires, Wire wire) {
		linkWires.addWire(wire);
		links.add(linkWires);
		wire.getConnections().forEach(this::addConnection);
		
		editHistory.addAction(EditAction.ADD_WIRE, circuitManager, wire);
	}
	
	private void removeWire(Wire wire) {
		wire.getConnections().forEach(this::removeConnection);
		
		LinkWires linkWires = wire.getLinkWires();
		if(linkWires == null) {
			return;
		}
		
		linkWires.removeWire(wire);
		
		editHistory.addAction(EditAction.REMOVE_WIRE, circuitManager, wire);
	}
	
	private void rejoinWires() {
		boolean changed = false;
		
		for(LinkWires linkWires : links) {
			Set<Wire> removed = new HashSet<>();
			for(Wire wire : new HashSet<>(linkWires.getWires())) {
				if(removed.contains(wire)) continue;
				
				Connection start = wire.getStartConnection();
				Connection end = wire.getEndConnection();
				
				int x = wire.getX();
				int y = wire.getY();
				int length = wire.getLength();
				
				Set<Connection> startConns = getConnections(start.getX(), start.getY());
				if(startConns != null && startConns.size() == 2) {
					List<Wire> startWires = startConns.stream()
					                                  .filter(conn -> conn != start && conn instanceof WireConnection)
					                                  .map(conn -> (Wire)conn.getParent())
					                                  .filter(w -> w.isHorizontal() == wire.isHorizontal())
					                                  .collect(Collectors.toList());
					
					if(startWires.size() == 1) {
						Wire startWire = startWires.get(0);
						length += startWire.getLength();
						
						if(startWire.getX() < x) {
							x = startWire.getX();
						}
						if(startWire.getY() < y) {
							y = startWire.getY();
						}
						
						removeWire(startWire);
						removed.add(startWire);
					}
				}
				
				Set<Connection> endConns = getConnections(end.getX(), end.getY());
				if(endConns != null && endConns.size() == 2) {
					List<Wire> endWires = endConns.stream()
					                              .filter(conn -> conn != end && conn instanceof WireConnection)
					                              .map(conn -> (Wire)conn.getParent())
					                              .filter(w -> w.isHorizontal() == wire.isHorizontal())
					                              .collect(Collectors.toList());
					
					if(endWires.size() == 1) {
						Wire endWire = endWires.get(0);
						length += endWire.getLength();
						
						removeWire(endWire);
						removed.add(endWire);
					}
				}
				
				if(length != wire.getLength()) {
					removeWire(wire);
					removed.add(wire);
					addWire(linkWires, new Wire(linkWires, x, y, length, wire.isHorizontal()));
					changed = true;
				}
			}
		}
		
		if(changed) {
			rejoinWires();
		}
	}
	
	private void removeComponent(ComponentPeer<?> component) {
		for(Connection connection : component.getConnections()) {
			removeConnection(connection);
			
			PortConnection portConnection = (PortConnection)connection;
			LinkWires linkWires = portConnection.getLinkWires();
			if(linkWires != null) {
				linkWires.removePort(portConnection);
				if(linkWires.isEmpty()) {
					linkWires.clear();
					links.remove(linkWires);
				}
			}
		}
		if(!components.remove(component)) {
			throw new IllegalStateException("Couldn't find component!");
		}
		
		editHistory.addAction(EditAction.REMOVE_COMPONENT, circuitManager, component);
	}
	
	private void handleConnection(Connection connection, LinkWires linkWires) {
		LinkWires linksToMerge = connection.getLinkWires();
		if(linksToMerge == null) {
			if(connection instanceof PortConnection) {
				linkWires.addPort((PortConnection)connection);
			} else if(connection instanceof WireConnection) {
				linkWires.addWire((Wire)connection.getParent());
			}
		} else if(linkWires != linksToMerge) {
			links.remove(linksToMerge);
			linkWires.merge(linksToMerge);
		}
		
		links.add(linkWires);
	}
	
	public Connection findConnection(int x, int y) {
		Pair<Integer, Integer> pair = new Pair<>(x, y);
		return connectionsMap.containsKey(pair) ? connectionsMap.get(pair).iterator().next() : null;
	}
	
	public Set<Connection> getConnections(int x, int y) {
		Pair<Integer, Integer> pair = new Pair<>(x, y);
		return connectionsMap.containsKey(pair) ? connectionsMap.get(pair) : Collections.emptySet();
	}
	
	public void paint(GraphicsContext graphics) {
		components.forEach(component -> paintComponent(graphics, component));
		
		links.forEach(linkWire -> linkWire.getWires().forEach(wire -> paintWire(graphics, wire)));
		
		if(badLinks != null) {
			for(LinkWires badLink : badLinks) {
				Stream.concat(badLink.getPorts().stream(),
				              badLink.getInvalidPorts().stream()).forEach(port -> {
					graphics.setStroke(Color.BLACK);
					graphics.strokeText(String.valueOf(port.getPort().getLink().getBitSize()),
					                    port.getScreenX() + 11,
					                    port.getScreenY() + 21);
					
					graphics.setStroke(Color.ORANGE);
					graphics.strokeOval(port.getScreenX() - 2, port.getScreenY() - 2, 10, 10);
					graphics.strokeText(String.valueOf(port.getPort().getLink().getBitSize()),
					                    port.getScreenX() + 10,
					                    port.getScreenY() + 20);
				});
			}
		}
		
		if(moveElements != null) {
			graphics.save();
			graphics.setGlobalAlpha(0.5);
			
			for(GuiElement element : moveElements) {
				if(element instanceof ComponentPeer<?>) {
					paintComponent(graphics, (ComponentPeer<?>)element);
				} else if(element instanceof Wire) {
					paintWire(graphics, (Wire)element);
				}
			}
			
			graphics.restore();
		}
	}
	
	private void paintComponent(GraphicsContext graphics, ComponentPeer<?> component) {
		graphics.save();
		component.paint(graphics, currentState);
		graphics.restore();
		
		for(PortConnection connection : component.getConnections()) {
			graphics.save();
			connection.paint(graphics, currentState);
			graphics.restore();
		}
	}
	
	private void paintWire(GraphicsContext graphics, Wire wire) {
		graphics.save();
		wire.paint(graphics, currentState);
		graphics.restore();
		
		Connection startConn = wire.getStartConnection();
		if(getConnections(startConn.getX(), startConn.getY()).size() > 2) {
			graphics.save();
			startConn.paint(graphics, currentState);
			graphics.restore();
		}
		
		Connection endConn = wire.getStartConnection();
		if(getConnections(endConn.getX(), endConn.getY()).size() > 2) {
			graphics.save();
			endConn.paint(graphics, currentState);
			graphics.restore();
		}
	}
	
	private void addConnection(Connection connection) {
		Pair<Integer, Integer> pair = new Pair<>(connection.getX(), connection.getY());
		Set<Connection> set = connectionsMap.containsKey(pair) ? connectionsMap.get(pair) : new HashSet<>();
		set.add(connection);
		connectionsMap.put(pair, set);
	}
	
	private void removeConnection(Connection connection) {
		Pair<Integer, Integer> pair = new Pair<>(connection.getX(), connection.getY());
		if(!connectionsMap.containsKey(pair)) {
			return;
		}
		Set<Connection> set = connectionsMap.get(pair);
		set.remove(connection);
		if(set.isEmpty()) {
			connectionsMap.remove(pair);
		}
	}
}
