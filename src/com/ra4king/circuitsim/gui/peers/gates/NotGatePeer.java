package com.ra4king.circuitsim.gui.peers.gates;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.gates.Gate;
import com.ra4king.circuitsim.simulator.components.gates.NotGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class NotGatePeer extends GatePeer {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Gates", "NOT"),
		                     new Image(NotGatePeer.class.getResourceAsStream("/resources/NotGate.png")),
		                     new Properties());
	}
	
	public NotGatePeer(Properties properties, int x, int y) {
		super(properties, x, y, 3, 2);
	}
	
	@Override
	protected void ensureProperties(Properties properties) {
		properties.ensureProperty(Properties.BITSIZE);
	}
	
	@Override
	public Gate buildGate(Properties properties) {
		return new NotGate(properties.getValue(Properties.LABEL),
		                   properties.getValue(Properties.BITSIZE));
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		super.paint(graphics, circuitState);
		
		int x = getScreenX();
		int y = getScreenY();
		int width = 3 * GuiUtils.BLOCK_SIZE;
		int height = 2 * GuiUtils.BLOCK_SIZE;
		
		graphics.beginPath();
		graphics.moveTo(x, y);
		graphics.lineTo(x, y + height);
		graphics.lineTo(x + width * 0.7, y + height * 0.5);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
		
		graphics.fillOval(x + width * 0.7, y + height * 0.5 - width * 0.125, width * 0.25, width * 0.25);
		graphics.strokeOval(x + width * 0.7, y + height * 0.5 - width * 0.125, width * 0.25, width * 0.25);
	}
}
