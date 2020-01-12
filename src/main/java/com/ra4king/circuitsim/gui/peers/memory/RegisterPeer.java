package com.ra4king.circuitsim.gui.peers.memory;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.CircuitManager;
import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;
import com.ra4king.circuitsim.simulator.components.memory.Register;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class RegisterPeer extends ComponentPeer<Register> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Memory", "Register"),
		                     new Image(RegisterPeer.class.getResourceAsStream("/Register.png")),
		                     new Properties());
	}
	
	private final PortConnection clockConnection;
	
	public RegisterPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		Register register = new Register(properties.getValue(Properties.LABEL),
		                                 properties.getValue(Properties.BITSIZE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, register.getPort(Register.PORT_IN), "In", 0, 2));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ENABLE), "Enable", 0, 3));
		connections.add(
			clockConnection = new PortConnection(this, register.getPort(Register.PORT_CLK), "Clock", 1, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_ZERO), "Clear", 3, getHeight()));
		connections.add(new PortConnection(this, register.getPort(Register.PORT_OUT), "Out", getWidth(), 2));
		
		init(register, properties, connections);
	}
	
	@Override
	public boolean keyPressed(CircuitManager manager, CircuitState state, KeyCode keyCode, String text) {
		switch(keyCode) {
			case DIGIT0:
			case DIGIT1:
			case DIGIT2:
			case DIGIT3:
			case DIGIT4:
			case DIGIT5:
			case DIGIT6:
			case DIGIT7:
			case DIGIT8:
			case DIGIT9:
			case NUMPAD0:
			case NUMPAD1:
			case NUMPAD2:
			case NUMPAD3:
			case NUMPAD4:
			case NUMPAD5:
			case NUMPAD6:
			case NUMPAD7:
			case NUMPAD8:
			case NUMPAD9:
			case A:
			case B:
			case C:
			case D:
			case E:
			case F:
				char c = text.charAt(0);
				
				int value;
				if(c >= '0' && c <= '9') {
					value = c - '0';
				} else {
					value = Character.toUpperCase(c) - 'A' + 10;
				}
				
				WireValue currentValue = state.getLastPushed(getComponent().getPort(Register.PORT_OUT));
				WireValue typedValue = WireValue.of(value, Math.min(4, currentValue.getBitSize()));
				if(typedValue.getValue() != value) {
					typedValue.setAllBits(State.ZERO); // to prevent typing '9' on a 3-bit value, producing 1
				}
				
				if(currentValue.getBitSize() <= 4) {
					currentValue.set(typedValue);
				} else {
					for(int i = currentValue.getBitSize() - 1; i >= 4; i--) {
						currentValue.setBit(i, currentValue.getBit(i - 4));
					}
					
					for(int i = 0; i < 4; i++) {
						currentValue.setBit(i, typedValue.getBit(i));
					}
				}
				
				state.pushValue(getComponent().getPort(Register.PORT_OUT), currentValue);
				break;
		}
		
		return false;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		String value = circuitState.getLastPushed(getComponent().getPort(Register.PORT_OUT)).toHexString();
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		graphics.setFill(Color.BLACK);
		graphics.setFont(GuiUtils.getFont(13));
		for(int i = 0; i * 4 < value.length(); i++) {
			int endIndex = i * 4 + 4 > value.length() ? value.length() : 4 * i + 4;
			String toPrint = value.substring(4 * i, endIndex);
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), toPrint, false);
			graphics.fillText(toPrint, x + width * 0.5 - bounds.getWidth() * 0.5, y + 11 + 10 * i);
		}
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFill(Color.GRAY);
		graphics.setFont(GuiUtils.getFont(10));
		graphics.fillText("D", x + 3, y + height * 0.5 + 6);
		graphics.fillText("Q", x + width - 10, y + height * 0.5 + 6);
		graphics.fillText("en", x + 3, y + height - 7);
		graphics.fillText("0", x + width - 13, y + height - 4);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawClockInput(graphics, clockConnection, Direction.SOUTH);
	}
}
