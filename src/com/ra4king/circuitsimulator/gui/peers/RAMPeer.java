package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.RAM;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class RAMPeer extends ComponentPeer<RAM> {
	private static final Property ADDRESS_BITS = new Property("Address bits", Properties.BITSIZE.validator, "1");
	
	public RAMPeer(Circuit circuit, Properties props, int x, int y) {
		super(x, y, 5, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(ADDRESS_BITS);
		properties.merge(props);
		
		RAM ram = circuit.addComponent(
				new RAM(properties.getValue(Properties.LABEL),
				        properties.getIntValue(Properties.BITSIZE),
				        properties.getIntValue(ADDRESS_BITS)));
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ADDRESS), "Address", 0, 2));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLK), "Clock", 1, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ENABLE), "Enable", 2, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_LOAD), "Load", 3, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLEAR), "Clear", 4, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_DATA), "Data", getWidth(), 2));
		
		init(ram, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.strokeText("RAM", getScreenX() + getScreenWidth() / 2 - 15, getScreenY() + getScreenHeight() / 2 + 5);
	}
}
