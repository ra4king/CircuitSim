package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsimulator.gui.Properties.PropertyValidator;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Splitter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class SplitterPeer extends ComponentPeer<Splitter> {
	private static final Property<Integer> FANOUTS;
	
	static {
		List<Integer> fanOuts = new ArrayList<>();
		for(int i = 1; i <= 32; i++) {
			fanOuts.add(i);
		}
		
		FANOUTS = new Property<>("Fanouts", new PropertyListValidator<>(fanOuts), 1);
	}
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Splitter"),
		                     new Image(SplitterPeer.class.getResourceAsStream("/resources/Splitter.png")),
		                     new Properties());
	}
	
	public SplitterPeer(Properties props, int x, int y) {
		super(x, y, 0, 1);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.DIRECTION);
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
		
		setWidth(Math.max(1, splitter.getNumPorts() - 1));
		
		int[] bitFanIndices = splitter.getBitFanIndices();
		for(int i = 0; i < bitFanIndices.length; i++) {
			properties.setProperty(new Property<>("Bit " + i, validator, bitFanIndices[i]));
		}
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED), 0, 0));
		for(int i = 0; i < splitter.getNumPorts() - 1; i++) {
			String tooltip = "";
			for(int j = 0, start = -1; j < bitFanIndices.length; j++) {
				if(bitFanIndices[j] == splitter.getNumPorts() - 2 - i) {
					if(start == -1 || j == bitFanIndices.length - 1) {
						tooltip += (start == -1 ? "," : "-") + j;
						start = j;
					}
				} else if(start != -1) {
					if(start < j - 1) {
						tooltip += "-" + (j - 1);
					}
					
					start = -1;
				}
			}
			
			connections.add(new PortConnection(this, splitter.getPort(splitter.getNumPorts() - 2 - i),
			                                   tooltip.isEmpty() ? tooltip : tooltip.substring(1),
			                                   i + 1, getHeight()));
		}
		
		connections = GuiUtils.rotatePorts(connections, Direction.EAST, properties.getValue(Properties.DIRECTION));
		GuiUtils.rotateElement(this, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		init(splitter, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext g, CircuitState circuitState) {
		GuiUtils.rotateGraphics(this, g, getProperties().getValue(Properties.DIRECTION));
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth() > getScreenHeight() ? getScreenWidth() : getScreenHeight();
		int height = getScreenWidth() > getScreenHeight() ? getScreenHeight() : getScreenWidth();
		
		g.setLineWidth(2);
		g.setStroke(Color.BLACK);
		g.strokeLine(x, y, x + GuiUtils.BLOCK_SIZE, y + height);
		g.strokeLine(x + GuiUtils.BLOCK_SIZE, y + height, x + width, y + height);
	}
}
