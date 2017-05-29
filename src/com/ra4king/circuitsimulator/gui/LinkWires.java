package com.ra4king.circuitsimulator.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port.Link;

import javafx.scene.canvas.GraphicsContext;

/**
 * @author Roi Atalla
 */
public class LinkWires {
	private Set<PortConnection> ports;
	private Set<PortConnection> invalidPorts;
	private Set<Wire> wires;
	
	private Exception lastException;
	
	public LinkWires() {
		ports = new HashSet<>();
		invalidPorts = new HashSet<>();
		wires = new HashSet<>();
	}
	
	public boolean isLinkValid() {
		return invalidPorts.isEmpty();
	}
	
	public Exception getLastException() {
		return lastException;
	}
	
	public boolean isEmpty() {
		return (ports.size() + invalidPorts.size()) <= 1 && wires.size() == 0;
	}
	
	public Link getLink() {
		return ports.size() > 0 ? ports.iterator().next().getPort().getLink() : null;
	}
	
	public void addWire(Wire wire) {
		wire.setLinkWires(this);
		wires.add(wire);
	}
	
	public void removeWire(Wire wire) {
		if(wires.contains(wire)) {
			wire.setLinkWires(null);
			wires.remove(wire);
		} else {
			throw new IllegalStateException("Wire isn't even in here");
		}
	}
	
	public Set<Wire> getWires() {
		return wires;
	}
	
	public Set<LinkWires> splitWires(Set<Wire> toRemove) {
		toRemove.forEach(wire -> wire.setLinkWires(null));
		wires.removeAll(toRemove);
		
		Link link = getLink();
		if(link != null) {
			for(PortConnection port : ports) {
				link.unlinkPort(port.getPort());
			}
		}
		
		Set<LinkWires> newLinkWires = new HashSet<>();
		
		while(!wires.isEmpty()) {
			Wire nextWire = wires.iterator().next();
			wires.remove(nextWire);
			LinkWires linkWires = findAttachedConnections(nextWire);
			linkWires.addWire(nextWire);
			newLinkWires.add(linkWires);
		}
		
		Set<PortConnection> portsLeft = new HashSet<>(ports);
		portsLeft.addAll(invalidPorts);
		
		while(portsLeft.size() != 0) {
			LinkWires linkWires = new LinkWires();
			
			PortConnection nextPort = portsLeft.iterator().next();
			portsLeft.remove(nextPort);
			removePort(nextPort);
			linkWires.addPort(nextPort);
			
			for(Iterator<PortConnection> iter = portsLeft.iterator(); iter.hasNext(); ) {
				PortConnection otherPort = iter.next();
				
				if(nextPort.getX() == otherPort.getX() && nextPort.getY() == otherPort.getY()) {
					iter.remove();
					removePort(otherPort);
					linkWires.addPort(otherPort);
				}
			}
			
			if(linkWires.isEmpty()) {
				linkWires.clear();
			} else {
				newLinkWires.add(linkWires);
			}
		}
		
		return newLinkWires;
	}
	
	private LinkWires findAttachedConnections(Wire wire) {
		LinkWires linkWires = new LinkWires();
		
		Connection start = wire.getConnections().get(0);
		Connection end = wire.getConnections().get(wire.getConnections().size() - 1);
		for(Iterator<Wire> iter = wires.iterator(); iter.hasNext(); ) {
			Wire w = iter.next();
			
			boolean added = false;
			
			for(Connection c : w.getConnections()) {
				if((c.getX() == start.getX() && c.getY() == start.getY()) ||
						   (c.getX() == end.getX() && c.getY() == end.getY())) {
					linkWires.addWire(w);
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
						linkWires.addWire(w);
						iter.remove();
						break;
					}
				}
			}
		}
		
		for(Wire attachedWire : new ArrayList<>(linkWires.getWires())) {
			linkWires.merge(findAttachedConnections(attachedWire));
		}
		
		Set<PortConnection> allPorts = new HashSet<>();
		allPorts.addAll(ports);
		allPorts.addAll(invalidPorts);
		allPorts.forEach(port -> {
			for(Connection c : wire.getConnections()) {
				if(port.getX() == c.getX() && port.getY() == c.getY()) {
					removePort(port);
					linkWires.addPort(port);
					break;
				}
			}
		});
		
