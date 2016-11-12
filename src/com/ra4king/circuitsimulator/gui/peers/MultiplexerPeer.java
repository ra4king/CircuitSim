package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Multiplexer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class MultiplexerPeer extends ComponentPeer<Multiplexer> {
	private List<Connection> connections = new ArrayList<>();
	
	public MultiplexerPeer(Multiplexer mux, int x, int y) {
		super(mux, x, y, 2 * GuiUtils.BLOCK_SIZE, (mux.NUM_IN_PORTS + 1) * GuiUtils.BLOCK_SIZE);
		
		for(int i = 0; i < mux.NUM_IN_PORTS; i++) {
			connections.add(new PortConnection(this, mux.getPort(i), 0, (i + 1) * GuiUtils.BLOCK_SIZE));
		}
		
		connections.add(new PortConnection(this, mux.getPort(mux.PORT_SEL), GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, mux.getPort(mux.PORT_OUT), getWidth(), ((mux.NUM_IN_PORTS + 1) / 2) * GuiUtils.BLOCK_SIZE));
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
		
		graphics.strokeText(getComponent().toString(), getX() + 2, getY() + getHeight() / 2 + 5);
	}
}
