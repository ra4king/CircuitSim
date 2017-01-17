package com.ra4king.circuitsimulator.gui.peers.gates;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;
import com.ra4king.circuitsimulator.simulator.components.gates.OrGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class OrGatePeer extends GatePeer {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Gates", "OR"),
		                     new Image(OrGatePeer.class.getResourceAsStream("/resources/OrGate.png")),
		                     new Properties());
	}
	
	public OrGatePeer(Properties properties, int x, int y) {
		super(properties, x, y);
	}
	
	@Override
	public Gate getGate(Properties properties) {
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
		return new OrGate(properties.getValue(Properties.LABEL),
		                  properties.getValue(Properties.BITSIZE),
		                  properties.getValue(Properties.NUM_INPUTS));
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		super.paint(graphics, circuitState);
		
		graphics.beginPath();
		graphics.moveTo(getScreenX(), getScreenY() + getScreenHeight());
		graphics.arc(getScreenX(), getScreenY() + getScreenHeight() * 0.5, getScreenWidth() * 0.25,
		             getScreenHeight() * 0.5, 270, 180);
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth() * 1.25,
		               getScreenY() + getScreenHeight(), getScreenWidth());
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(), getScreenX(),
		               getScreenY() + getScreenHeight(), getScreenWidth());
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
	}
}
