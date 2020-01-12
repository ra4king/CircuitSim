package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.WireValue;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class Probe extends ComponentPeer<Component> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Probe"),
		                     new Image(Probe.class.getResourceAsStream("/Probe.png")),
		                     new Properties(new Property<>(Properties.DIRECTION, Direction.SOUTH)));
	}
	
	public Probe(Properties props, int x, int y) {
		super(x, y, 0, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		int bitSize = properties.getValue(Properties.BITSIZE);
		
		Component probe = new Component(properties.getValue(Properties.LABEL),
		                                new int[] { bitSize }) {
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
		};
		
		setWidth(Math.max(2, Math.min(8, bitSize)));
		setHeight((int)Math.round((1 + (bitSize - 1) / 8) * 1.5));
		
		List<PortConnection> connections = new ArrayList<>();
		switch(properties.getValue(Properties.DIRECTION)) {
			case EAST:
				connections.add(new PortConnection(this, probe.getPort(0), getWidth(), getHeight() / 2));
				break;
			case WEST:
				connections.add(new PortConnection(this, probe.getPort(0), 0, getHeight() / 2));
				break;
			case NORTH:
				connections.add(new PortConnection(this, probe.getPort(0), getWidth() / 2, 0));
				break;
			case SOUTH:
				connections.add(new PortConnection(this, probe.getPort(0), getWidth() / 2, getHeight()));
				break;
		}
		
		init(probe, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFont(GuiUtils.getFont(16));
		Port port = getComponent().getPort(0);
		WireValue value = circuitState.getLastReceived(port);
		if(circuitState.isShortCircuited(port.getLink())) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		
		graphics.setStroke(Color.WHITE);
		
		graphics.fillRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 20, 20);
		graphics.strokeRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 20, 20);
		
		graphics.setFill(Color.BLACK);
		GuiUtils.drawValue(graphics, value.toString(), getScreenX(), getScreenY(), getScreenWidth());
	}
}
