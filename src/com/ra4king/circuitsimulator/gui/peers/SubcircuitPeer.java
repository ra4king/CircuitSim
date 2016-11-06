package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Subcircuit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class SubcircuitPeer extends ComponentPeer<Subcircuit> {
	private List<Connection> connections = new ArrayList<>();
	
	public SubcircuitPeer(Subcircuit subcircuit, int x, int y) {
		super(subcircuit, x, y, 2 * GuiUtils.BLOCK_SIZE, subcircuit.getNumPorts() / 2 * GuiUtils.BLOCK_SIZE);
		
		for(int i = 0; i < subcircuit.getNumPorts(); i++) {
			int connX = i < subcircuit.getNumPorts() / 2 ? 0 : getWidth();
			int connY = (1 + (i % (subcircuit.getNumPorts() / 2))) * GuiUtils.BLOCK_SIZE;
			connections.add(new PortConnection(this, subcircuit.getPort(i), connX, connY));
		}
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
	}
}
