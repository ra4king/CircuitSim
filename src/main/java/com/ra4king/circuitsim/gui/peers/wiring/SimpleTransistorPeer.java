package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyListValidator;
import com.ra4king.circuitsim.gui.properties.PropertyValidators;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.wiring.SimpleTransistor;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class SimpleTransistorPeer extends ComponentPeer<SimpleTransistor> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Transistor"),
		                     new Image(TransistorPeer.class.getResourceAsStream("/images/SimpleTransistor.png")),
		                     new Properties());
	}
	
	private static final Property<Boolean> TRANSISTOR_TYPE_PROPERTY;
	private static final Property<Boolean> GATE_LOCATION_PROPERTY;
	
	static {
		TRANSISTOR_TYPE_PROPERTY =
			new Property<>("Type",
			               new PropertyListValidator<>(Arrays.asList(true, false), val -> val ? "P-Type" : "N-Type"),
			               true);
		
		GATE_LOCATION_PROPERTY = new Property<>("Gate Location", PropertyValidators.LOCATION_VALIDATOR, true);
	}
	
	public SimpleTransistorPeer(Properties props, int x, int y) {
		super(x, y, 4, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(TRANSISTOR_TYPE_PROPERTY);
		properties.ensureProperty(GATE_LOCATION_PROPERTY);
		properties.mergeIfExists(props);
		
		boolean isPType = properties.getValue(TRANSISTOR_TYPE_PROPERTY);
		SimpleTransistor transistor = new SimpleTransistor(properties.getValue(Properties.LABEL), isPType);
		
		List<PortConnection> connections = new ArrayList<>();
		
		Direction direction = effectiveDirection(isPType);
		int yOff;
		if (direction == Direction.SOUTH) {
			yOff = properties.getValue(GATE_LOCATION_PROPERTY) ? getHeight() : 0;
		} else {
			yOff = properties.getValue(GATE_LOCATION_PROPERTY) ? 0 : getHeight();
		}
		
		connections.add(new PortConnection(this,
		                                   transistor.getPort(SimpleTransistor.PORT_SOURCE),
		                                   "Source",
		                                   0,
		                                   getHeight() - yOff));
		connections.add(new PortConnection(this,
		                                   transistor.getPort(SimpleTransistor.PORT_GATE),
		                                   "Gate",
		                                   getWidth() / 2,
		                                   yOff));
		connections.add(new PortConnection(this,
		                                   transistor.getPort(SimpleTransistor.PORT_DRAIN),
		                                   "Drain",
		                                   getWidth(),
		                                   getHeight() - yOff));
		
		GuiUtils.rotatePorts(connections, Direction.EAST, direction);
		GuiUtils.rotateElementSize(this, Direction.EAST, direction);
		
		init(transistor, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState state) {
		boolean isPType = getProperties().getValue(TRANSISTOR_TYPE_PROPERTY);
		Direction direction = effectiveDirection(isPType);
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		GuiUtils.rotateGraphics(this, graphics, direction);
		
		int x = getScreenX();
		int y = getScreenY();
		int width = Math.max(getScreenWidth(), getScreenHeight());
		int height = Math.min(getScreenWidth(), getScreenHeight());
		
		boolean gateLoc = (direction == Direction.SOUTH) ^ getProperties().getValue(GATE_LOCATION_PROPERTY);
		
		int yOff = gateLoc ? 0 : height;
		int m = gateLoc ? 1 : -1;
		
		graphics.setStroke(getColor());
		graphics.setLineWidth(2);
		
		graphics.beginPath();
		graphics.moveTo(x, y + height - yOff);
		graphics.lineTo(x + width / 3.0, y + height - yOff);
		graphics.lineTo(x + width / 3.0, y + yOff + m * height * 0.7);
		graphics.lineTo(x + 2.0 * width / 3.0, y + yOff + m * height * 0.7);
		graphics.lineTo(x + 2.0 * width / 3.0, y + height - yOff);
		graphics.lineTo(x + width, y + height - yOff);
		
		graphics.moveTo(x + width / 3.0, y + yOff + m * height * 0.5);
		graphics.lineTo(x + 2.0 * width / 3.0, y + yOff + m * height * 0.5);
		graphics.stroke();
		
		graphics.setLineWidth(1);
		
		if (getProperties().getValue(TRANSISTOR_TYPE_PROPERTY)) {
			graphics.strokeOval(x + width * 0.5 - 3, y + (gateLoc ? 3 : height - 9), 6, 6);
		} else {
			graphics.strokeLine(x + width * 0.5, y + yOff, x + width * 0.5, y + height * 0.5);
		}
	}
	
	private Color getColor() {
		return getComponent().getIllegallyWired() ? Color.RED : Color.BLACK;
	}
	
	// Follow the Patt & Patel convention in which P-type transistors point
	// downward and N-type transistors point upward
	private Direction effectiveDirection(boolean isPType) {
		return isPType ? Direction.SOUTH : Direction.NORTH;
	}
}
