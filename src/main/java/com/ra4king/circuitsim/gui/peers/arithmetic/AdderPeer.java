package com.ra4king.circuitsim.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.arithmetic.Adder;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class AdderPeer extends ComponentPeer<Adder> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Arithmetic", "Adder"),
			new Image(AdderPeer.class.getResourceAsStream("/images/Adder.png")),
			new Properties());
	}
	
	public AdderPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		Adder adder = new Adder(properties.getValue(Properties.LABEL), properties.getValue(Properties.BITSIZE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_A), "A", 0, 1));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_B), "B", 0, 3));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_IN), "Carry in", 2, 0));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_OUT), "Out", getWidth(), 2));
		connections.add(new PortConnection(this, adder.getPort(Adder.PORT_CARRY_OUT), "Carry out", 2, getHeight()));
		
		init(adder, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(GuiUtils.getFont(16, true));
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), "+");
		graphics.setFill(Color.BLACK);
		graphics.fillText(
			"+",
			getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
			getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.45);
	}
}
