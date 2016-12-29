package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.ControlledBuffer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class ControlledBufferPeer extends ComponentPeer<ControlledBuffer> {
	public ControlledBufferPeer(Circuit circuit, Properties properties, int x, int y) {
		super(x, y, 2, 2);
		
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		
		ControlledBuffer buffer = circuit.addComponent(
				new ControlledBuffer(properties.getValue(Properties.LABEL),
				                     properties.getIntValue(Properties.BITSIZE)));
		circuit.addComponent(buffer);
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_IN), "In", 1, 0));
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_ENABLE), "Enable", 0, 1));
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_OUT), "Out", 1, getHeight()));
		
		init(buffer, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
