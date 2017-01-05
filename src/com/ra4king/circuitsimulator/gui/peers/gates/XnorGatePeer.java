package com.ra4king.circuitsimulator.gui.peers.gates;

import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;
import com.ra4king.circuitsimulator.simulator.components.gates.XnorGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * @author Roi Atalla
 */
public class XnorGatePeer extends GatePeer {
	public XnorGatePeer(Properties properties, int x, int y) {
		super(properties, x, y);
	}
	
	@Override
	public Gate getGate(Properties properties) {
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
		return new XnorGate(properties.getValue(Properties.LABEL),
		                    properties.getIntValue(Properties.BITSIZE),
		                    properties.getIntValue(Properties.NUM_INPUTS));
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.beginPath();
		graphics.moveTo(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight());
		graphics.arc(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight() * 0.5,
		             getScreenWidth() * 0.25, getScreenHeight() * 0.5, 270, 180);
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth(),
		               getScreenY() + getScreenHeight() * 1.3, getScreenWidth() * 0.7);
		graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(),
		               getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight(),
		               getScreenWidth() * 0.7);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
		
		graphics.strokeArc(getScreenX() - getScreenWidth() * 0.3, getScreenY(), getScreenWidth() * 0.5,
		                   getScreenHeight(), 270, 180, ArcType.OPEN);
		
		graphics.fillOval(getScreenX() + getScreenWidth() * 0.8,
		                  getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2,
		                  getScreenWidth() * 0.2);
		graphics.strokeOval(getScreenX() + getScreenWidth() * 0.8,
		                    getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2,
		                    getScreenWidth() * 0.2);
	}
}
