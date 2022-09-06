package com.ra4king.circuitsim.gui.peers.gates;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.gates.XorGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class XorGatePeer extends GatePeer<XorGate> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Gates", "XOR"),
			new Image(XorGatePeer.class.getResourceAsStream("/images/XorGate.png")),
			new Properties());
	}
	
	public XorGatePeer(Properties properties, int x, int y) {
		super(properties, x, y);
	}
	
	@Override
	protected void ensureProperties(Properties properties) {
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
	}
	
	@Override
	public XorGate buildGate(Properties properties) {
		int numInputs;
		return new XorGate(
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
		graphics.moveTo(x + width * 0.1, y + height);
		graphics.arc(x + width * 0.1, y + height * 0.5, width * 0.25, height * 0.5, 270, 180);
		graphics.arcTo(x + width * 0.66, y, x + width * 1.25, y + height, width);
		graphics.arcTo(x + width * 0.66, y + height, x + width * 0.1, y + height, width);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
		
		graphics.strokeArc(x - width * 0.3, y, width * 0.5, height, 270, 180, ArcType.OPEN);
	}
}
