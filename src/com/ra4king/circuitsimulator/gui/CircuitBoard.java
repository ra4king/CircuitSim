package com.ra4king.circuitsimulator.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentCreator;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.ShortCircuitException;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class CircuitBoard {
	private Circuit circuit;
	
	private Set<ComponentPeer<?>> components;
	private Set<LinkWires> links;
	private Set<LinkWires> badLinks;
	
	private Set<GuiElement> moveElements;
	
	private Map<Pair<Integer, Integer>, Set<Connection>> connectionsMap;
	
	public CircuitBoard(Simulator simulator) {
		this.circuit = new Circuit(simulator);
		
		components = new HashSet<>();
		links = new HashSet<>();
		
		connectionsMap = new HashMap<>();
	}
	
	public Circuit getCircuit() {
		return circuit;
	}
	
	public Set<ComponentPeer<?>> getComponents() {
		return components;
	}
	
	public Set<LinkWires> getLinks() {
		return links;
	}
	
	public void runSim() {
		if((badLinks = links.stream().filter(link -> !link.isLinkValid()).collect(Collectors.toSet())).size() != 0) {
			return;
		}
		
		try {
			circuit.getSimulator().stepAll();
		}
		catch(ShortCircuitException exc) {
			exc.printStackTrace();
		}
	}
	
	public void createComponent(ComponentCreator creator, int x, int y) {
		ComponentPeer<?> component = creator.createComponent(circuit, x, y);
		
		for(ComponentPeer<?> c : components) {
			if(c.intersects(component)) {
				circuit.removeComponent(component.getComponent());
				return;
			}
		}
		
		addComponent(component);
	}
	
	public void addComponent(ComponentPeer<?> component) {
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
		
		components.add(component);
		
		toReAdd.forEach(wire -> {
			removeWire(wire);
			addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
		});
		
		rejoinWires();
		
		runSim();
	}
	
	public void initMove(Set<GuiElement> elements) {
		if(moveElements != null) {
			finalizeMove();
		}
		
		moveElements = elements;
		removeElements(elements);
	}
	
	public void moveElements(int dx, int dy) {
		for(GuiElement element : moveElements) {
			element.setX(element.getX() + dx);
			element.setY(element.getY() + dy);
		}
		
		// TODO: Add wires to attach connections
	}
	
	public void finalizeMove() {
		for(GuiElement element : moveElements) {
			if(element instanceof ComponentPeer<?>) {
				addComponent((ComponentPeer<?>)element);
			} else if(element instanceof Wire) {
				Wire wire = (Wire)element;
				addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
			}
		}
		
		moveElements = null;
	}
	
	public void removeElements(Set<GuiElement> elements) {
		Map<LinkWires, Set<Wire>> wiresToRemove = new HashMap<>();
		
		for(GuiElement element : elements) {
			if(element instanceof ComponentPeer<?>) {
				removeComponent((ComponentPeer<?>)element);
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
				
				Set<Wire> set = wiresToRemove.containsKey(linkWires) ? wiresToRemove.get(linkWires) : new HashSet<>();
				set.add(wire);
				wiresToRemove.put(linkWires, set);
			}
		}
		
		wiresToRemove.forEach((linkWires, wires) -> {
			links.remove(linkWires);
			links.addAll(linkWires.splitWires(wires));
		});
		
		rejoinWires();
		
		runSim();
	}
	
	public void addWire(int x, int y, int length, boolean horizontal) {
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
			                              currConnection == ((Wire)currConnection.getParent()).getStartConnection() ||
			                              currConnection == ((Wire)currConnection.getParent()).getEndConnection())) {
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
		addWire(links, new Wire(links, connection.getX(), connection.getY(), wire.getLength() - len, wire.isHorizontal()));
	}
	
	private void addWire(LinkWires linkWires, Wire wire) {
		linkWires.addWire(wire);
		links.add(linkWires);
		wire.getConnections().forEach(this::addConnection);
	}
	
	private void removeWire(Wire wire) {
		wire.getConnections().forEach(this::removeConnection);
		
		LinkWires linkWires = wire.getLinkWires();
		if(linkWires == null) {
			return;
		}
		
		linkWires.removeWire(wire);
	}
	
	private void rejoinWires() {
		Set<Boolean> changed = new HashSet<>();
		
		links.forEach(linkWires -> {
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
					changed.add(true);
				}
			}
		});
		
		if(!changed.isEmpty()) {
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
		components.remove(component);
		circuit.removeComponent(component.getComponent());
	}
	
	private void handleConnection(Connection connection, LinkWires linkWires) {
		LinkWires linksToMerge = connection.getLinkWires();
		if(linksToMerge == null) {
			if(connection instanceof PortConnection) {
				linkWires.addPort((PortConnection)connection);
			} else if(connection instanceof WireConnection) {
				linkWires.addWire((Wire)connection.getParent());
			}
		} else {
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
		return connectionsMap.containsKey(pair) ? connectionsMap.get(pair) : null;
	}
	
	public void paint(GraphicsContext graphics) {
		components.forEach(peer -> {
			graphics.save();
			peer.paint(graphics, circuit.getTopLevelState());
			graphics.restore();
			
			for(Connection connection : peer.getConnections()) {
				graphics.save();
				connection.paint(graphics, circuit.getTopLevelState());
				graphics.restore();
			}
		});
		
		links.forEach(linkWire -> {
			graphics.save();
			linkWire.paint(graphics, circuit.getTopLevelState());
			graphics.restore();
			
			for(Wire wire : linkWire.getWires()) {
				graphics.save();
				wire.paint(graphics, circuit.getTopLevelState());
				graphics.restore();
				
				graphics.save();
				wire.getStartConnection().paint(graphics, circuit.getTopLevelState());
				graphics.restore();
				
				graphics.save();
				wire.getEndConnection().paint(graphics, circuit.getTopLevelState());
				graphics.restore();
			}
		});
		
		if(badLinks != null) {
			badLinks.forEach(badLink -> {
				Stream.concat(badLink.getPorts().stream(), badLink.getInvalidPorts().stream()).forEach(port -> {
					graphics.setStroke(Color.BLACK);
					graphics.strokeText(
							String.valueOf(port.getLinkWires().getLink().getBitSize()), port.getScreenX() + 11, port.getScreenY() +
							                                                                     21);
					
					graphics.setStroke(Color.ORANGE);
					graphics.strokeOval(port.getScreenX() - 2, port.getScreenY() - 2, 10, 10);
					graphics.strokeText(
							String.valueOf(port.getLinkWires().getLink().getBitSize()), port.getScreenX() + 10, port.getScreenY() +
							                                                                     20);
				});
			});
		}
		
		if(moveElements != null) {
			moveElements.forEach(element -> element.paint(graphics, circuit.getTopLevelState()));
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
