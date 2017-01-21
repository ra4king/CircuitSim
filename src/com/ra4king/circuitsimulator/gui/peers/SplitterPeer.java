package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
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
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(FANOUTS);
		properties.mergeIfExists(props);
		
		int bitSize = properties.getValue(Properties.BITSIZE);
		int numInputs = properties.getValue(FANOUTS);
		
		Splitter splitter;
		if(props.containsProperty("Bit 0")) {
			int[] bitFanIndices = new int[bitSize];
			
			for(int i = 0; i < bitFanIndices.length; i++) {
				bitFanIndices[i] = Math.min(numInputs - 1, props.getValueOrDefault("Bit " + i, i));
			}
			
			splitter = new Splitter(properties.getValue(Properties.LABEL), bitFanIndices);
		} else {
			splitter = new Splitter(properties.getValue(Properties.LABEL), bitSize, numInputs);
		}
		
		List<Integer> fanOuts = new ArrayList<>();
		for(int i = -1; i < numInputs; i++) {
			fanOuts.add(i);
		}
		
		PropertyValidator<Integer> validator =
				new PropertyListValidator<>(fanOuts, (value) -> value == -1 ? "None" : value.toString());
		
		int[] bitFanIndices = splitter.getBitFanIndices();
		for(int i = 0; i < bitFanIndices.length; i++) {
			properties.setProperty(new Property<>("Bit " + i, validator, bitFanIndices[i]));
		}
		
		setWidth(Math.max(2, splitter.getNumPorts() - 1));
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED), 0, 0));
		for(int i = 0; i < splitter.getNumPorts() - 1; i++) {
			String tooltip = "";
			for(int j = 0, start = -1; j < bitFanIndices.length; j++) {
				if(bitFanIndices[j] == i) {
					if(start == -1 || j == 0) {
						start = j;
						tooltip += "," + j;
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
		
		init(splitter, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext g, CircuitState circuitState) {
		g.setLineWidth(2);
		g.setStroke(Color.BLACK);
		g.strokeLine(getScreenX(), getScreenY(), getScreenX() + GuiUtils.BLOCK_SIZE, getScreenY() + getScreenHeight());
		g.strokeLine(getScreenX() + GuiUtils.BLOCK_SIZE, getScreenY() + getScreenHeight(),
		             getScreenX() + getScreenWidth(), getScreenY() + getScreenHeight());
	}
}
