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
	private List<Connection> connections = new ArrayList<>();
	
	public PinPeer(Pin pin, int x, int y) {
		super(pin, x, y, Math.max(2 * GuiUtils.BLOCK_SIZE, GuiUtils.BLOCK_SIZE * pin.getBitSize()), GuiUtils.BLOCK_SIZE * 2);
		
		connections.add(new PortConnection(this, pin.getPort(0), isInput() ? getWidth() : 0, GuiUtils.BLOCK_SIZE));
	}
	
	public boolean isInput() {
		return getComponent().isInput();
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(Graphics2D g, CircuitState circuitState) {
		WireValue value = isInput() ? circuitState.getLastPushedValue(getComponent().getPort(0)) :
								  circuitState.getValue(getComponent().getPort(0));
		if(circuitState.isShortCircuited(getComponent().getPort(0).getLink())) {
			g.setColor(Color.RED);
		} else {
			GuiUtils.setBitColor(g, value, Color.WHITE);
		}
		GuiUtils.drawShape(isInput() ? g::fillRect : g::fillOval, this);
		
		g.setColor(value.getBitSize() > 1 ? Color.BLACK : Color.WHITE);
		g.drawString(value.toString(), getX() + 2, getY() + getHeight() - 5);
		
		g.setColor(Color.BLACK);
		GuiUtils.drawShape(isInput() ? g::drawRect : g::drawOval, this);
	}
}
