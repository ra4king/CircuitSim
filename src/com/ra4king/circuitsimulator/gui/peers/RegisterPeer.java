package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Register;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class RegisterPeer extends ComponentPeer<Register> {
	public RegisterPeer(Circuit circuit, Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.merge(props);
		
		Register register = circuit.addComponent(
				new Register(properties.getValue(Properties.LABEL),
				             properties.getIntValue(Properties.BITSIZE)));
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, register.getPort(Register.PORT_IN), 0, 2));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ENABLE), 0, 3));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_CLK), 1, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ZERO), 2, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_OUT), getWidth(), 2));
		
		init(register, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		String value = circuitState.getValue(getComponent().getPort(Register.PORT_OUT)).toString();
		graphics.strokeText(value.length() <= 4 ? value : value.substring(0, 4), getScreenX() + 2, getScreenY() + 15);
		if(value.length() > 4) {
			graphics.strokeText(value.substring(4), getScreenX() + 2, getScreenY() + 25);
		}
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
