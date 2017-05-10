package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.Register;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class RegisterPeer extends ComponentPeer<Register> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Memory", "Register"),
		                     new Image(RegisterPeer.class.getResourceAsStream("/resources/Register.png")),
		                     new Properties());
	}
	
	public RegisterPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		Register register = new Register(properties.getValue(Properties.LABEL),
		                                 properties.getValue(Properties.BITSIZE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, register.getPort(Register.PORT_IN), "In", 0, 2));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ENABLE), "Enable", 0, 3));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_CLK), "Clock", 1, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ZERO), "Clear", 2, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_OUT), "Out", getWidth(), 2));
		
		init(register, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(getComponent().getName(),
			                    getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
			                    getScreenY() - 5);
		}
		
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		WireValue lastPushedValue = circuitState.getLastPushedValue(getComponent().getPort(Register.PORT_OUT));
		String value;
		if(lastPushedValue.getBitSize() <= 8) {
			value = lastPushedValue.toString();
		} else {
			int hexDigits = 1 + (getComponent().getBitSize() - 1) / 4;
			if(lastPushedValue.isValidValue()) {
				int num = lastPushedValue.getValue();
				value = String.format("%0" + hexDigits + "x", num);
			} else {
				value = "";
				for(int i = 0; i < hexDigits; i++) {
					value += "x";
				}
			}
		}
		
		graphics.setStroke(Color.BLACK);
		for(int i = 0; i * 4 < value.length(); i++) {
			int endIndex = i * 4 + 4 > value.length() ? value.length() : 4 * i + 4;
			String toPrint = value.substring(4 * i, endIndex);
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), toPrint);
			graphics.strokeText(toPrint, getScreenX() + getScreenWidth() * 0.5 - bounds.getWidth() * 0.5,
			                    getScreenY() + 15 + 10 * i);
		}
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
