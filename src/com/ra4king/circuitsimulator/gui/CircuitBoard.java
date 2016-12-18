package com.ra4king.circuitsimulator.gui;

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
		if((badLinks = links.stream().filter(link -> !link.isLinkGood()).collect(Collectors.toSet())).size() != 0) {
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
		for(Connection connection : component.getConnections()) {
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
						links.add(linkWires);
					} else if(links.remove(linkWires)) {
						handleConnection(connection, linkWires);
					}
				}
			}
			
			addConnection(connection);
		}
		
		components.add(component);
		
		runSim();
	}
	
	public void moveComponent(ComponentPeer<?> componentPeer, int x, int y) {
		removeComponent(componentPeer);
		componentPeer.setX(x);
		componentPeer.setY(y);
		addComponent(componentPeer);
	}
	
	private boolean wireAlreadyExists(Wire wire) {
		Set<Connection> connections = connectionsMap.get(new Pair<>(wire.getX(), wire.getY()));
		if(connections == null || connections.isEmpty()) {
			return false;
		}
		
		for(Connection connection : connections) {
			if(connection instanceof WireConnection) {
				Wire existing = (Wire)connection.getParent();
				if(wire.isWithin(existing)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void addWire(int x, int y, int length, boolean horizontal) {
		LinkWires linkWires = new LinkWires();
		
		// these are wires that would be split in half
		Set<Wire> toReAdd = new HashSet<>();
		Set<Wire> wiresAdded = new HashSet<>();
		
		{
			Connection lastConnection = findConnection(x, y);
			if(lastConnection != null) {
				handleConnection(lastConnection, linkWires);
				
				GuiElement parent = lastConnection.getParent();
				if(lastConnection instanceof WireConnection &&
						   lastConnection != parent.getConnections().get(0) &&
						   lastConnection != parent.getConnections().get(parent.getConnections().size() - 1)) {
					toReAdd.add((Wire)parent);
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
					   currConnection == currConnection.getParent().getConnections().get(0) ||
					   currConnection == currConnection.getParent().getConnections().get(currConnection.getParent()
							                                                                     .getConnections().size() - 1))) {
				int len = horizontal ? currConnection.getX() - lastX
						          : currConnection.getY() - lastY;
				Wire wire = new Wire(linkWires, lastX, lastY, len, horizontal);
				if(!wireAlreadyExists(wire)) {
					linkWires.addWire(wire);
					wiresAdded.add(wire);
					
					GuiElement parent = currConnection.getParent();
					if(currConnection instanceof WireConnection &&
							   currConnection != parent.getConnections().get(0) &&
							   currConnection != parent.getConnections().get(parent.getConnections().size() - 1)) {
						toReAdd.add((Wire)parent);
					}
					
					handleConnection(currConnection, linkWires);
					lastX = currConnection.getX();
					lastY = currConnection.getY();
				}
			} else if(i == length) {
				int len = horizontal ? x + xOff - lastX
						          : y + yOff - lastY;
				Wire wire = new Wire(linkWires, lastX, lastY, len, horizontal);
				if(!wireAlreadyExists(wire)) {
					linkWires.addWire(wire);
					wiresAdded.add(wire);
				}
			}
		}
		
		for(Wire wire : wiresAdded) {
			wire.getConnections().forEach(this::addConnection);
		}
		
		toReAdd.forEach(w -> {
			removeWire(w);
			addWire(w.getX(), w.getY(), w.getLength(), w.isHorizontal());
		});
		
		runSim();
	}
	
	public void removeComponent(ComponentPeer<?> component) {
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
		
		runSim();
	}
	
	public void removeWire(Wire wire) {
		LinkWires linkWires = wire.getLinkWires();
		if(linkWires == null || !links.contains(linkWires)) {
			return;
		}
		
		links.remove(linkWires);
		links.addAll(linkWires.removeWire(wire));
		
		wire.getConnections().forEach(this::removeConnection);
		
		runSim();
	}
	
	private void handleConnection(Connection connection, LinkWires linkWires) {
		LinkWires selectedLink = connection.getLinkWires();
		if(selectedLink == null) {
			if(connection instanceof PortConnection) {
				linkWires.addPort((PortConnection)connection);
			} else if(connection instanceof WireConnection) {
				linkWires.addWire((Wire)connection.getParent());
			}
		} else {
			links.remove(selectedLink);
			linkWires.merge(selectedLink);
		}
		
		links.add(linkWires);
	}
	
	public Connection findConnection(int x, int y) {
		Pair<Integer, Integer> pair = new Pair<>(x, y);
		return connectionsMap.containsKey(pair) ? connectionsMap.get(pair).iterator().next() : null;
	}
	
	public void paint(GraphicsContext graphics) {
		for(ComponentPeer<?> peer : components) {
			graphics.save();
			peer.paint(graphics, circuit.getTopLevelState());
			graphics.restore();
			
			for(Connection connection : peer.getConnections()) {
				graphics.save();
				connection.paint(graphics, circuit.getTopLevelState());
				graphics.restore();
			}
		}
		
		links.forEach(linkWire -> {
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
		
		if(badLinks != null) {
			for(LinkWires linkWires : badLinks) {
				Stream.concat(linkWires.getPorts().stream(), linkWires.getBadPorts().stream()).forEach(port -> {
					graphics.setStroke(Color.BLACK);
					graphics.strokeText(
							String.valueOf(port.getLink().getBitSize()), port.getScreenX() + 11, port.getScreenY() +
									                                                                     21);
					
					graphics.setStroke(Color.ORANGE);
					graphics.strokeOval(port.getScreenX() - 2, port.getScreenY() - 2, 10, 10);
					graphics.strokeText(
							String.valueOf(port.getLink().getBitSize()), port.getScreenX() + 10, port.getScreenY() +
									                                                                     20);
				});
			}
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
			throw new IllegalArgumentException("There is no connection here in the first place!");
		}
		Set<Connection> set = connectionsMap.get(pair);
		set.remove(connection);
		if(set.isEmpty()) {
			connectionsMap.remove(pair);
		}
	}
}
