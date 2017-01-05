package com.ra4king.circuitsimulator.gui;

import java.util.List;

import com.ra4king.circuitsimulator.simulator.Component;

/**
 * @author Roi Atalla
 */
public abstract class ComponentPeer<C extends Component> extends GuiElement {
	private C component;
	private Properties properties;
	private List<Connection> connections;
	
	public ComponentPeer(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	protected final void init(C component, Properties properties, List<Connection> connections) {
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
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public String toString() {
		return getComponent().toString();
	}
}
