package com.ra4king.circuitsim.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyListValidator;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.arithmetic.Shifter;
import com.ra4king.circuitsim.simulator.components.arithmetic.Shifter.ShiftType;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ShifterPeer extends ComponentPeer<Shifter> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Arithmetic", "Shifter"),
		                     new Image(ShifterPeer.class.getResourceAsStream("/images/Shifter.png")),
		                     new Properties());
	}
	
	public ShifterPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(new Property<>("Shift Type",
		                                         new PropertyListValidator<>(Shifter.ShiftType.values(),
		                                                                     (t) -> t.toString().replace('_', ' ')),
		                                         ShiftType.LOGICAL_LEFT));
		properties.mergeIfExists(props);
		
		Shifter shifter = new Shifter(properties.getValue(Properties.LABEL),
		                              properties.getValue(Properties.BITSIZE),
		                              properties.getValue("Shift Type"));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, shifter.getPort(Shifter.PORT_IN), "In", 0, 1));
		connections.add(new PortConnection(this, shifter.getPort(Shifter.PORT_SHIFT), "Shift", 0, 3));
		connections.add(new PortConnection(this, shifter.getPort(Shifter.PORT_OUT), "Out", 4, 2));
		
		init(shifter, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState state) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFill(Color.BLACK);
		graphics.setLineWidth(1.5);
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		ShiftType shiftType = getProperties().getValue("Shift Type");
		boolean rotateRight = false;
		switch (shiftType) {
			case ROTATE_LEFT:
				graphics.strokeLine(x + width - 5, y + height * 0.5, x + width - 5, y + height * 0.5 - 10);
				graphics.strokeLine(x + width - 15, y + height * 0.5 - 10, x + width - 5, y + height * 0.5 - 10);
			case LOGICAL_LEFT:
				graphics.strokeLine(x + width - 15, y + height * 0.5, x + width - 5, y + height * 0.5);
				graphics.strokeLine(x + width - 10, y + height * 0.5 - 5, x + width - 15, y + height * 0.5);
				graphics.strokeLine(x + width - 10, y + height * 0.5 + 5, x + width - 15, y + height * 0.5);
				break;
			case ROTATE_RIGHT:
				rotateRight = true;
				graphics.strokeLine(x + width - 15, y + height * 0.5, x + width - 15, y + height * 0.5 - 10);
				graphics.strokeLine(x + width - 15, y + height * 0.5 - 10, x + width - 5, y + height * 0.5 - 10);
			case ARITHMETIC_RIGHT:
				if (!rotateRight) {
					graphics.strokeLine(x + width - 20, y + height * 0.5, x + width - 18, y + height * 0.5);
				}
			case LOGICAL_RIGHT:
				graphics.strokeLine(x + width - 15, y + height * 0.5, x + width - 5, y + height * 0.5);
				graphics.strokeLine(x + width - 10, y + height * 0.5 - 5, x + width - 5, y + height * 0.5);
				graphics.strokeLine(x + width - 10, y + height * 0.5 + 5, x + width - 5, y + height * 0.5);
				break;
		}
	}
}
