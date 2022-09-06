package com.ra4king.circuitsim.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.arithmetic.Multiplier;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class MultiplierPeer extends ComponentPeer<Multiplier> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Arithmetic", "Multiplier"),
		                     new Image(MultiplierPeer.class.getResourceAsStream("/images/Multiplier.png")),
		                     new Properties());
	}
	
	public MultiplierPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		Multiplier
			multiplier =
			new Multiplier(properties.getValue(Properties.LABEL), properties.getValue(Properties.BITSIZE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, multiplier.getPort(Multiplier.PORT_A), "A", 0, 1));
		connections.add(new PortConnection(this, multiplier.getPort(Multiplier.PORT_B), "B", 0, 3));
		connections.add(new PortConnection(this, multiplier.getPort(Multiplier.PORT_CARRY_IN), "Carry in", 2, 0));
		connections.add(new PortConnection(this, multiplier.getPort(Multiplier.PORT_OUT_LOWER), "Out", getWidth(), 2));
		connections.add(new PortConnection(this, multiplier.getPort(Multiplier.PORT_OUT_UPPER), "Upper bits", 2,
		                                   getHeight()));
		
		init(multiplier, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(GuiUtils.getFont(16, true));
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), "×");
		graphics.setFill(Color.BLACK);
		graphics.fillText("×",
		                  getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
		                  getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.45);
	}
}
