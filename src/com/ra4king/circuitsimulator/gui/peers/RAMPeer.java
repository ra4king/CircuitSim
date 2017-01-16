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
import com.ra4king.circuitsimulator.gui.Properties.MemoryLine;
import com.ra4king.circuitsimulator.gui.Properties.PropertyMemoryValidator;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.RAM;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class RAMPeer extends ComponentPeer<RAM> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Memory", "RAM"),
		                     new Image(RAMPeer.class.getResourceAsStream("/resources/RAM.png")),
		                     new Properties());
	}
	
	public RAMPeer(Properties props, int x, int y) {
		super(x, y, 5, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.ADDRESS_BITS);
		properties.mergeIfExists(props);
		
		int addressBits = properties.getValue(Properties.ADDRESS_BITS);
		int dataBits = properties.getValue(Properties.BITSIZE);
		
		RAM ram = new RAM(properties.getValue(Properties.LABEL), dataBits, addressBits);
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ADDRESS), "Address", 0, 2));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLK), "Clock", 1, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ENABLE), "Enable", 2, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_LOAD), "Load", 3, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLEAR), "Clear", 4, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_DATA), "Data", getWidth(), 2));
		
		init(ram, properties, connections);
	}
	
	@Override
	public List<MenuItem> getContextMenuItems(CircuitManager circuit) {
		MenuItem menuItem = new MenuItem("Edit contents");
		menuItem.setOnAction(event -> {
			PropertyMemoryValidator memoryValidator =
					new PropertyMemoryValidator(getComponent().getAddressBits(), getComponent().getDataBits());
			
			CircuitState currentState = circuit.getCircuitBoard().getCurrentState();
			List<MemoryLine> memory =
					memoryValidator.parse(getComponent().getMemoryContents(currentState), (address, value) -> {
						getComponent().store(currentState, address, value);
						Platform.runLater(() -> {
							try {
								circuit.getCircuitBoard().runSim();
							} catch(Exception exc) {
							}
						});
					});
			
			getComponent().addMemoryListener((address, data) -> {
				int index = address / 16;
				MemoryLine line = memory.get(index);
				line.values.get(address - index * 16).setValue(String.format("%x", data));
			});
			
			memoryValidator.createAndShowMemoryWindow(circuit.getSimulatorWindow().getStage(), memory);
		});
		return Collections.singletonList(menuItem);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.strokeText("RAM", getScreenX() + getScreenWidth() / 2 - 15, getScreenY() + getScreenHeight() / 2 + 5);
	}
}
