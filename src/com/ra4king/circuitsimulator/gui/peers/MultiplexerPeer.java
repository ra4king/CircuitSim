package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Multiplexer;

import javafx.scene.canvas.GraphicsContext;
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
	
	public MultiplexerPeer(Properties props, int x, int y) {
		super(x, y, 2, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(SELECTOR_BITS);
		properties.mergeIfExists(props);
		
		Multiplexer mux = new Multiplexer(properties.getValue(Properties.LABEL),
		                                  properties.getIntValue(Properties.BITSIZE),
		                                  properties.getIntValue(SELECTOR_BITS));
		setHeight(mux.NUM_IN_PORTS + 1);
		
		List<Connection> connections = new ArrayList<>();
		for(int i = 0; i < mux.NUM_IN_PORTS; i++) {
			connections.add(new PortConnection(this, mux.getPort(i), 0, i + 1));
		}
		
		connections.add(new PortConnection(this, mux.getPort(mux.PORT_SEL), 1, getHeight()));
		connections.add(new PortConnection(this, mux.getPort(mux.PORT_OUT), getWidth(), (mux.NUM_IN_PORTS + 1) / 2));
		
		init(mux, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.strokeText(getComponent().toString(), getScreenX() + 2, getScreenY() + getScreenHeight() / 2 + 5);
	}
}
