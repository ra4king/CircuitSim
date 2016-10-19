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
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.Pin;

/**
 * @author Roi Atalla
 */
public class PinPeer extends ComponentPeer<Pin> {
	public PinPeer(Pin pin, int x, int y) {
		super(pin, x, y, pin.getBitSize() * 15, 15);
	}
	
	public boolean isInput() {
		return getComponent().isInput();
	}
	
	@Override
	public List<Connection> getConnections() {
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, getComponent().getPort(0), getWidth(), getHeight() / 2));
		return connections;
	}
	
	@Override
	public void paint(Graphics2D g, CircuitState circuitState) {
		WireValue value = circuitState.getValue(getComponent().getPort(0));
		GuiUtils.setBitColor(g, value, Color.WHITE);
		GuiUtils.drawShape(g::fillRect, this);
		
		g.setColor(value.getBitSize() > 1 ? Color.BLACK : Color.WHITE);
		g.drawString(value.toString(), getX() + 5, getY() + 12);
		
		g.setColor(Color.BLACK);
		GuiUtils.drawShape(g::drawRect, this);
	}
}
