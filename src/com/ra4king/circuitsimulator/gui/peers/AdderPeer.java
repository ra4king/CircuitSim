package com.ra4king.circuitsimulator.gui.peers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Adder;

/**
 * @author Roi Atalla
 */
public class AdderPeer extends ComponentPeer<Adder> {
	private List<Connection> connections = new ArrayList<>();
	
	public AdderPeer(Adder adder, int x, int y) {
		super(adder, x, y, 2 * GuiUtils.BLOCK_SIZE, 3 * GuiUtils.BLOCK_SIZE);
		
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_A), 0, GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_B), 0, 2 * GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_IN), GuiUtils.BLOCK_SIZE, 0));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_OUT), getWidth(), GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_OUT), GuiUtils.BLOCK_SIZE, getHeight()));
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(Graphics2D g, CircuitState circuitState) {
		g.setColor(Color.WHITE);
		GuiUtils.drawShape(g::fillRect, this);
		g.setColor(Color.BLACK);
		GuiUtils.drawShape(g::drawRect, this);
		
		g.drawString("+", getX() + getWidth() / 2 - 2, getY() + getHeight() / 2 + 2);
	}
}
