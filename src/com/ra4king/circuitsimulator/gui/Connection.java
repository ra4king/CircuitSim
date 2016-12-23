package com.ra4king.circuitsimulator.gui;

import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.WireValue;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public abstract class Connection {
	private GuiElement parent;
	private int x;
	private int y;
	
	public Connection(GuiElement parent, int x, int y) {
		this.parent = parent;
		this.x = x;
		this.y = y;
	}
	
	public GuiElement getParent() {
		return parent;
	}
	
	public abstract LinkWires getLinkWires();
	
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
		if(getLinkWires() == null || getLinkWires().getLink() == null) {
			GuiUtils.setBitColor(graphics, new WireValue(1));
		} else if(!getLinkWires().isLinkValid()) {
			graphics.setFill(Color.ORANGE);
		} else if(circuitState.isShortCircuited(getLinkWires().getLink())) {
			graphics.setFill(Color.RED);
		} else {
			GuiUtils.setBitColor(graphics, circuitState.getValue(getLinkWires().getLink()));
		}
		
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
