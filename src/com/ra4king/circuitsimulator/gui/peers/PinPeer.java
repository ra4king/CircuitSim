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
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Pin;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class PinPeer extends ComponentPeer<Pin> {
	private List<Connection> connections = new ArrayList<>();
	
	public PinPeer(Pin pin, int x, int y) {
		super(pin, x, y, Math.max(2, Math.min(8, pin.getBitSize())), 2 + 7 * ((pin.getBitSize() - 1) / 8) / 4);
		
		connections.add(new PortConnection(this, pin.getPort(0), isInput() ? getWidth() : 0, 1));
	}
	
	public boolean isInput() {
		return getComponent().isInput();
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	public void clicked(CircuitState state, int x, int y) {
		if(!isInput())
			return;
		
		Pin pin = getComponent();
		
		WireValue value = state.getLastPushedValue(pin.getPort(Pin.PORT));
		if(pin.getBitSize() == 1) {
			pin.setValue(state,
					new WireValue(1, value.getBit(0) == State.ONE ? State.ZERO : State.ONE));
		} else {
			int xOff = x - getScreenX();
			int yOff = y - getScreenY();
			
			double bitWidth = getScreenWidth() / Math.min(8.0, pin.getBitSize());
			double bitHeight = getScreenHeight() / ((pin.getBitSize() - 1) / 8 + 1.0);
			
			int bitCol = (int)(xOff / bitWidth);
			int bitRow = (int)(yOff / bitHeight);
			
			int bit = pin.getBitSize() - 1 - (bitCol + bitRow * 8);
			if(bit >= 0 && bit < pin.getBitSize()) {
				WireValue newValue = new WireValue(value);
				newValue.setBit(bit, value.getBit(bit) == State.ONE ? State.ZERO : State.ONE);
				pin.setValue(state, newValue);
			}
		}
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
			graphics.fillRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 20, 20);
			graphics.strokeRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 20, 20);
		}
		
		graphics.setStroke(value.getBitSize() > 1 ? Color.BLACK : Color.WHITE);
		
		String string = value.toString();
		for(int i = 0, row = 1; i < string.length(); row++) {
			String sub = string.substring(i, i + Math.min(8, string.length() - i));
			i += sub.length();
			graphics.strokeText(sub, getScreenX() + 2, getScreenY() + 14 * row);
		}
	}
}