		return linkWires;
	}
	
	public void addPort(PortConnection port) {
		port.setLinkWires(this);
		
		if(ports.size() > 0) {
			Link link = getLink();
			try {
				link.linkPort(port.getPort());
			} catch(Exception exc) {
				invalidPorts.add(port);
				lastException = exc;
				return;
			}
		}
		
		ports.add(port);
	}
	
	public Set<PortConnection> getPorts() {
		return ports;
	}
	
	public Set<PortConnection> getInvalidPorts() {
		return invalidPorts;
	}
	
	public void removePort(PortConnection port) {
		if(!ports.contains(port)) {
			if(invalidPorts.remove(port)) {
				port.setLinkWires(null);
			}
			
			return;
		}
		
		getLink().unlinkPort(port.getPort());
		ports.remove(port);
		port.setLinkWires(null);
		
		if(ports.isEmpty()) {
			Set<PortConnection> invalidPorts = new HashSet<>(this.invalidPorts);
			this.invalidPorts.clear();
			
			for(PortConnection invalid : invalidPorts) {
				addPort(invalid);
			}
		}
	}
	
	public void clear() {
		Stream.concat(new HashSet<>(ports).stream(), new HashSet<>(invalidPorts).stream()).forEach(this::removePort);
		new HashSet<>(wires).forEach(this::removeWire);
	}
	
	public LinkWires merge(LinkWires other) {
		if(other == this) {
			return this;
		}
		
		other.ports.forEach(this::addPort);
		other.invalidPorts.forEach(this::addPort);
		other.wires.forEach(this::addWire);
		return this;
	}
	
	public static class Wire extends GuiElement {
		private LinkWires linkWires;
		private int length;
		private boolean horizontal;
		private List<Connection> connections = new ArrayList<>();
		
		public Wire(Wire wire) {
			this(wire.linkWires, wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
		}
		
		public Wire(LinkWires linkWires, int startX, int startY, int length, boolean horizontal) {
			super(startX, startY, horizontal ? Math.abs(length) : 0, horizontal ? 0 : Math.abs(length));
			
			setLinkWires(linkWires);
			
			if(length == 0) {
				throw new IllegalArgumentException("Length cannot be 0");
			}
			
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
			
			int xOffset = horizontal ? 1 : 0;
			int yOffset = horizontal ? 0 : 1;
			for(int i = 0; i < length; i++) {
				connections.add(new WireConnection(this, i * xOffset, i * yOffset));
			}
			connections.add(new WireConnection(this, length * xOffset, length * yOffset));
		}
		
		@Override
		public int getScreenX() {
			return super.getScreenX() - (horizontal ? 0 : 1);
		}
		
		@Override
		public int getScreenY() {
			return super.getScreenY() - (horizontal ? 1 : 0);
		}
		
		@Override
		public int getScreenWidth() {
			return horizontal ? super.getScreenWidth() : 2;
		}
		
		@Override
		public int getScreenHeight() {
			return horizontal ? 2 : super.getScreenHeight();
		}
		
		public LinkWires getLinkWires() {
			return linkWires;
		}
		
		public void setLinkWires(LinkWires linkWires) {
			if(linkWires == null) {
				linkWires = new LinkWires();
				linkWires.addWire(this);
			}
			
			this.linkWires = linkWires;
		}
		
		public int getLength() {
			return length;
		}
		
		public boolean isHorizontal() {
			return horizontal;
		}
		
		public boolean isWithin(Wire wire) {
			return wire.horizontal == this.horizontal &&
					       this.getX() >= wire.getX() && this.getX() + this.getWidth() <= wire.getX() + wire.getWidth
							                                                                                         () &&
					
					       this.getY() >= wire.getY() && this.getY() + this.getHeight() <= wire.getY() + wire
							                                                                                     .getHeight();
		}
		
		public boolean overlaps(Wire wire) {
			return wire.horizontal == this.horizontal &&
					       (wire.horizontal
					        ? !(wire.getX() >= getX() + getLength() || getX() >= wire.getX() + wire.getLength())
					        : !(wire.getY() >= getY() + getLength() || getY() >= wire.getY() + wire.getLength()));
		}
		
		public Connection getStartConnection() {
			return connections.get(0);
		}
		
		public Connection getEndConnection() {
			return connections.get(connections.size() - 1);
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
				return this.getX() == wire.getX() && this.getY() == wire.getY() && this.horizontal == wire.horizontal
						       && this.length == wire.length;
			}
			
			return false;
		}
		
		@Override
		public void paint(GraphicsContext graphics, CircuitState circuitState) {
			GuiUtils.setBitColor(graphics, circuitState, linkWires);
			GuiUtils.drawShape(graphics::fillRect, this);
		}
	}
}
