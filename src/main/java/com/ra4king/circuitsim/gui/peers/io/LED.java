package com.ra4king.circuitsim.gui.peers.io;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyValidators;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class LED extends ComponentPeer<Component> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Input/Output", "LED"),
			new Image(LED.class.getResourceAsStream("/images/LED.png")),
			new Properties());
	}
	
	private static final Property<Color> ON_COLOR_PROPERTY;
	private static final Property<Color> OFF_COLOR_PROPERTY;
	
	static {
		OFF_COLOR_PROPERTY = new Property<>("Off Color", PropertyValidators.COLOR_VALIDATOR, Color.DARKGRAY);
		ON_COLOR_PROPERTY = new Property<>("On Color", PropertyValidators.COLOR_VALIDATOR, Color.RED);
	}
	
	private final Color offColor;
	private final Color onColor;
	
	public LED(Properties props, int x, int y) {
		super(x, y, 2, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(OFF_COLOR_PROPERTY);
		properties.ensureProperty(ON_COLOR_PROPERTY);
		properties.mergeIfExists(props);
		
		offColor = properties.getValue(OFF_COLOR_PROPERTY);
		onColor = properties.getValue(ON_COLOR_PROPERTY);
		
		Component component = new Component(properties.getValue(Properties.LABEL), new int[] { 1 }) {
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
		};
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, component.getPort(0), "1-bit input", 0, 1));
		
		GuiUtils.rotatePorts(connections, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		init(component, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState state) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		State bit = state.getLastReceived(getComponent().getPort(0)).getBit(0);
		
		graphics.setFill(bit == State.ONE ? onColor : offColor);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillOval, this);
		GuiUtils.drawShape(graphics::strokeOval, this);
	}
}
