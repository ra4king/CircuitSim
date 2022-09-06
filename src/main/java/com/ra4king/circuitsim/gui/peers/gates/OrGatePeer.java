package com.ra4king.circuitsim.gui.peers.gates;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.gates.OrGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class OrGatePeer extends GatePeer<OrGate> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Gates", "OR"),
			new Image(OrGatePeer.class.getResourceAsStream("/images/OrGate.png")),
			new Properties());
	}
	
	public OrGatePeer(Properties properties, int x, int y) {
		super(properties, x, y);
	}
	
	@Override
	protected void ensureProperties(Properties properties) {
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
	}
	
	@Override
	public OrGate buildGate(Properties properties) {
		int numInputs;
		return new OrGate(
			properties.getValue(Properties.LABEL),
			properties.getValue(Properties.BITSIZE),
			numInputs = properties.getValue(Properties.NUM_INPUTS),
			parseNegatedInputs(numInputs, properties));
	}
	
	@Override
	public void paintGate(GraphicsContext graphics, CircuitState circuitState) {
		int x = getScreenX();
		int y = getScreenY();
		int width = 4 * GuiUtils.BLOCK_SIZE;
		int height = 4 * GuiUtils.BLOCK_SIZE;
		
		graphics.beginPath();
		graphics.moveTo(x, y + height);
		graphics.arc(x, y + height * 0.5, width * 0.25, height * 0.5, 270, 180);
		graphics.arcTo(x + width * 0.66, y, x + width * 1.25, y + height, width);
		graphics.arcTo(x + width * 0.66, y + height, x, y + height, width);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
	}
}
