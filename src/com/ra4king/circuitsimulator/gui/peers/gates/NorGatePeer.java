package com.ra4king.circuitsimulator.gui.peers.gates;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;
import com.ra4king.circuitsimulator.simulator.components.gates.NorGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class NorGatePeer extends GatePeer {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Gates", "NOR"),
		                     new Image(NorGatePeer.class.getResourceAsStream("/resources/NorGate.png")),
		                     new Properties());
	}
	
	public NorGatePeer(Properties properties, int x, int y) {
		super(properties, x, y);
	}
	
	@Override
	public Gate getGate(Properties properties) {
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
		return new NorGate(properties.getValue(Properties.LABEL),
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
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth(),
		               getScreenY() + getScreenHeight() * 1.3, getScreenWidth() * 0.7);
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(), getScreenX(),
		               getScreenY() + getScreenHeight(), getScreenWidth() * 0.7);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
		
		graphics.fillOval(getScreenX() + getScreenWidth() * 0.8,
		                  getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2,
		                  getScreenWidth() * 0.2);
		graphics.strokeOval(getScreenX() + getScreenWidth() * 0.8,
		                    getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() *
				                                                                                     0.2,
		                    getScreenWidth() * 0.2);
	}
}
