package com.ra4king.circuitsimulator.gui.peers.gates;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class XorGatePeer extends GatePeer {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Gates", "XOR"),
		                     new Image(XorGatePeer.class.getResourceAsStream("/resources/XorGate.png")),
		                     new Properties());
	}
	
	public XorGatePeer(Properties properties, int x, int y) {
		super(properties, x, y);
	}
	
	@Override
	protected void ensureProperties(Properties properties) {
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
	}
	
	@Override
	public Gate buildGate(Properties properties) {
		return new XorGate(properties.getValue(Properties.LABEL),
		                   properties.getValue(Properties.BITSIZE),
		                   properties.getValue(Properties.NUM_INPUTS));
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		super.paint(graphics, circuitState);
		
		graphics.beginPath();
		graphics.moveTo(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight());
		graphics.arc(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight() * 0.5,
		             getScreenWidth() * 0.25, getScreenHeight() * 0.5, 270, 180);
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth() * 1.25,
		               getScreenY() + getScreenHeight(), getScreenWidth());
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(),
		               getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight(), getScreenWidth());
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
		
		graphics.strokeArc(getScreenX() - getScreenWidth() * 0.3, getScreenY(), getScreenWidth() * 0.5,
		                   getScreenHeight(), 270, 180, ArcType.OPEN);
	}
}
