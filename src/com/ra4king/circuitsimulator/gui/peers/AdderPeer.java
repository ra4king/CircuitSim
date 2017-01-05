package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Adder;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class AdderPeer extends ComponentPeer<Adder> {
	public AdderPeer(Properties props, int x, int y) {
		super(x, y, 2, 3);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		Adder adder = new Adder(properties.getValue(Properties.LABEL),
		                        properties.getIntValue(Properties.BITSIZE));
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_A), "A", 0, 1));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_B), "B", 0, 2));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_IN), "Carry in", 1, 0));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_OUT), "Out", getWidth(), 1));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_OUT), "Carry out", 1, getHeight()));
		
		init(adder, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), "+");
		graphics.strokeText("+",
		                    getScreenX() + (getScreenWidth() + bounds.getWidth()) * 0.5,
		                    getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.5);
	}
}
