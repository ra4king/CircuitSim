package com.ra4king.circuitsimulator.gui;

import java.util.List;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;

/**
 * @author Roi Atalla
 */
public abstract class ComponentPeer<C extends Component> extends GuiElement {
	private C component;
	private Properties properties;
	private List<PortConnection> connections;
	
	public ComponentPeer(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	protected final void init(C component, Properties properties, List<PortConnection> connections) {
		if(this.component != null) {
			throw new IllegalStateException("ComponentPeer already initialized.");
		}
		
		this.component = component;
		this.properties = properties;
		this.connections = connections;
	}
	
	public C getComponent() {
		return component;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	@Override
	public List<PortConnection> getConnections() {
		return connections;
	}
	
	public void clicked(CircuitState state, double x, double y) {}
	
	@Override
	public String toString() {
		return getComponent().toString();
	}
}
