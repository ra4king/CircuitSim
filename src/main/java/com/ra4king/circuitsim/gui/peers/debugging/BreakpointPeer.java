package com.ra4king.circuitsim.gui.peers.debugging;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.debugging.Breakpoint;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Charles Jenkins
 */
public class BreakpointPeer extends ComponentPeer<Breakpoint> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Debugging", "Breakpoint"),
			new Image(BreakpointPeer.class.getResourceAsStream("/images/Breakpoint.png")),
			new Properties());
	}
	
	public BreakpointPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.VALUE);
		properties.mergeIfExists(props);
		
		Breakpoint breakpoint = new Breakpoint(
			properties.getValue(Properties.LABEL),
			properties.getValue(Properties.BITSIZE),
			properties.getValue(Properties.VALUE).getValue());
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, breakpoint.getPort(Breakpoint.PORT_ENABLE), "ENABLE", 0, 1));
		connections.add(new PortConnection(this, breakpoint.getPort(Breakpoint.PORT_DATA), "DATA", 2, getHeight()));
		
		init(breakpoint, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(GuiUtils.getFont(16, true));
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), "BP");
		graphics.setFill(Color.BLACK);
		graphics.fillText(
			"BP",
			getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
			getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.45);
	}
}
