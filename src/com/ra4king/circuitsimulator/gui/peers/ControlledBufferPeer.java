package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.ControlledBuffer;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class ControlledBufferPeer extends ComponentPeer<ControlledBuffer> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Gates", "Buffer"),
		                     new Image(ControlledBufferPeer.class.getResourceAsStream("/resources/Buffer.png")),
		                     new Properties());
	}
	
	public ControlledBufferPeer(Properties props, int x, int y) {
		super(x, y, 2, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		ControlledBuffer buffer = new ControlledBuffer(properties.getValue(Properties.LABEL),
		                                               properties.getIntValue(Properties.BITSIZE));
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_IN), "In", 1, 0));
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_ENABLE), "Enable", 0, 1));
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_OUT), "Out", 1, getHeight()));
		
		init(buffer, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.beginPath();
		graphics.moveTo(getScreenX(), getScreenY());
		graphics.lineTo(getScreenX() + getScreenWidth(), getScreenY());
		graphics.lineTo(getScreenX() + getScreenWidth() * 0.5, getScreenY() + getScreenHeight());
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.fill();
		graphics.setStroke(Color.BLACK);
		graphics.stroke();
	}
}
