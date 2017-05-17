package com.ra4king.circuitsimulator.gui.peers.gates;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.ControlledBuffer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

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
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		ControlledBuffer buffer = new ControlledBuffer(properties.getValue(Properties.LABEL),
		                                               properties.getValue(Properties.BITSIZE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_IN), "In",
		                                   0, getHeight() / 2));
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_ENABLE), "Enable",
		                                   getWidth() / 2, getHeight()));
		connections.add(new PortConnection(this, buffer.getPort(ControlledBuffer.PORT_OUT), "Out",
		                                   getWidth(), getHeight() / 2));
		connections = GuiUtils.rotatePorts(connections, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		init(buffer, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawLabel(this, graphics);
		GuiUtils.rotateGraphics(this, graphics, getProperties().getValue(Properties.DIRECTION));
		
		graphics.beginPath();
		graphics.moveTo(getScreenX(), getScreenY());
		graphics.lineTo(getScreenX() + getScreenWidth(), getScreenY() + getScreenHeight() * 0.5);
		graphics.lineTo(getScreenX(), getScreenY() + getScreenHeight());
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.fill();
		graphics.setStroke(Color.BLACK);
		graphics.stroke();
	}
}
