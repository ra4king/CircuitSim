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
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;

/**
 * @author Roi Atalla
 */
public class GatePeer extends ComponentPeer<Gate> {
	public GatePeer(Gate gate, int x, int y) {
		super(gate, x, y, 30, 30);
	}
	
	@Override
	public List<Connection> getConnections() {
		List<Connection> connections = new ArrayList<>();
		
		int gates = getComponent().getNumPorts() - 1;
		for(int i = 0; i < gates; i++) {
			connections.add(new PortConnection(this, getComponent().getPort(i), 0, getHeight() / 2 + (i - gates / 2) * 10 + ((~gates) & 1) * 5));
		}
		
		connections.add(new PortConnection(this, getComponent().getPort(gates), getWidth(), getHeight() / 2));
		
		return connections;
	}
	
	@Override
	public void paint(Graphics2D g, CircuitState circuitState) {
		g.setColor(Color.BLACK);
		GuiUtils.drawShape(g::drawRect, this);
		g.drawString(getComponent().toString(), getX() + 2, getY() + getHeight() - 10);
	}
}
