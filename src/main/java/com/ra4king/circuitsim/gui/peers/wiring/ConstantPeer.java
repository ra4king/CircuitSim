package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.wiring.Constant;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ConstantPeer extends ComponentPeer<Constant> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Wiring", "Constant"),
			new Image(ConstantPeer.class.getResourceAsStream("/images/Constant.png")),
			new Properties());
	}
	
	private final WireValue value;
	
	public ConstantPeer(Properties props, int x, int y) {
		super(x, y, 0, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.BASE);
		properties.ensureProperty(Properties.VALUE);
		properties.mergeIfExists(props);
		
		Constant constant = new Constant(
			properties.getValue(Properties.LABEL),
			properties.getValue(Properties.BITSIZE),
			properties.getValue(Properties.VALUE).getValue());
		
		int bitSize = constant.getBitSize();
		switch (properties.getValue(Properties.BASE)) {
			case BINARY -> {
				setWidth(Math.max(2, Math.min(8, bitSize)));
				setHeight((int)Math.round((1 + (bitSize - 1) / 8) * 1.5));
			}
			case HEXADECIMAL -> {
				setWidth(Math.max(2, 1 + (bitSize - 1) / 4));
				setHeight(2);
			}
			case DECIMAL -> {
				// 3.322 ~ log_2(10)
				int width = Math.max(2, (int)Math.ceil(bitSize / 3.322));
				width += bitSize == 32 ? 1 : 0;
				setWidth(width);
				setHeight(2);
			}
		}
		
		value = WireValue.of(constant.getValue(), bitSize);
		
		List<PortConnection> connections = new ArrayList<>();
		switch (properties.getValue(Properties.DIRECTION)) {
			case EAST -> connections.add(new PortConnection(this, constant.getPort(0), getWidth(), getHeight() / 2));
			case WEST -> connections.add(new PortConnection(this, constant.getPort(0), 0, getHeight() / 2));
			case NORTH -> connections.add(new PortConnection(this, constant.getPort(0), getWidth() / 2, 0));
			case SOUTH -> connections.add(new PortConnection(this, constant.getPort(0), getWidth() / 2, getHeight()));
		}
		
		init(constant, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFont(GuiUtils.getFont(16));
		graphics.setFill(Color.GRAY);
		graphics.setStroke(Color.GRAY);
		
		graphics.fillRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 10, 10);
		graphics.strokeRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 10, 10);
		
		String valStr = switch (getProperties().getValue(Properties.BASE)) {
			case BINARY -> value.toString();
			case HEXADECIMAL -> value.toHexString();
			case DECIMAL -> value.toDecString();
		};
		
		if (value.getBitSize() > 1) {
			graphics.setFill(Color.BLACK);
		} else {
			GuiUtils.setBitColor(graphics, value.getBit(0));
		}
		
		if (getProperties().getValue(Properties.BASE) == Properties.Base.DECIMAL) {
			GuiUtils.drawValueOneLine(graphics, valStr, getScreenX(), getScreenY(), getScreenWidth());
		} else {
			GuiUtils.drawValue(graphics, valStr, getScreenX(), getScreenY(), getScreenWidth());
		}
	}
}
