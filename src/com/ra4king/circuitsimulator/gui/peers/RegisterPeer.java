package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Register;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class RegisterPeer extends ComponentPeer<Register> {
	private List<Connection> connections = new ArrayList<>();
	
	public RegisterPeer(Register register, int x, int y) {
		super(register, x, y, 4 * GuiUtils.BLOCK_SIZE, 4 * GuiUtils.BLOCK_SIZE);
		
		connections.add(new PortConnection(this, register.getPort(Register.PORT_IN), 0, 2 * GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ENABLE), 0, 3 * GuiUtils.BLOCK_SIZE));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_CLK), GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ZERO), 2 * GuiUtils.BLOCK_SIZE, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_OUT), getWidth(), 2 * GuiUtils.BLOCK_SIZE));
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		String value = circuitState.getValue(getComponent().getPort(Register.PORT_OUT)).toString();
		graphics.strokeText(value.length() <= 4 ? value : value.substring(0, 4), getX() + 2, getY() + 15);
		if(value.length() > 4) {
			graphics.strokeText(value.substring(4), getX() + 2, getY() + 25);
		}
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
