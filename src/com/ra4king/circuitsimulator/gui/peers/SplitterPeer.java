package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Splitter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class SplitterPeer extends ComponentPeer<Splitter> {
	private List<Connection> connections = new ArrayList<>();
	
	public SplitterPeer(Splitter splitter, int x, int y) {
		super(splitter, x, y, Math.max(2, splitter.getNumPorts() - 1), 1);
		
		connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED), 0, 0));
		for(int i = 0; i < splitter.getNumPorts() - 1; i++) {
			connections.add(new PortConnection(this, splitter.getPort(splitter.getNumPorts() - 2 - i), i + 1, getHeight()));
		}
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(GraphicsContext g, CircuitState circuitState) {
		g.setLineWidth(2);
		g.setStroke(Color.BLACK);
		g.strokeLine(getScreenX(), getScreenY(), getScreenX() + GuiUtils.BLOCK_SIZE, getScreenY() + getScreenHeight());
		g.strokeLine(getScreenX() + GuiUtils.BLOCK_SIZE, getScreenY() + getScreenHeight(), getScreenX() + getScreenWidth(), getScreenY() + getScreenHeight());
	}
}
