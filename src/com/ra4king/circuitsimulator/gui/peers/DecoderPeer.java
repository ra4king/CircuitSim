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
import com.ra4king.circuitsimulator.simulator.components.Decoder;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class DecoderPeer extends ComponentPeer<Decoder> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Plexer", "Decoder"),
		                     new Image(DemultiplexerPeer.class.getResourceAsStream("/resources/Decoder.png")),
		                     new Properties());
	}
	
	public DecoderPeer(Properties props, int x, int y) {
		super(x, y, 3, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.SELECTOR_BITS);
		properties.mergeIfExists(props);
		
		Decoder decoder = new Decoder(properties.getValue(Properties.LABEL),
		                              properties.getValue(Properties.SELECTOR_BITS));
		setHeight(decoder.getNumOutputs() + 2);
		
		List<Connection> connections = new ArrayList<>();
		for(int i = 0; i < decoder.getNumOutputs(); i++) {
			connections.add(new PortConnection(this, decoder.getOutputPort(i), getWidth(), i + 1));
		}
		
		connections.add(new PortConnection(this, decoder.getSelectorPort(), "Selector", getWidth() / 2, getHeight()));
		
		init(decoder, properties, connections);
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
