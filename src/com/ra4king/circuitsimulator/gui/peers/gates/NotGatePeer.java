package com.ra4king.circuitsimulator.gui.peers.gates;

import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;
import com.ra4king.circuitsimulator.simulator.components.gates.NotGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class NotGatePeer extends GatePeer {
	public NotGatePeer(Circuit circuit, Properties properties, int x, int y) {
		super(circuit, properties, x, y);
	}
	
	@Override
	public Gate getGate(Circuit circuit, Properties properties) {
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		return circuit.addComponent(new NotGate(properties.getValue(Properties.LABEL),
		                                        properties.getIntValue(Properties.BITSIZE)));
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.beginPath();
		graphics.moveTo(getScreenX(), getScreenY());
		graphics.lineTo(getScreenX(), getScreenY() + getScreenHeight());
		graphics.lineTo(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		graphics.fill();
		graphics.stroke();
		
		graphics.fillOval(getScreenX() + getScreenWidth() * 0.8,
		                  getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2,
		                  getScreenWidth() * 0.2);
		graphics.strokeOval(getScreenX() + getScreenWidth() * 0.8,
		                    getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2,
		                    getScreenWidth() * 0.2);
	}
}
