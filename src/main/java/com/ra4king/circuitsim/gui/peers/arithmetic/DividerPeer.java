package com.ra4king.circuitsim.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.arithmetic.Divider;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class DividerPeer extends ComponentPeer<Divider> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Arithmetic", "Divider"),
		                     new Image(DividerPeer.class.getResourceAsStream("/images/Divider.png")),
		                     new Properties());
	}
	
	public DividerPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		Divider divider = new Divider(properties.getValue(Properties.LABEL), properties.getValue(Properties.BITSIZE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, divider.getPort(Divider.PORT_DIVIDEND), "Dividend", 0, 1));
		connections.add(new PortConnection(this, divider.getPort(Divider.PORT_DIVISOR), "Divisor", 0, 3));
		connections.add(new PortConnection(this, divider.getPort(Divider.PORT_QUOTIENT), "Quotient", getWidth(), 2));
		connections.add(new PortConnection(this, divider.getPort(Divider.PORT_REMAINDER), "Remainder", 2,
		                                   getHeight()));
		
		init(divider, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(GuiUtils.getFont(16, true));
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), "÷");
		graphics.setFill(Color.BLACK);
		graphics.fillText("÷",
		                  getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
		                  getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.45);
	}
}
