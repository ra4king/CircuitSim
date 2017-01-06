package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Register;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class RegisterPeer extends ComponentPeer<Register> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Memory", "Register"),
		                     null,//new Image(RegisterPeer.class.getResourceAsStream("/resources/Register.png")),
		                     new Properties());
	}
	
	public RegisterPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		Register register = new Register(properties.getValue(Properties.LABEL),
		                                 properties.getIntValue(Properties.BITSIZE));
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, register.getPort(Register.PORT_IN), "In", 0, 2));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ENABLE), "Enable", 0, 3));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_CLK), "Clock", 1, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ZERO), "Clear", 2, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_OUT), "Out", getWidth(), 2));
		
		init(register, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		String value = circuitState.getLastPushedValue(getComponent().getPort(Register.PORT_OUT)).toString();
		graphics.strokeText(value.length() <= 4 ? value : value.substring(0, 4), getScreenX() + 2, getScreenY() + 15);
		if(value.length() > 4) {
			graphics.strokeText(value.substring(4), getScreenX() + 2, getScreenY() + 25);
		}
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
