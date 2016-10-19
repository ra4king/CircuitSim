package com.ra4king.circuitsimulator.gui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public abstract class Connection extends GuiElement {
	private GuiElement parent;
	
	public Connection(GuiElement parent, int x, int y) {
		super(x, y, 6, 6);
		this.parent = parent;
	}
	
	public GuiElement getParent() {
		return parent;
	}
	
	public int getX() {
		return parent.getX() + super.getX() - getWidth() / 2;
	}
	
	public int getXOffset() {
		return super.getX();
	}
	
	public int getY() {
		return parent.getY() + super.getY() - getHeight() / 2;
	}
	
	public int getYOffset() {
		return super.getY();
	}
	
	public abstract Link getLink();
	
	@Override
	public List<Connection> getConnections() {
		return new ArrayList<>();
	}
	
	@Override
	public void paint(Graphics2D g, CircuitState circuitState) {
		GuiUtils.setBitColor(g, getLink() == null ? new WireValue(1) : circuitState.getValue(getLink()));
		GuiUtils.drawShape(g::fillOval, this);
	}
	
	public static class PortConnection extends Connection {
		private Port port;
		
		public PortConnection(ComponentPeer<?> parent, Port port, int x, int y) {
			super(parent, x, y);
			this.port = port;
		}
		
		public Port getPort() {
			return port;
		}
		
		@Override
		public Link getLink() {
			return port.getLink();
		}
	}
	
	public static class WireConnection extends Connection {
		public WireConnection(Wire parent, int x, int y) {
			super(parent, x, y);
		}
		
		@Override
		public Link getLink() {
			return ((Wire)getParent()).getLinkWires().getLink();
		}
	}
}
