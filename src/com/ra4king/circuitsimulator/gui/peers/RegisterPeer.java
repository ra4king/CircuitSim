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
import com.ra4king.circuitsimulator.simulator.components.Register;

/**
 * @author Roi Atalla
 */
public class RegisterPeer extends ComponentPeer<Register> {
	private List<Connection> connections = new ArrayList<>();
	
	public RegisterPeer(Register register, int x, int y) {
		super(register, x, y, 3 * GuiUtils.BLOCK_SIZE, 4 * GuiUtils.BLOCK_SIZE);
		
		connections.add(new PortConnection(this, register.getPort(Register.PORT_IN), 0, 2 * GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ENABLE), 0, 3 * GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_CLK), GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ZERO), 2 * GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_OUT), getWidth(), 2 * GuiUtils.BLOCK_SIZE));
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
		String value = circuitState.getValue(getComponent().getPort(Register.PORT_OUT)).toString();
		g.drawString(value.length() <= 4 ? value : value.substring(0, 4), getX() + 2, getY() + 15);
		if(value.length() > 4) {
			g.drawString(value.substring(4), getX() + 2, getY() + 25);
		}
		GuiUtils.drawShape(g::drawRect, this);
	}
}
