package com.ra4king.circuitsimulator.gui.peers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Splitter;

/**
 * @author Roi Atalla
 */
public class SplitterPeer extends ComponentPeer<Splitter> {
	private List<Connection> connections = new ArrayList<>();
	
	public SplitterPeer(Splitter splitter, int x, int y) {
		super(splitter, x, y, (splitter.getNumPorts() - 1) * GuiUtils.BLOCK_SIZE, GuiUtils.BLOCK_SIZE);
		
		connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED), 0, 0));
		for(int i = 0; i < splitter.getNumPorts() - 1; i++) {
			connections.add(new PortConnection(this, splitter.getPort(splitter.getNumPorts() - 2 - i), (i + 1) * GuiUtils.BLOCK_SIZE, getHeight()));
		}
	}
	
	@Override
	public void paint(Graphics2D g, CircuitState circuitState) {
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.BLACK);
		g.drawLine(getX(), getY(), getX() + GuiUtils.BLOCK_SIZE, getY() + getHeight());
		g.drawLine(getX() + GuiUtils.BLOCK_SIZE, getY() + getHeight(), getX() + getWidth(), getY() + getHeight());
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
}
