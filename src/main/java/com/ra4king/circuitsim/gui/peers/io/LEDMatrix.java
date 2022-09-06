package com.ra4king.circuitsim.gui.peers.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Property;
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
public class LEDMatrix extends ComponentPeer<Component> {
	private static final Property<Integer> COL_COUNT, ROW_COUNT;
	
	static {
		COL_COUNT = new Property<>("Column count", Properties.BITSIZE.validator, 5);
		ROW_COUNT = new Property<>("Row count", Properties.BITSIZE.validator, 7);
	}
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Input/Output", "LED Matrix"),
			new Image(LEDMatrix.class.getResourceAsStream("/images/LEDMatrix.png")),
			new Properties());
	}
	
	public LEDMatrix(Properties props, int x, int y) {
		super(x, y, 0, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(COL_COUNT);
		properties.ensureProperty(ROW_COUNT);
		properties.mergeIfExists(props);
		
		int rows = properties.getValue(ROW_COUNT);
		int cols = properties.getValue(COL_COUNT);
		
		setWidth(cols);
		setHeight(rows);
		
		int[] bitsizes = new int[rows];
		Arrays.fill(bitsizes, cols);
		
		Component component = new Component(properties.getValue(Properties.LABEL), bitsizes) {
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
		};
		
		List<PortConnection> connections = new ArrayList<>();
		for (int i = 0; i < rows; i++) {
			connections.add(new PortConnection(this, component.getPort(i), 0, i));
		}
		
		init(component, properties, connections);
	}
	
	@Override
	public int getScreenY() {
		return (int)(super.getScreenY() - 0.5 * GuiUtils.BLOCK_SIZE);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		for (int i = 0; i < getComponent().getNumPorts(); i++) {
			WireValue value = circuitState.getLastReceived(getComponent().getPort(i));
			for (int b = value.getBitSize() - 1; b >= 0; b--) {
				GuiUtils.setBitColor(graphics, value.getBit(b));
				graphics.fillRect(
					getScreenX() + (value.getBitSize() - b - 1) * GuiUtils.BLOCK_SIZE,
					getScreenY() + i * GuiUtils.BLOCK_SIZE,
					GuiUtils.BLOCK_SIZE,
					GuiUtils.BLOCK_SIZE);
			}
		}
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
