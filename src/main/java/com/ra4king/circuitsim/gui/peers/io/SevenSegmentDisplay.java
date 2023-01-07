package com.ra4king.circuitsim.gui.peers.io;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class SevenSegmentDisplay extends ComponentPeer<Component> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Input/Output", "7-Segment Display"),
			new Image(SevenSegmentDisplay.class.getResourceAsStream("/images/HexDisplay.png")),
			new Properties());
	}
	
	public SevenSegmentDisplay(Properties props, int x, int y) {
		super(x, y, 4, 6);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.mergeIfExists(props);
		
		Component component = new Component(properties.getValue(Properties.LABEL), new int[] { 7 }) {
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
		};
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, component.getPort(0), "7-bit input", getWidth() / 2, getHeight()));
		init(component, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		WireValue value = circuitState.getLastReceived(getComponent().getPort(0));
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		int margin = 4;
		int size = 6;
		
		if (value.getBit(0) == State.ONE) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin + size, y + margin, width - 2 * margin - 2 * size, size);
		
		if (value.getBit(1) == State.ONE) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin + size, y + (height - size) / 2.0, width - 2 * margin - 2 * size, size);
		
		if (value.getBit(2) == State.ONE) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin + size, y + height - margin - size, width - 2 * margin - 2 * size, size);
		
		if (value.getBit(3) == State.ONE) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + width - margin - size, y + margin + size / 2.0, size, (height - size) / 2.0 - margin);
		
		if (value.getBit(4) == State.ONE) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin, y + margin + size / 2.0, size, (height - size) / 2.0 - margin);
		
		if (value.getBit(5) == State.ONE) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + width - margin - size, y + height / 2.0, size, (height - size) / 2.0 - margin);
		
		if (value.getBit(6) == State.ONE) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin, y + height / 2.0, size, (height - size) / 2.0 - margin);
	}
}
