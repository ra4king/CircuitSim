package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Subcircuit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class SubcircuitPeer extends ComponentPeer<Subcircuit> {
	public SubcircuitPeer(Circuit circuit, Properties props, Circuit parent, int x, int y) {
		super(x, y, 2, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.merge(props);
		
		Subcircuit subcircuit = circuit.addComponent(
				new Subcircuit(properties.getValue(Properties.LABEL), parent));
		setHeight(1 + (subcircuit.getNumPorts() + 1) / 2);
		
		List<Connection> connections = new ArrayList<>();
		for(int i = 0; i < subcircuit.getNumPorts(); i++) {
			int connX = i < ((subcircuit.getNumPorts() + 1) / 2) ? 0 : getWidth();
			int connY = 1 + (i % ((subcircuit.getNumPorts() + 1) / 2));
			connections.add(new PortConnection(this, subcircuit.getPort(i), subcircuit.getPins().get(i).getName(),
			                                   connX, connY));
		}
		
		init(subcircuit, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
	}
}
