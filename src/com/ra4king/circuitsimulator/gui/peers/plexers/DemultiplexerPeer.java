package com.ra4king.circuitsimulator.gui.peers.plexers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.plexers.Demultiplexer;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class DemultiplexerPeer extends ComponentPeer<Demultiplexer> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Plexer", "Demux"),
		                     new Image(DemultiplexerPeer.class.getResourceAsStream("/resources/Demux.png")),
		                     new Properties());
	}
	
	public DemultiplexerPeer(Properties props, int x, int y) {
		super(x, y, 3, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.SELECTOR_BITS);
		properties.mergeIfExists(props);
		
		Demultiplexer demux = new Demultiplexer(properties.getValue(Properties.LABEL),
		                                        properties.getValue(Properties.BITSIZE),
		                                        properties.getValue(Properties.SELECTOR_BITS));
		setHeight(demux.getNumOutputs() + 2);
		
		List<PortConnection> connections = new ArrayList<>();
		for(int i = 0; i < demux.getNumOutputs(); i++) {
			connections.add(new PortConnection(this, demux.getOutputPort(i), getWidth(), i + 1));
		}
		
		connections.add(new PortConnection(this, demux.getSelectorPort(), "Selector", getWidth() / 2 + 1, getHeight
				                                                                                                  ()));
		connections.add(new PortConnection(this, demux.getInputPort(), "In", 0, getHeight() / 2));
		
		init(demux, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		int block = GuiUtils.BLOCK_SIZE;
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(getComponent().getName(),
			                    x + (width - bounds.getWidth()) * 0.5,
			                    y - 5);
		}
		
		graphics.beginPath();
		graphics.moveTo(x + width, y);
		graphics.lineTo(x, y + Math.min(2 * block, height * 0.2));
		graphics.lineTo(x, y + height - Math.max(5, height * 0.1));
		graphics.lineTo(x + width, y + height);
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.fill();
		graphics.setStroke(Color.BLACK);
		graphics.stroke();
		
		graphics.setStroke(Color.DARKGRAY);
		graphics.strokeText("0", x + width - 10, y + 13);
	}
}
