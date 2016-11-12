package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.Pin;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class PinPeer extends ComponentPeer<Pin> {
	private List<Connection> connections = new ArrayList<>();
	
	public PinPeer(Pin pin, int x, int y) {
		super(pin, x, y, GuiUtils.BLOCK_SIZE * Math.max(2, Math.min(8, pin.getBitSize())), GuiUtils.BLOCK_SIZE * (2 + 7 * ((pin.getBitSize() - 1) / 8) / 4));
		
		connections.add(new PortConnection(this, pin.getPort(0), isInput() ? getWidth() : 0, GuiUtils.BLOCK_SIZE));
	}
	
	public boolean isInput() {
		return getComponent().isInput();
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		Port port = getComponent().getPort(Pin.PORT);
		WireValue value = isInput() ? circuitState.getLastPushedValue(port): circuitState.getValue(port);
		if(circuitState.isShortCircuited(port.getLink())) {
			graphics.setFill(Color.RED);
		} else {
			GuiUtils.setBitColor(graphics, value, Color.WHITE);
		}
		graphics.setStroke(Color.BLACK);
		
		if(isInput()) {
			GuiUtils.drawShape(graphics::fillRect, this);
			GuiUtils.drawShape(graphics::strokeRect, this);
		} else {
			graphics.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 20, 20);
			graphics.strokeRoundRect(getX(), getY(), getWidth(), getHeight(), 20, 20);
		}
		
		graphics.setStroke(value.getBitSize() > 1 ? Color.BLACK : Color.WHITE);
		
		String string = value.toString();
		for(int i = 0, row = 1; i < string.length(); row++) {
			String sub = string.substring(i, i + Math.min(8, string.length() - i));
			i += sub.length();
			graphics.strokeText(sub, getX() + 2, getY() + 14 * row);
		}
	}
}
