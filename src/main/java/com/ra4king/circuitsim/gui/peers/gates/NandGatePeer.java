package com.ra4king.circuitsim.gui.peers.gates;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.gates.NandGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class NandGatePeer extends GatePeer<NandGate> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Gates", "NAND"),
			new Image(NandGatePeer.class.getResourceAsStream("/images/NandGate.png")),
			new Properties());
	}
	
	public NandGatePeer(Properties properties, int x, int y) {
		super(properties, x, y);
	}
	
	@Override
	protected void ensureProperties(Properties properties) {
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
	}
	
	@Override
	public NandGate buildGate(Properties properties) {
		int numInputs;
		return new NandGate(
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
		graphics.moveTo(x, y);
		graphics.lineTo(x, y + height);
		graphics.arc(x + width * 0.3, y + height * 0.5, width * 0.5, height * 0.5, 270, 180);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
		
		graphics.fillOval(x + width * 0.8, y + height * 0.5 - width * 0.1, width * 0.2, width * 0.2);
		graphics.strokeOval(x + width * 0.8, y + height * 0.5 - width * 0.1, width * 0.2, width * 0.2);
	}
}
