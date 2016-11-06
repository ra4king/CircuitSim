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
		super(ram, x, y, 5 * GuiUtils.BLOCK_SIZE, 4 * GuiUtils.BLOCK_SIZE);
		
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ADDRESS), 0, 2 * GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLK), GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ENABLE), 2 * GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_LOAD), 3 * GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLEAR), 4 * GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_DATA), getWidth(), 2 * GuiUtils.BLOCK_SIZE));
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
		
		graphics.strokeText("RAM", getX() + getWidth() / 2 - 15, getY() + getHeight() / 2 + 5);
	}
}
