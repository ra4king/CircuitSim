package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsim.gui.Properties.PropertyValidator;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.wiring.Splitter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class SplitterPeer extends ComponentPeer<Splitter> {
	private static final Property<Integer> FANOUTS;
	private static final Property<Boolean> INPUT_LOCATION;
	
	static {
		List<Integer> fanOuts = new ArrayList<>();
		for(int i = 1; i <= 32; i++) {
			fanOuts.add(i);
		}
		
		FANOUTS = new Property<>("Fanouts", new PropertyListValidator<>(fanOuts), 2);
		
		INPUT_LOCATION = new Property<>("Input location", Properties.LOCATION_VALIDATOR, true);
	}
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Splitter"),
		                     new Image(SplitterPeer.class.getResourceAsStream("/Splitter.png")),
		                     new Properties(new Property<>(Properties.BITSIZE, 2)));
	}
	
	public SplitterPeer(Properties props, int x, int y) {
		super(x, y, 2, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(INPUT_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(FANOUTS);
		properties.mergeIfExists(props);
		
		int bitSize = properties.getValue(Properties.BITSIZE);
		int numInputs = properties.getValue(FANOUTS);
		
		List<Integer> fanOuts = new ArrayList<>();
		for(int i = -1; i < numInputs; i++) {
			fanOuts.add(i);
		}
		
		PropertyValidator<Integer> validator =
				new PropertyListValidator<>(fanOuts, (value) -> value == -1 ? "None" : value.toString());
		
		Splitter splitter;
		
		int availableBits = 0;
		while(props.containsProperty("Bit " + availableBits)) availableBits++;
		
		if(availableBits == bitSize) {
			int[] bitFanIndices = new int[bitSize];
			
			for(int i = 0; i < bitFanIndices.length; i++) {
				Object value = props.getValue("Bit " + i);
				int index;
				if(value == null) {
					index = i;
				} else if(value instanceof String) {
					index = validator.parse((String)value);
				} else {
					index = (Integer)value;
				}
				bitFanIndices[i] = Math.min(numInputs - 1, index);
			}
			
			splitter = new Splitter(properties.getValue(Properties.LABEL), bitFanIndices);
		} else {
			splitter = new Splitter(properties.getValue(Properties.LABEL), bitSize, numInputs);
		}
		
		setHeight(Math.max(2, splitter.getNumPorts()));
		
		int[] bitFanIndices = splitter.getBitFanIndices();
		for(int i = 0; i < bitFanIndices.length; i++) {
			properties.setProperty(new Property<>("Bit " + i, validator, bitFanIndices[i]));
		}
		
		Direction direction = properties.getValue(Properties.DIRECTION);
		boolean inputOnTopLeft = properties.getValue(INPUT_LOCATION);
		
		GuiUtils.rotateElementSize(this, Direction.EAST, direction);
		
		List<PortConnection> connections = new ArrayList<>();
		for(int i = 0; i < splitter.getNumPorts() - 1; i++) {
			StringBuilder tooltip = new StringBuilder();
			for(int j = 0, start = -1; j < bitFanIndices.length; j++) {
				if(bitFanIndices[j] == splitter.getNumPorts() - 2 - i) {
					if(start == -1 || j == bitFanIndices.length - 1) {
						tooltip.append(start == -1 ? ',' : '-').append(j);
						start = j;
					}
				} else if(start != -1) {
					if(start < j - 1) {
						tooltip.append('-').append(j - 1);
					}
					
					start = -1;
				}
			}
			
			int cx, cy;
			switch(direction) {
				case EAST:
					cx = getWidth();
					cy = inputOnTopLeft ? i + 2 : getHeight() - i - 2;
					break;
				case WEST:
					cx = 0;
					cy = inputOnTopLeft ? i + 2 : getHeight() - i - 2;
					break;
				case SOUTH:
					cx = inputOnTopLeft ? i + 2 : getWidth() - i - 2;
					cy = getHeight();
					break;
				case NORTH:
					cx = inputOnTopLeft ? i + 2 : getWidth() - i - 2;
					cy = 0;
					break;
				default:
					throw new IllegalArgumentException("Why are you doing this?");
			}
			
			connections.add(new PortConnection(this, splitter.getPort(splitter.getNumPorts() - 2 - i),
			                                   tooltip.length() == 0 ? tooltip.toString() : tooltip.substring(1),
			                                   cx, cy));
		}
		
		switch(direction) {
			case EAST:
				connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED),
				                                   0, inputOnTopLeft ? 0 : getHeight()));
				break;
			case WEST:
				connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED),
				                                   getWidth(), inputOnTopLeft ? 0 : getHeight()));
				break;
			case SOUTH:
				connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED),
				                                   inputOnTopLeft ? 0 : getWidth(), 0));
				break;
			case NORTH:
				connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED),
				                                   inputOnTopLeft ? 0 : getWidth(), getHeight()));
				break;
		}
		
		init(splitter, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		Direction direction = getProperties().getValue(Properties.DIRECTION);
		boolean inputOnTop = getProperties().getValue(INPUT_LOCATION);
		switch(direction) {
			case SOUTH:
			case WEST:
				inputOnTop = !inputOnTop;
			case EAST:
			case NORTH:
				GuiUtils.rotateGraphics(this, graphics, direction);
				
				int x = getScreenX();
				int y = getScreenY();
				int height = getScreenWidth() > getScreenHeight() ? getScreenWidth() : getScreenHeight();
				
				graphics.setLineWidth(3);
				graphics.setStroke(Color.BLACK);
				
				graphics.strokeLine(x,
				                    y + (inputOnTop ? 0 : height),
				                    x + GuiUtils.BLOCK_SIZE,
				                    y + (inputOnTop ? GuiUtils.BLOCK_SIZE : height - GuiUtils.BLOCK_SIZE));
				
				graphics.strokeLine(x + GuiUtils.BLOCK_SIZE,
				                    y + (inputOnTop ? GuiUtils.BLOCK_SIZE : height - GuiUtils.BLOCK_SIZE),
				                    x + GuiUtils.BLOCK_SIZE,
				                    y + (inputOnTop ? height : 0));
				
				for(int i = 2 * GuiUtils.BLOCK_SIZE; i <= height; i += GuiUtils.BLOCK_SIZE) {
					int offset = inputOnTop ? 0 : 2 * GuiUtils.BLOCK_SIZE;
					graphics.strokeLine(x + GuiUtils.BLOCK_SIZE,
					                    y + i - offset,
					                    x + 2 * GuiUtils.BLOCK_SIZE,
					                    y + i - offset);
				}
		}
	}
}
