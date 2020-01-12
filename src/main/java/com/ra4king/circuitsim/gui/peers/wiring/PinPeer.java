package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ra4king.circuitsim.gui.CircuitManager;
import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class PinPeer extends ComponentPeer<Pin> {
	public static final Property<Boolean> IS_INPUT = new Property<>("Is input?", Properties.YESNO_VALIDATOR, true);
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Input Pin"),
		                     new Image(PinPeer.class.getResourceAsStream("/InputPin.png")),
		                     new Properties(new Property<>(IS_INPUT, true)));
		
		manager.addComponent(new Pair<>("Wiring", "Output Pin"),
		                     new Image(PinPeer.class.getResourceAsStream("/OutputPin.png")),
		                     new Properties(new Property<>(IS_INPUT, false),
		                                    new Property<>(Properties.DIRECTION, Direction.WEST)));
	}
	
	public PinPeer(Properties props, int x, int y) {
		super(x, y, 0, 0);
		
		Object value = props.getValueOrDefault(IS_INPUT, false);
		boolean isInput;
		if(value instanceof String) {
			isInput = Boolean.parseBoolean((String)value);
		} else {
			isInput = (Boolean)value;
		}
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(
				new Property<>(Properties.LABEL_LOCATION, isInput ? Direction.WEST : Direction.EAST));
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(IS_INPUT);
		properties.mergeIfExists(props);
		
		Pin pin = new Pin(properties.getValue(Properties.LABEL),
		                  properties.getValue(Properties.BITSIZE),
		                  properties.getValue(IS_INPUT));
		setWidth(Math.max(2, Math.min(8, pin.getBitSize())));
		setHeight((int)Math.round((1 + (pin.getBitSize() - 1) / 8) * 1.5));
		
		List<PortConnection> connections = new ArrayList<>();
		switch(properties.getValue(Properties.DIRECTION)) {
			case EAST:
				connections.add(new PortConnection(this, pin.getPort(0), getWidth(), getHeight() / 2));
				break;
			case WEST:
				connections.add(new PortConnection(this, pin.getPort(0), 0, getHeight() / 2));
				break;
			case NORTH:
				connections.add(new PortConnection(this, pin.getPort(0), getWidth() / 2, 0));
				break;
			case SOUTH:
				connections.add(new PortConnection(this, pin.getPort(0), getWidth() / 2, getHeight()));
				break;
		}
		
		init(pin, properties, connections);
	}
	
	public boolean isInput() {
		return getComponent().isInput();
	}
	
	@Override
	public void mousePressed(CircuitManager manager, CircuitState state, double x, double y) {
		if(!isInput()) {
			return;
		}
		
		if(state != manager.getCircuit().getTopLevelState()) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.initOwner(manager.getSimulatorWindow().getStage());
			alert.initModality(Modality.WINDOW_MODAL);
			alert.setTitle("Switch to top-level state?");
			alert.setHeaderText("Switch to top-level state?");
			alert.setContentText("Cannot modify state of a subcircuit. Switch to top-level state?");
			Optional<ButtonType> buttonType = alert.showAndWait();
			if(buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
				state = manager.getCircuit().getTopLevelState();
				manager.getCircuitBoard().setCurrentState(state);
			} else {
				return;
			}
		}
		
		Pin pin = getComponent();
		
		WireValue value = state.getLastPushed(pin.getPort(Pin.PORT));
		if(pin.getBitSize() == 1) {
			pin.setValue(state,
			             new WireValue(1, value.getBit(0) == State.ONE ? State.ZERO : State.ONE));
		} else {
			double bitWidth = getScreenWidth() / Math.min(8.0, pin.getBitSize());
			double bitHeight = getScreenHeight() / ((pin.getBitSize() - 1) / 8 + 1.0);
			
			int bitCol = (int)(x / bitWidth);
			int bitRow = (int)(y / bitHeight);
			
			int bit = pin.getBitSize() - 1 - (bitCol + bitRow * 8);
			if(bit >= 0 && bit < pin.getBitSize()) {
				WireValue newValue = new WireValue(value);
				newValue.setBit(bit, value.getBit(bit) == State.ONE ? State.ZERO : State.ONE);
				pin.setValue(state, newValue);
			}
		}
	}
	
	@Override
	public boolean keyPressed(CircuitManager manager, CircuitState state, KeyCode keyCode, String text) {
		if(!isInput()) {
			return false;
		}
		
		switch(keyCode) {
			case NUMPAD0:
			case NUMPAD1:
			case DIGIT0:
			case DIGIT1:
				int value = text.charAt(0) - '0';
				
				WireValue currentValue = new WireValue(state.getLastPushed(getComponent().getPort(Pin.PORT)));
				
				for(int i = currentValue.getBitSize() - 1; i > 0; i--) {
					currentValue.setBit(i, currentValue.getBit(i - 1));
				}
				
				currentValue.setBit(0, value == 1 ? State.ONE : State.ZERO);
				getComponent().setValue(state, currentValue);
				break;
		}
		
		return false;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFont(GuiUtils.getFont(16, true));
		Port port = getComponent().getPort(Pin.PORT);
		WireValue value = isInput() ? circuitState.getLastPushed(port)
		                            : circuitState.getLastReceived(port);
		if(circuitState.isShortCircuited(port.getLink())) {
			graphics.setFill(Color.RED);
		} else {
			if(value.getBitSize() == 1) {
				GuiUtils.setBitColor(graphics, value.getBit(0));
			} else {
				graphics.setFill(Color.WHITE);
			}
		}
		graphics.setStroke(Color.BLACK);
		
		if(isInput()) {
			GuiUtils.drawShape(graphics::fillRect, this);
			GuiUtils.drawShape(graphics::strokeRect, this);
		} else {
			graphics.fillRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 20, 20);
			graphics.strokeRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 20, 20);
		}
		
		graphics.setFill(port.getLink().getBitSize() > 1 ? Color.BLACK : Color.WHITE);
		GuiUtils.drawValue(graphics, value.toString(), getScreenX(), getScreenY(), getScreenWidth());
	}
}
