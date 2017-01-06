package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Splitter;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class SplitterPeer extends ComponentPeer<Splitter> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Splitter"),
		                     null,//new Image(SplitterPeer.class.getResourceAsStream("/resources/Splitter.png")),
		                     new Properties());
	}
	
	public SplitterPeer(Properties props, int x, int y) {
		super(x, y, 0, 1);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.NUM_INPUTS);
		properties.mergeIfExists(props);
		
		Splitter splitter = new Splitter(properties.getValue(Properties.LABEL),
		                                 properties.getIntValue(Properties.BITSIZE),
		                                 properties.getIntValue(Properties.NUM_INPUTS));
		setWidth(Math.max(2, splitter.getNumPorts() - 1));
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, splitter.getPort(splitter.PORT_JOINED), 0, 0));
		for(int i = 0; i < splitter.getNumPorts() - 1; i++) {
			connections.add(
					new PortConnection(this, splitter.getPort(splitter.getNumPorts() - 2 - i), i + 1, getHeight()));
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
