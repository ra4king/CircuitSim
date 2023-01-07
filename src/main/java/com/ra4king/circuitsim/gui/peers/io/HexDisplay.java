package com.ra4king.circuitsim.gui.peers.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class HexDisplay extends ComponentPeer<Component> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Input/Output", "Hex Display"),
			new Image(HexDisplay.class.getResourceAsStream("/images/HexDisplay.png")),
			new Properties());
	}
	
	public HexDisplay(Properties props, int x, int y) {
		super(x, y, 4, 6);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.mergeIfExists(props);
		
		Component component = new Component(properties.getValue(Properties.LABEL), new int[] { 4 }) {
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
		};
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, component.getPort(0), "4-bit input", getWidth() / 2, getHeight()));
		init(component, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		drawDigit(graphics, -1);
		
		WireValue value = circuitState.getLastReceived(getComponent().getPort(0));
		if (value.isValidValue()) {
			drawDigit(graphics, value.getValue());
		}
	}
	
	private void drawDigit(GraphicsContext graphics, int num) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		int margin = 4;
		int size = 6;
		
		if (top.contains(num)) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin + size, y + margin, width - 2 * margin - 2 * size, size);
		
		if (middle.contains(num)) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin + size, y + (height - size) / 2.0, width - 2 * margin - 2 * size, size);
		
		if (bottom.contains(num)) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin + size, y + height - margin - size, width - 2 * margin - 2 * size, size);
		
		if (topRight.contains(num)) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + width - margin - size, y + margin + size / 2.0, size, (height - size) / 2.0 - margin);
		
		if (topLeft.contains(num)) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin, y + margin + size / 2.0, size, (height - size) / 2.0 - margin);
		
		if (botRight.contains(num)) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + width - margin - size, y + height / 2.0, size, (height - size) / 2.0 - margin);
		
		if (botLeft.contains(num)) {
			graphics.setFill(Color.RED);
		} else {
			graphics.setFill(Color.LIGHTGRAY);
		}
		graphics.fillRect(x + margin, y + height / 2.0, size, (height - size) / 2.0 - margin);
	}
	
	private static final Set<Integer> top = new HashSet<>(Arrays.asList(0, 2, 3, 5, 6, 7, 8, 9, 10, 12, 14, 15));
	private static final Set<Integer> topRight = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 7, 8, 9, 10, 13));
	private static final Set<Integer> botRight = new HashSet<>(Arrays.asList(0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13));
	private static final Set<Integer> bottom = new HashSet<>(Arrays.asList(0, 2, 3, 5, 6, 8, 9, 11, 12, 13, 14));
	private static final Set<Integer> botLeft = new HashSet<>(Arrays.asList(0, 2, 6, 8, 10, 11, 12, 13, 14, 15));
	private static final Set<Integer> topLeft = new HashSet<>(Arrays.asList(0, 4, 5, 6, 8, 9, 10, 11, 12, 14, 15));
	private static final Set<Integer> middle = new HashSet<>(Arrays.asList(2, 3, 4, 5, 6, 8, 9, 10, 11, 13, 14, 15));
}
