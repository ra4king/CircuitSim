package com.ra4king.circuitsimulator.gui.peers.gates;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;

/**
 * @author Roi Atalla
 */
public abstract class GatePeer extends ComponentPeer<Gate> {
	public GatePeer(Circuit circuit, Properties props, int x, int y) {
		this(circuit, props, x, y, 4, 4);
	}
	
	public GatePeer(Circuit circuit, Properties props, int x, int y, int width, int height) {
		super(x, y, width, height);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.merge(props);
		
		Gate gate = getGate(circuit, properties);
		
		List<Connection> connections = new ArrayList<>();
		int gateNum = gate.getNumPorts() - 1;
		for(int i = 0; i < gateNum; i++) {
			int add = (gateNum % 2 == 0 && i >= gateNum / 2) ? 3 : 2;
			connections.add(
					new PortConnection(this, gate.getPort(i), 0, i + add - gateNum / 2 - (gateNum == 1 ? 1 : 0)));
		}
		
		connections.add(new PortConnection(this, gate.getPort(gateNum), getWidth(), getHeight() / 2));
		
		init(gate, properties, connections);
	}
	
	public abstract Gate getGate(Circuit circuit, Properties properties);
}
