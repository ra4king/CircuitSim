package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class GatePeer extends ComponentPeer<Gate> {
	private List<Connection> connections = new ArrayList<>();
	
	public GatePeer(Gate gate, int x, int y) {
		super(gate, x, y, 3, 2 * ((gate.getNumPorts() + 1) / 2));
		
		int gates = gate.getNumPorts() - 1;
		for(int i = 0; i < gates; i++) {
			int add = (gates % 2 == 0 && i >= gates / 2) ? 2 : 1;
			connections.add(new PortConnection(this, gate.getPort(i), 0, i + add));
		}
		
		connections.add(new PortConnection(this, gate.getPort(gates), getWidth(), gates / 2 + 1));
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
		
		graphics.strokeText(getComponent().toString(), getScreenX() + 3, getScreenY() + getScreenHeight() / 2 + 5);
	}
}
