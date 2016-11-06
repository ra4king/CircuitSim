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
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.strokeText("+", getX() + getWidth() / 2 - 2, getY() + getHeight() / 2 + 2);
	}
}
