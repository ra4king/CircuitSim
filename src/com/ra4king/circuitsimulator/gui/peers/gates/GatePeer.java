package com.ra4king.circuitsimulator.gui.peers.gates;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;

import javafx.scene.canvas.GraphicsContext;

/**
 * @author Roi Atalla
 */
public abstract class GatePeer extends ComponentPeer<Gate> {
	public GatePeer(Properties props, int x, int y) {
		this(props, x, y, 4, 4);
	}
	
	public GatePeer(Properties props, int x, int y, int width, int height) {
		super(x, y, width, height);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		ensureProperties(properties);
		properties.mergeIfExists(props);
		
		Gate gate = buildGate(properties);
		
		List<PortConnection> connections = new ArrayList<>();
		int gateNum = gate.getNumPorts() - 1;
		for(int i = 0; i < gateNum; i++) {
			int add = (gateNum % 2 == 0 && i >= gateNum / 2) ? 3 : 2;
			connections.add(
					new PortConnection(this, gate.getPort(i), 0, i + add - gateNum / 2 - (gateNum == 1 ? 1 : 0)));
		}
		
		connections.add(new PortConnection(this, gate.getPort(gateNum), getWidth(), getHeight() / 2));
		
		connections = GuiUtils.rotatePorts(connections, Direction.EAST, properties.getValue(Properties.DIRECTION));
		GuiUtils.rotateElementSize(this, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		init(gate, properties, connections);
	}
	
	protected abstract void ensureProperties(Properties properties);
	
	public abstract Gate buildGate(Properties properties);
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		GuiUtils.rotateGraphics(this, graphics, getProperties().getValue(Properties.DIRECTION));
	}
}
