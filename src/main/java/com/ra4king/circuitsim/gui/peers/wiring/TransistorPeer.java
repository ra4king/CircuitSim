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
import com.ra4king.circuitsim.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.wiring.Transistor;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class TransistorPeer extends ComponentPeer<Transistor> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Transistor"),
		                     new Image(TransistorPeer.class.getResourceAsStream("/images/Transistor.png")),
		                     new Properties());
	}
	
	private static final Property<Boolean> TRANSISTOR_TYPE_PROPERTY;
	private static final Property<Boolean> GATE_LOCATION_PROPERTY;
	
	static {
		TRANSISTOR_TYPE_PROPERTY =
				new Property<>("Type",
				               new PropertyListValidator<>(Arrays.asList(true, false),
				                                           val -> val ? "P-Type" : "N-Type"), true);
		
		GATE_LOCATION_PROPERTY = new Property<>("Gate Location", Properties.LOCATION_VALIDATOR, true);
	}
	
	public TransistorPeer(Properties props, int x, int y) {
		super(x, y, 4, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(TRANSISTOR_TYPE_PROPERTY);
		properties.ensureProperty(GATE_LOCATION_PROPERTY);
		properties.mergeIfExists(props);
		
		Transistor transistor = new Transistor(properties.getValue(Properties.LABEL),
		                                       properties.getValue(TRANSISTOR_TYPE_PROPERTY));
		
		List<PortConnection> connections = new ArrayList<>();
		
		int yOff = 0;
		switch(properties.getValue(Properties.DIRECTION)) {
			case EAST:
			case NORTH:
				yOff = properties.getValue(GATE_LOCATION_PROPERTY) ? 0 : getHeight();
				break;
			case WEST:
			case SOUTH:
				yOff = properties.getValue(GATE_LOCATION_PROPERTY) ? getHeight() : 0;
				break;
		}
		
		connections.add(new PortConnection(this, transistor.getPort(Transistor.PORT_IN), "Input",
		                                   0, getHeight() - yOff));
		connections.add(new PortConnection(this, transistor.getPort(Transistor.PORT_GATE), "Gate",
		                                   getWidth() / 2, yOff));
		connections.add(new PortConnection(this, transistor.getPort(Transistor.PORT_OUT), "Output",
		                                   getWidth(), getHeight() - yOff));
		
		GuiUtils.rotatePorts(connections, Direction.EAST, properties.getValue(Properties.DIRECTION));
		GuiUtils.rotateElementSize(this, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		init(transistor, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState state) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		GuiUtils.rotateGraphics(this, graphics, getProperties().getValue(Properties.DIRECTION));
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth() > getScreenHeight() ? getScreenWidth() : getScreenHeight();
		int height = getScreenWidth() > getScreenHeight() ? getScreenHeight() : getScreenWidth();
		
		boolean gateLoc = getProperties().getValue(GATE_LOCATION_PROPERTY);
		switch(getProperties().getValue(Properties.DIRECTION)) {
			case WEST:
			case SOUTH:
				gateLoc = !gateLoc;
				break;
		}
		
		int yOff = gateLoc ? 0 : height;
		int m = gateLoc ? 1 : -1;
		
		graphics.setStroke(Color.BLACK);
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
		
		if(getProperties().getValue(TRANSISTOR_TYPE_PROPERTY)) {
			graphics.strokeOval(x + width * 0.5 - 3, y + (gateLoc ? 3 : height - 9), 6, 6);
		} else {
			graphics.strokeLine(x + width * 0.5, y + yOff, x + width * 0.5, y + height * 0.5);
		}
	}
}
