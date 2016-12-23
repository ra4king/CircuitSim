package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.RAM;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class RAMPeer extends ComponentPeer<RAM> {
	private List<Connection> connections = new ArrayList<>();
	
	public RAMPeer(RAM ram, int x, int y) {
		super(ram, x, y, 5, 4);
		
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ADDRESS), "Address", 0, 2));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLK), "Clock", 1, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ENABLE), "Enable", 2, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_LOAD), "Load", 3, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLEAR), "Clear", 4, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_DATA), "Data", getWidth(), 2));
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.strokeText("RAM", getScreenX() + getScreenWidth() / 2 - 15, getScreenY() + getScreenHeight() / 2 + 5);
	}
}
