package com.ra4king.circuitsimulator.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class LinkWires {
	private Set<PortConnection> ports;
	private Set<Wire> wires;
	
	private Set<PortConnection> badPorts;
	
	public LinkWires() {
		ports = new HashSet<>();
		wires = new HashSet<>();
		
		badPorts = new HashSet<>();
	}
	
	public boolean isLinkGood() {
		return badPorts.size() == 0;
	}
	
	public boolean isEmpty() {
		return ports.size() == 0 && wires.size() == 0 && badPorts.size() == 0;
	}
	
	public Link getLink() {
		return ports.size() > 0 ? ports.iterator().next().getLink() : null;
	}
	
	public void addWire(Wire wire) {
		wire.setLinkWires(this);
		wires.add(wire);
	}
	
	public Set<Wire> getWires() {
		return wires;
	}
	
	public List<LinkWires> removeWire(Wire wire) {
		if(!wires.contains(wire)) {
			return Collections.singletonList(this);
		}
		
		wires.remove(wire);
		
		Link link = getLink();
		if(link != null) {
			for(PortConnection portConnection : ports) {
				link.unlinkPort(portConnection.getPort());
			}
		}
		
		List<LinkWires> newLinkWires = new ArrayList<>();
		
		while(wires.size() != 0) {
			Wire nextWire = wires.iterator().next();
			wires.remove(nextWire);
			
			Pair<List<Wire>, List<PortConnection>> attachedConnections = findAttachedConnections(nextWire);
			
			LinkWires linkWires = new LinkWires();
			linkWires.addWire(nextWire);
			attachedConnections.first.forEach(linkWires::addWire);
			attachedConnections.second.forEach(linkWires::addPort);
			newLinkWires.add(linkWires);
		}
		
		while(ports.size() != 0) {
			removePort(ports.iterator().next());
		}
		
		while(badPorts.size() != 0) {
			removePort(badPorts.iterator().next());
		}
		
		return newLinkWires;
	}
	
	private Pair<List<Wire>, List<PortConnection>> findAttachedConnections(Wire wire) {
		List<Wire> attachedWires = new ArrayList<>();
		List<PortConnection> attachedPorts = new ArrayList<>();
		
		Connection start = wire.getConnections().get(0);
		Connection end = wire.getConnections().get(wire.getConnections().size() - 1);
		for(Iterator<Wire> iter = wires.iterator(); iter.hasNext();) {
			Wire w = iter.next();
			
			boolean added = false;
			
			for(Connection c : w.getConnections()) {
				if((c.getX() == start.getX() && c.getY() == start.getY()) ||
						   (c.getX() == end.getX() && c.getY() == end.getY())) {
					attachedWires.add(w);
					iter.remove();
					added = true;
					break;
				}
			}
			
			if(!added) {
				Connection wStart = w.getConnections().get(0);
				Connection wEnd = w.getConnections().get(w.getConnections().size() - 1);
				
				for(Connection c : wire.getConnections()) {
					if((c.getX() == wStart.getX() && c.getY() == wStart.getY()) ||
							   (c.getX() == wEnd.getX() && c.getY() == wEnd.getY())) {
						attachedWires.add(w);
						iter.remove();
						break;
					}
				}
			}
		}
		
		for(Wire attachedWire : new ArrayList<>(attachedWires)) {
			Pair<List<Wire>, List<PortConnection>> attachedConnections = findAttachedConnections(attachedWire);
			attachedWires.addAll(attachedConnections.first);
			attachedPorts.addAll(attachedConnections.second);
		}
		
		Set<PortConnection> allPorts = new HashSet<>();
		allPorts.addAll(ports);
		allPorts.addAll(badPorts);
		allPorts.forEach(port -> {
			for(Connection c : wire.getConnections()) {
				if(port.getX() == c.getX() && port.getY() == c.getY()) {
					attachedPorts.add(port);
					removePort(port);
					break;
				}
			}
		});
		
		return new Pair<>(attachedWires, attachedPorts);
	}
	
	public void addPort(PortConnection port) {
		port.setLinkWires(this);
		
		if(ports.size() > 0) {
			Link link = getLink();
			if(port.getLink() != link) {
				try {
					link.linkPort(port.getPort());
				} catch(Exception exc) {
					exc.printStackTrace();
					badPorts.add(port);
					return;
				}
			}
		}
		
		ports.add(port);
	}
	
	public Set<PortConnection> getPorts() {
		return ports;
	}
	
	public Set<PortConnection> getBadPorts() {
		return badPorts;
	}
	
	public void removePort(PortConnection port) {
		if(!ports.contains(port)) {
			if(badPorts.remove(port)) {
				port.setLinkWires(null);
			}
			
			return;
		}
		
		getLink().unlinkPort(port.getPort());
		ports.remove(port);
		port.setLinkWires(null);
	}
	
	public LinkWires merge(LinkWires other) {
		other.ports.forEach(this::addPort);
		other.badPorts.forEach(this::addPort);
		other.wires.forEach(this::addWire);
		return this;
	}
	
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		for(Wire wire : wires) {
			wire.paint(graphics, circuitState);
		}
	}
	
	public static class Wire extends GuiElement {
		private LinkWires linkWires;
		private int length;
		private boolean horizontal;
		private List<Connection> connections = new ArrayList<>();
		
		public Wire(LinkWires linkWires, int startX, int startY, int length, boolean horizontal) {
			super(startX, startY, horizontal ? Math.abs(length) : 2, horizontal ? 2 : Math.abs(length));
			
			this.linkWires = linkWires;
			
			if(length == 0)
				throw new IllegalArgumentException("Length cannot be 0");
			
			if(length < 0) {
				if(horizontal) {
					setX(startX + length);
				} else {
					setY(startY + length);
				}
				
				this.length = length = -length;
			} else {
				this.length = length;
			}
			
			this.horizontal = horizontal;
			
			int count = Math.max(1, length / GuiUtils.BLOCK_SIZE);
			int xOffset = horizontal ? GuiUtils.BLOCK_SIZE : 0;
			int yOffset = horizontal ? 0 : GuiUtils.BLOCK_SIZE;
			for(int i = 0; i < count; i++) {
				connections.add(new WireConnection(this, i * xOffset, i * yOffset));
			}
			connections.add(new WireConnection(this, length * (xOffset != 0 ? 1 : 0), length * (yOffset != 0 ? 1 : 0)));
		}
		
		public LinkWires getLinkWires() {
			return linkWires;
		}
		
		public void setLinkWires(LinkWires linkWires) {
			this.linkWires = linkWires;
			
			for(Connection connection : connections) {
				connection.setLinkWires(linkWires);
			}
		}
		
		public int getLength() {
			return length;
		}
		
		public boolean isHorizontal() {
			return horizontal;
		}
		
		@Override
		public List<Connection> getConnections() {
			return connections;
		}
		
		@Override
		public int hashCode() {
			return getX() ^ getY() ^ (horizontal ? 1 : 0) ^ length;
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof Wire) {
				Wire wire = (Wire)other;
				return this.getX() == wire.getX() && this.getY() == wire.getY() && this.horizontal == wire.horizontal && this.length == wire.length;
			}
			
			return false;
		}
		
		@Override
		public void paint(GraphicsContext graphics, CircuitState circuitState) {
			graphics.setLineWidth(2);
			if(linkWires.isLinkGood()) {
				Link link = linkWires.getLink();
				if(link != null) {
					if(circuitState.isShortCircuited(link)) {
						graphics.setStroke(Color.RED);
					} else {
						GuiUtils.setBitColor(graphics, circuitState.getValue(link));
					}
				} else {
					GuiUtils.setBitColor(graphics, new WireValue(1));
				}
			} else {
				graphics.setStroke(Color.ORANGE);
			}
			graphics.strokeLine(getX(), getY(), horizontal ? getX() + length : getX(), horizontal ? getY() : getY() + length);
		}
	}
}
