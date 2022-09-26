package com.ra4king.circuitsim.gui.peers.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import com.ra4king.circuitsim.gui.CircuitManager;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyMemoryValidator;
import com.ra4king.circuitsim.gui.properties.PropertyMemoryValidator.MemoryLine;
import com.ra4king.circuitsim.gui.properties.PropertyValidators;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.memory.RAM;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class RAMPeer extends ComponentPeer<RAM> {
	public static final Property<Boolean>
		SEPARATE_LOAD_STORE_PORTS =
		new Property<>("Separate Load/Store Ports?", PropertyValidators.YESNO_VALIDATOR, false);
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Memory", "RAM"),
		                     new Image(RAMPeer.class.getResourceAsStream("/images/RAM.png")),
		                     new Properties(SEPARATE_LOAD_STORE_PORTS));
	}
	
	private final PortConnection clockConnection;
	
	public RAMPeer(Properties props, int x, int y) {
		super(x, y, 9, 5);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.ADDRESS_BITS);
		properties.ensureProperty(SEPARATE_LOAD_STORE_PORTS);
		properties.mergeIfExists(props);
		
		int addressBits = properties.getValue(Properties.ADDRESS_BITS);
		int dataBits = properties.getValue(Properties.BITSIZE);
		boolean separateLoadStore = properties.getValue(SEPARATE_LOAD_STORE_PORTS);
		
		RAM ram = new RAM(properties.getValue(Properties.LABEL), dataBits, addressBits, separateLoadStore);
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ADDRESS), "Address", 0, 2));
		connections.add(clockConnection = new PortConnection(this, ram.getPort(RAM.PORT_CLK), "Clock", 3,
		                                                     getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_ENABLE), "Enable", 4, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_LOAD), "Load", 5, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(RAM.PORT_DATA), "Data", getWidth(), 2));
		if (separateLoadStore) {
			connections.add(new PortConnection(this, ram.getPort(RAM.PORT_DATA_IN), "Data Input", 0, 4));
			connections.add(new PortConnection(this, ram.getPort(RAM.PORT_STORE), "Store", 6, getHeight()));
			connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLEAR), "Clear", 7, getHeight()));
		} else {
			connections.add(new PortConnection(this, ram.getPort(RAM.PORT_CLEAR), "Clear", 6, getHeight()));
		}
		
		init(ram, properties, connections);
	}
	
	public boolean isSeparateLoadStore() {
		return getComponent().isSeparateLoadStore();
	}
	
	private final AtomicBoolean isEditorOpen = new AtomicBoolean(false);
	
	@Override
	public List<MenuItem> getContextMenuItems(CircuitManager circuit) {
		MenuItem menuItem = new MenuItem("Edit contents");
		menuItem.setOnAction(event -> {
			RAM ram = getComponent();
			
			PropertyMemoryValidator
				memoryValidator =
				new PropertyMemoryValidator(ram.getAddressBits(), ram.getDataBits());
			
			List<MemoryLine> memory = new ArrayList<>();
			BiConsumer<Integer, Integer> listener = (address, data) -> {
				int index = address / 16;
				MemoryLine line = memory.get(index);
				line.values.get(address - index * 16).setValue(memoryValidator.formatValue(data));
			};
			
			if (isEditorOpen.getAndSet(true)) {
				return;
			}
			
			try {
				// Internal state can change in between and data can get out of sync
				CircuitSim simulatorWindow = circuit.getSimulatorWindow();
				simulatorWindow.getSimulator().runSync(() -> {
					ram.addMemoryListener(listener);
					
					CircuitState currentState = circuit.getCircuitBoard().getCurrentState();
					memory.addAll(memoryValidator.parse(ram.getMemoryContents(currentState), (address, value) -> {
						simulatorWindow.getSimulator().runSync(() -> {
							// Component has been removed
							if (ram.getCircuit() == null) {
								return;
							}
							
							ram.store(currentState, address, value);
						});
					}));
				});
				
				memoryValidator.createAndShowMemoryWindow(simulatorWindow.getStage(), memory);
			} finally {
				ram.removeMemoryListener(listener);
				isEditorOpen.set(false);
			}
		});
		return Collections.singletonList(menuItem);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawClockInput(graphics, clockConnection, Direction.SOUTH);
		
		WireValue addressVal = circuitState.getLastReceived(getComponent().getPort(RAM.PORT_ADDRESS));
		WireValue valueVal;
		if (addressVal.isValidValue()) {
			int val = getComponent().load(circuitState, addressVal.getValue());
			valueVal = WireValue.of(val, getComponent().getDataBits());
		} else {
			valueVal = new WireValue(getComponent().getDataBits());
		}
		
		String address = addressVal.toHexString();
		String value = valueVal.toHexString();
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		graphics.setFont(GuiUtils.getFont(11, true));
		
		String text = "RAM";
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), text);
		graphics.setFill(Color.BLACK);
		graphics.fillText(text, x + (width - bounds.getWidth()) * 0.5, y + (height + bounds.getHeight()) * 0.2);
		
		// Draw address
		text = "A: " + address;
		double addrY = y + bounds.getHeight() + 12;
		graphics.fillText(text, x + 13, addrY);
		
		// Draw data afterward
		bounds = GuiUtils.getBounds(graphics.getFont(), text);
		graphics.fillText("D: " + value, x + 13, addrY + bounds.getHeight());
		
		graphics.setFill(Color.GRAY);
		graphics.setFont(GuiUtils.getFont(10));
		graphics.fillText("A", x + 3, y + height * 0.5 - 1);
		graphics.fillText("D", x + width - 9, y + height * 0.5 - 1);
		graphics.fillText("en", x + width * 0.5 - 11.5, y + height - 3.5);
		graphics.fillText("L", x + width * 0.5 + 2, y + height - 3.5);
		
		if (isSeparateLoadStore()) {
			graphics.fillText("Din", x + 3, y + height * 0.5 + 20);
			graphics.fillText("St", x + width * 0.5 + 8.5, y + height - 3.5);
			graphics.fillText("0", x + width * 0.5 + 21.5, y + height - 3.5);
		} else {
			graphics.fillText("0", x + width * 0.5 + 11.5, y + height - 3.5);
		}
	}
}
