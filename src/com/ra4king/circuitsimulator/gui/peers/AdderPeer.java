package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Adder;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class AdderPeer extends ComponentPeer<Adder> {
	private List<Connection> connections = new ArrayList<>();
	
	public AdderPeer(Adder adder, int x, int y) {
		super(adder, x, y, 2, 3);
		
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_A), 0, 1));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_B), 0, 2));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_IN), 1, 0));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_OUT), getWidth(), 1));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_OUT), 1, getHeight()));
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.strokeText("+", getScreenX() + getScreenWidth() / 2 - 2, getScreenY() + getScreenHeight() / 2 + 2);
	}
}
