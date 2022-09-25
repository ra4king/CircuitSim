package com.ra4king.circuitsim.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyListValidator;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.arithmetic.Comparator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ComparatorPeer extends ComponentPeer<Comparator> {
	private static final Property<Boolean>
		USE_SIGNED_COMPARE =
		new Property<>("Comparison Type",
		               new PropertyListValidator<>(Arrays.asList(true, false),
		                                           s -> s ? "2's " + "complement" : "Unsigned"),
		               true);
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Arithmetic", "Comparator"),
		                     new Image(ComparatorPeer.class.getResourceAsStream("/images/Comparator.png")),
		                     new Properties());
	}
	
	public ComparatorPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(USE_SIGNED_COMPARE);
		properties.mergeIfExists(props);
		
		Comparator comparator = new Comparator(properties.getValue(Properties.LABEL),
		                                       properties.getValue(Properties.BITSIZE),
		                                       properties.getValue(USE_SIGNED_COMPARE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, comparator.getPort(Comparator.PORT_A), "A", 0, 1));
		connections.add(new PortConnection(this, comparator.getPort(Comparator.PORT_B), "B", 0, 3));
		connections.add(new PortConnection(this, comparator.getPort(Comparator.PORT_LT), "A < B", getWidth(), 1));
		connections.add(new PortConnection(this, comparator.getPort(Comparator.PORT_EQ), "A = B", getWidth(), 2));
		connections.add(new PortConnection(this, comparator.getPort(Comparator.PORT_GT), "A > B", getWidth(), 3));
		
		init(comparator, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setStroke(Color.BLACK);
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(GuiUtils.getFont(12, true));
		graphics.setFill(Color.BLACK);
		graphics.fillText("<", getScreenX() + getScreenWidth() - 12, getScreenY() + 12);
		graphics.fillText("=", getScreenX() + getScreenWidth() - 12, getScreenY() + 24);
		graphics.fillText(">", getScreenX() + getScreenWidth() - 12, getScreenY() + 35);
	}
}
