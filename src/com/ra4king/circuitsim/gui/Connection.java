package com.ra4king.circuitsim.gui;

import com.ra4king.circuitsim.gui.LinkWires.Wire;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Port;

import javafx.scene.canvas.GraphicsContext;

/**
 * @author Roi Atalla
 */
public abstract class Connection {
	private final GuiElement parent;
	private final int x;
	private final int y;
	
	public Connection(GuiElement parent, int x, int y) {
		this.parent = parent;
		this.x = x;
		this.y = y;
	}
	
	public GuiElement getParent() {
		return parent;
	}
	
	public abstract LinkWires getLinkWires();
	
	public int getXOffset() {
		return x;
	}
	
	public int getYOffset() {
		return y;
	}
	
	public int getX() {
		return parent.getX() + x;
	}
	
	public int getScreenX() {
		return getX() * GuiUtils.BLOCK_SIZE - 3;
	}
	
	public int getY() {
		return parent.getY() + y;
	}
	
	public int getScreenY() {
		return getY() * GuiUtils.BLOCK_SIZE - 3;
	}
	
	public int getScreenWidth() {
		return 6;
	}
	
	public int getScreenHeight() {
		return 6;
	}
	
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.setBitColor(graphics, circuitState, getLinkWires());
		graphics.fillOval(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight());
	}
	
	public static class PortConnection extends Connection {
		private Port port;
		private String name;
		private LinkWires linkWires;
		
		public PortConnection(ComponentPeer<?> parent, Port port, int x, int y) {
			this(parent, port, "", x, y);
		}
		
		public PortConnection(ComponentPeer<?> parent, Port port, String name, int x, int y) {
			super(parent, x, y);
			this.port = port;
			this.name = name;
			setLinkWires(null);
		}
		
		@Override
		public ComponentPeer<?> getParent() {
			return (ComponentPeer<?>)super.getParent();
		}
		
		public Port getPort() {
			return port;
		}
		
		public String getName() {
			return name;
		}
		
		public void setLinkWires(LinkWires linkWires) {
			if(linkWires == null) {
				linkWires = new LinkWires();
				linkWires.addPort(this);
			}
			
			this.linkWires = linkWires;
		}
		
		@Override
		public LinkWires getLinkWires() {
			return linkWires;
		}
	}
	
	public static class WireConnection extends Connection {
		public WireConnection(Wire wire, int x, int y) {
			super(wire, x, y);
		}
		
		@Override
		public Wire getParent() {
			return (Wire)super.getParent();
		}
		
		@Override
		public LinkWires getLinkWires() {
			return getParent().getLinkWires();
		}
	}
}
