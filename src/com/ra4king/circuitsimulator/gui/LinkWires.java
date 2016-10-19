package com.ra4king.circuitsimulator.gui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class LinkWires {
	private Set<Port> ports;
	private Set<Wire> wires;
	
	public LinkWires() {
		ports = new HashSet<>();
		wires = new HashSet<>();
	}
	
	public Link getLink() {
		return ports.size() > 0 ? ports.iterator().next().getLink() : null;
	}
	
	public void addWire(Wire wire) {
		wires.add(wire);
	}
	
	public Set<Wire> getWires() {
		return wires;
	}
	
	public void addPort(Port port) {
		if(ports.size() > 0) {
			Link link = getLink();
			if(port.getLink() != link) {
				link.linkPort(port);
			}
		}
		
		ports.add(port);
	}
	
	public Set<Port> getPorts() {
		return ports;
	}
	
	public LinkWires merge(LinkWires other) {
		other.ports.stream().forEach(this::addPort);
		other.wires.stream().map(Wire::new).forEach(this::addWire);
		return this;
	}
	
	public void paint(Graphics2D g, CircuitState circuitState) {
		for(Wire wire : wires) {
			wire.paint(g, circuitState);
		}
	}
	
	public class Wire extends GuiElement {
		private int length;
		private boolean horizontal;
		
		public Wire(Wire wire) {
			this(wire.getX(), wire.getY(), wire.length, wire.horizontal);
		}
		
		public Wire(int startX, int startY, int length, boolean horizontal) {
			super(startX, startY, horizontal ? length : 1, horizontal ? 1 : length);
			
			if(length == 0)
				throw new IllegalArgumentException("Length cannot be 0");
			
			if(length < 0) {
				if(horizontal) {
					setX(startX + length);
				} else {
					setY(startY + length);
				}
				
				this.length = -length;
			} else {
				this.length = length;
			}
			
			this.horizontal = horizontal;
		}
		
		public LinkWires getLinkWires() {
			return LinkWires.this;
		}
		
		public int getLength() {
			return length;
		}
		
		public boolean isHorizontal() {
			return horizontal;
		}
		
		@Override
		public List<Connection> getConnections() {
			int count = Math.max(1, length / 10);
			
			List<Connection> connections = new ArrayList<>();
			
			int xOffset = horizontal ? 1 : 0;
			int yOffset = horizontal ? 0 : 1;
			for(int i = 0; i < count; i++) {
				connections.add(new WireConnection(this, i * xOffset * 10, i * yOffset * 10));
			}
			connections.add(new WireConnection(this, length * xOffset, length * yOffset));
			
			return connections;
		}
		
		@Override
		public void paint(Graphics2D g, CircuitState circuitState) {
			WireValue value = ports.size() > 0 ? circuitState.getValue(ports.iterator().next()) : new WireValue(1);
			GuiUtils.setBitColor(g, value);
			g.drawLine(getX(), getY(), horizontal ? getX() + length : getX(), horizontal ? getY() : getY() + length);
		}
	}
}
