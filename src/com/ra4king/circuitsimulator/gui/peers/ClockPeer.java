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
import com.ra4king.circuitsimulator.simulator.components.Clock;

/**
 * @author Roi Atalla
 */
public class ClockPeer extends ComponentPeer<Clock> {
	private List<Connection> connections = new ArrayList<>();
	
	public ClockPeer(Clock clock, int x, int y) {
		super(clock, x, y, 2 * GuiUtils.BLOCK_SIZE, 2 * GuiUtils.BLOCK_SIZE);
		connections.add(new PortConnection(this, clock.getPort(Clock.PORT), getWidth(), GuiUtils.BLOCK_SIZE));
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(Graphics2D g, CircuitState circuitState) {
		GuiUtils.setBitColor(g, circuitState.getValue(getComponent().getPort(Clock.PORT)), Color.WHITE);
		GuiUtils.drawShape(g::fillRect, this);
		g.setColor(Color.BLACK);
		GuiUtils.drawShape(g::drawRect, this);
	}
}
