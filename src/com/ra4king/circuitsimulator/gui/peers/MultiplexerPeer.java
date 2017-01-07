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
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Multiplexer;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class MultiplexerPeer extends ComponentPeer<Multiplexer> {
	private static final Property SELECTOR_BITS;
	
	static {
		List<String> selBits = new ArrayList<>();
		for(int i = 1; i <= 8; i++) {
			selBits.add(String.valueOf(i));
		}
		SELECTOR_BITS = new Property("Selector bits", new PropertyListValidator(selBits), "1");
	}
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Mux"),
		                     new Image(MultiplexerPeer.class.getResourceAsStream("/resources/Mux.png")),
		                     new Properties());
	}
	
	public MultiplexerPeer(Properties props, int x, int y) {
		super(x, y, 3, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(SELECTOR_BITS);
		properties.mergeIfExists(props);
		
		Multiplexer mux = new Multiplexer(properties.getValue(Properties.LABEL),
		                                  properties.getIntValue(Properties.BITSIZE),
		                                  properties.getIntValue(SELECTOR_BITS));
		setHeight(mux.getNumInputs() + 2);
		
		List<Connection> connections = new ArrayList<>();
		for(int i = 0; i < mux.getNumInputs(); i++) {
			connections.add(new PortConnection(this, mux.getPort(i), 0, i + 1));
		}
		
		connections.add(new PortConnection(this, mux.getSelectorPort(), "Selector", getWidth() / 2, getHeight()));
		connections.add(new PortConnection(this, mux.getOutPort(), "Out", getWidth(), getHeight() / 2));
		
		init(mux, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(getComponent().getName(),
			                    getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
			                    getScreenY() - 5);
		}
		
		graphics.beginPath();
		graphics.moveTo(getScreenX(), getScreenY());
		graphics.lineTo(getScreenX() + getScreenWidth(), getScreenY() + Math.min(20, getScreenHeight() * 0.2));
		graphics.lineTo(getScreenX() + getScreenWidth(), getScreenY() + getScreenHeight()
				                                                 - Math.max(5, getScreenHeight() * 0.1));
		graphics.lineTo(getScreenX(), getScreenY() + getScreenHeight());
		graphics.closePath();
		
		graphics.setFill(Color.WHITE);
		graphics.fill();
		graphics.setStroke(Color.BLACK);
		graphics.stroke();
		
		graphics.setStroke(Color.DARKGRAY);
		graphics.strokeText("0", getScreenX() + 2, getScreenY() + 13);
	}
}
