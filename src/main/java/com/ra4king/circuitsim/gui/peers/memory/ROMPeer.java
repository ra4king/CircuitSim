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
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyMemoryValidator;
import com.ra4king.circuitsim.gui.properties.PropertyMemoryValidator.MemoryLine;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.memory.ROM;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ROMPeer extends ComponentPeer<ROM> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Memory", "ROM"),
			new Image(ROMPeer.class.getResourceAsStream("/images/ROM.png")),
			new Properties());
	}
	
	private final Property<List<MemoryLine>> contentsProperty;
	
	public ROMPeer(Properties props, int x, int y) {
		super(x, y, 9, 5);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.ADDRESS_BITS);
		properties.mergeIfExists(props);
		
		int addressBits = properties.getValue(Properties.ADDRESS_BITS);
		int dataBits = properties.getValue(Properties.BITSIZE);
		
		contentsProperty = new Property<>("Contents", new PropertyMemoryValidator(addressBits, dataBits), null);
		String oldMemory;
		Property<?> oldContents = props.getProperty("Contents");
		if (oldContents == null) {
			oldMemory = "";
		} else if (oldContents.validator == null) {
			oldMemory = props.getValue("Contents");
		} else {
			oldMemory = oldContents.getStringValue();
		}
		properties.setValue(contentsProperty, contentsProperty.validator.parse(oldMemory));
		
		int[] memory = memoryToArray(properties.getValue(contentsProperty));
		ROM ram = new ROM(properties.getValue(Properties.LABEL), dataBits, addressBits, memory);
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, ram.getPort(ROM.PORT_ADDRESS), "Address", 0, 2));
		connections.add(new PortConnection(this, ram.getPort(ROM.PORT_ENABLE), "Enable", 4, getHeight()));
		connections.add(new PortConnection(this, ram.getPort(ROM.PORT_DATA), "Data", getWidth(), 2));
		
		init(ram, properties, connections);
	}
	
	private static int[] memoryToArray(List<MemoryLine> lines) {
		if (lines == null) {
			return new int[0];
		}
		
		return lines
			.stream()
			.flatMap(line -> line.values.stream())
			.mapToInt(prop -> Integer.parseUnsignedInt(prop.get(), 16))
			.toArray();
	}
	
	private final AtomicBoolean isEditorOpen = new AtomicBoolean(false);
	
	@Override
	public List<MenuItem> getContextMenuItems(CircuitManager circuit) {
		MenuItem menuItem = new MenuItem("Edit contents");
		menuItem.setOnAction(event -> {
			ROM rom = getComponent();
			
			Property<List<MemoryLine>> property = getProperties().getProperty(contentsProperty.name);
			PropertyMemoryValidator memoryValidator = (PropertyMemoryValidator)property.validator;
			
			List<MemoryLine> lines = new ArrayList<>();
			BiConsumer<Integer, Integer> listener = (address, data) -> {
				int index = address / 16;
				MemoryLine line = property.value.get(index);
				line.values.get(address - index * 16).setValue(memoryValidator.formatValue(data));
				circuit.getCircuit().forEachState(state -> rom.valueChanged(state, null, 0));
			};
			
			if (isEditorOpen.getAndSet(true)) {
				return;
			}
			
			try {
				CircuitSim simulatorWindow = circuit.getSimulatorWindow();
				simulatorWindow.getSimulator().runSync(() -> {
					lines.addAll(memoryValidator.parse(rom.getMemory(), (address, newValue) -> {
						simulatorWindow.getSimulator().runSync(() -> {
							// Component has been removed
							if (rom.getCircuit() == null) {
								return;
							}
							
							int oldValue = rom.load(address);
							rom.store(address, newValue);
							
							simulatorWindow.getEditHistory().addCustomAction(
								circuit,
								"Edit ROM",
								() -> rom.store(address, oldValue),
								() -> rom.store(address, newValue));
						});
					}));
					
					rom.addMemoryListener(listener);
				});
				
				memoryValidator.createAndShowMemoryWindow(simulatorWindow.getStage(), lines);
			} finally {
				rom.removeMemoryListener(listener);
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
		
		String address = circuitState.getLastReceived(getComponent().getPort(ROM.PORT_ADDRESS)).toHexString();
		String value = circuitState.getLastPushed(getComponent().getPort(ROM.PORT_DATA)).toHexString();
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		graphics.setFont(GuiUtils.getFont(11, true));
		
		String text = "ROM";
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
	}
}
