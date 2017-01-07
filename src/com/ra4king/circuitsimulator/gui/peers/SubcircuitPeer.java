package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ra4king.circuitsimulator.gui.CircuitManager;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.gui.Properties.PropertyCircuitValidator;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Subcircuit;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class SubcircuitPeer extends ComponentPeer<Subcircuit> {
	public static final String SUBCIRCUIT = "Subcircuit";
	
	public SubcircuitPeer(Properties props, int x, int y) {
		super(x, y, 2, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(new Property(SUBCIRCUIT, null, ""));
		properties.mergeIfExists(props);
		
		Property subcircuitProperty = properties.getProperty(SUBCIRCUIT);
		CircuitManager subcircuitManager = ((PropertyCircuitValidator)subcircuitProperty.validator)
				                                   .getCircuitManager(subcircuitProperty.value);
		Subcircuit subcircuit = new Subcircuit(properties.getValue(Properties.LABEL), subcircuitManager.getCircuit());
		setHeight(1 + (subcircuit.getNumPorts() + 1) / 2);
		
		List<Connection> connections = new ArrayList<>();
		List<PinPeer> pins = subcircuitManager.getCircuitBoard()
		                                      .getComponents().stream()
		                                      .filter(componentPeer -> componentPeer instanceof PinPeer)
		                                      .map(componentPeer -> (PinPeer)componentPeer)
		                                      .sorted((o1, o2) -> {
			                                      int diff = o1.getX() - o2.getX();
			                                      if(diff == 0) {
				                                      return o1.getY() - o2.getY();
			                                      }
			                                      return diff;
		                                      })
		                                      .collect(Collectors.toList());
		
		if(pins.size() != subcircuit.getNumPorts()) {
			throw new IllegalStateException("Pin count and ports count don't match?");
		}
		
		for(int i = 0; i < pins.size(); i++) {
			int connX = i < ((pins.size() + 1) / 2) ? 0 : getWidth();
			int connY = 1 + (i % ((pins.size() + 1) / 2));
			connections.add(new PortConnection(this, subcircuit.getPort(pins.get(i).getComponent()),
			                                   pins.get(i).getComponent().getName(), connX, connY));
		}
		
		init(subcircuit, properties, connections);
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
		
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
