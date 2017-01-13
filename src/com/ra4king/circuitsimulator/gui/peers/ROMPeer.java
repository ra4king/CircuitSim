package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ra4king.circuitsimulator.gui.CircuitManager;
import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsimulator.gui.Properties.PropertyMemoryValidator;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.ROM;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ROMPeer extends ComponentPeer<ROM> {
	private static final Property ADDRESS_BITS;
	
	static {
		String[] addressBits = new String[16];
		for(int i = 0; i < addressBits.length; i++) {
			addressBits[i] = String.valueOf(i + 1);
		}
		ADDRESS_BITS = new Property("Address bits", new PropertyListValidator(addressBits), "8");
	}
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Memory", "ROM"),
		                     new Image(ROMPeer.class.getResourceAsStream("/resources/RAM.png")),
		                     new Properties());
	}
	
	public ROMPeer(Properties props, int x, int y) {
		super(x, y, 5, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(ADDRESS_BITS);
		properties.mergeIfExists(props);
		
		int addressBits = properties.getIntValue(ADDRESS_BITS);
		int dataBits = properties.getIntValue(Properties.BITSIZE);
		
		String contents = props.getValueOrDefault("Contents", "");
		
		PropertyMemoryValidator memoryValidator = new PropertyMemoryValidator(addressBits, dataBits);
		properties.setProperty(new Property("Contents", memoryValidator, contents));
		
		int[] memory = memoryValidator.parseToArray(contents);
		ROM ram = new ROM(properties.getValue(Properties.LABEL), dataBits, addressBits, memory);
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, ram.getPort(ROM.PORT_ADDRESS), "Address", 0, 2));
		connections.add(new PortConnection(this, ram.getPort(ROM.PORT_ENABLE), "Enable", 2, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(ROM.PORT_DATA), "Data", getWidth(), 2));
		
		init(ram, properties, connections);
	}
	
	@Override
	public List<MenuItem> getContextMenuItems(CircuitManager circuit) {
		MenuItem menuItem = new MenuItem("Edit contents");
		menuItem.setOnAction(event -> {
			Property property = getProperties().getProperty("Contents");
			((Button)property.validator.createGui(property.value, newValue -> {
				Properties newProps = new Properties(getProperties());
				newProps.setProperty(new Property(property.name, property.validator, newValue));
				
				try {
					circuit.getCircuitBoard().recreateComponent(this, newProps);
				} catch(Exception exc) {
					exc.printStackTrace();
				}
			})).fire();
		});
		return Collections.singletonList(menuItem);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.strokeText("ROM", getScreenX() + getScreenWidth() / 2 - 15, getScreenY() + getScreenHeight() / 2 + 5);
	}
}
