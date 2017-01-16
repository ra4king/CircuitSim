package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Pin;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class PinPeer extends ComponentPeer<Pin> {
	public static final Property<Boolean> IS_INPUT = new Property<>("Is input?", Properties.YESNO_VALIDATOR, true);
	
	public static void installComponent(ComponentManagerInterface manager) {
		Properties properties = new Properties();
		properties.setValue(IS_INPUT, true);
		properties.setValue(Properties.DIRECTION, Direction.WEST);
		manager.addComponent(new Pair<>("Wiring", "Input Pin"),
		                     new Image(PinPeer.class.getResourceAsStream("/resources/InputPin.png")),
		                     properties);
		
		properties = new Properties();
		properties.setValue(IS_INPUT, false);
		manager.addComponent(new Pair<>("Wiring", "Output Pin"),
		                     new Image(PinPeer.class.getResourceAsStream("/resources/OutputPin.png")),
		                     properties);
	}
	
	public PinPeer(Properties props, int x, int y) {
		super(x, y, 0, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(IS_INPUT);
		properties.mergeIfExists(props);
		
		Pin pin = new Pin(properties.getValue(Properties.LABEL),
		                  properties.getValue(Properties.BITSIZE),
		                  properties.getValue(IS_INPUT));
		setWidth(Math.max(2, Math.min(8, pin.getBitSize())));
		setHeight(2 + 7 * ((pin.getBitSize() - 1) / 8) / 4);
		
		List<PortConnection> connections = new ArrayList<>();
		switch(properties.getValue(Properties.DIRECTION)) {
			case EAST:
				connections.add(new PortConnection(this, pin.getPort(0), 0, getHeight() / 2));
				break;
			case WEST:
				connections.add(new PortConnection(this, pin.getPort(0), getWidth(), getHeight() / 2));
				break;
			case NORTH:
				connections.add(new PortConnection(this, pin.getPort(0), getWidth() / 2, getHeight()));
				break;
			case SOUTH:
				connections.add(new PortConnection(this, pin.getPort(0), getWidth() / 2, 0));
				break;
		}
		
		init(pin, properties, connections);
	}
	
	public boolean isInput() {
		return getComponent().isInput();
	}
	
	public void clicked(CircuitState state, int x, int y) {
		if(!isInput()) {
			return;
		}
		
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
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setStroke(Color.BLACK);
			if(isInput()) {
				graphics.strokeText(getComponent().getName(), getScreenX() - bounds.getWidth() - 5,
				                    getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.4);
			} else {
				graphics.strokeText(getComponent().getName(), getScreenX() + getScreenWidth() + 5,
				                    getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.4);
			}
		}
		
		Port port = getComponent().getPort(Pin.PORT);
		WireValue value = isInput() ? circuitState.getLastPushedValue(port)
		                            : circuitState.getLastReceived(port);
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
		
		graphics.setStroke(port.getLink().getBitSize() > 1 ? Color.BLACK : Color.WHITE);
		
		String string = value.toString();
		for(int i = 0, row = 1; i < string.length(); row++) {
			String sub = string.substring(i, i + Math.min(8, string.length() - i));
			i += sub.length();
			graphics.strokeText(sub, getScreenX() + 2, getScreenY() + 14 * row);
		}
	}
}
