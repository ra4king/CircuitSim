package com.ra4king.circuitsimulator.gui.peers.gates;

import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * @author Roi Atalla
 */
public class XorGatePeer extends GatePeer {
	public XorGatePeer(Circuit circuit, Properties properties, int x, int y) {
		super(circuit, properties, x, y);
	}
	
	@Override
	public Gate getGate(Circuit circuit, Properties properties) {
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
		return circuit.addComponent(new XorGate(properties.getValue(Properties.LABEL),
		                                        properties.getIntValue(Properties.BITSIZE),
		                                        properties.getIntValue(Properties.NUM_INPUTS)));
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
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
