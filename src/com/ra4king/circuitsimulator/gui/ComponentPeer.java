package com.ra4king.circuitsimulator.gui;

import com.ra4king.circuitsimulator.simulator.Component;

/**
 * @author Roi Atalla
 */
public abstract class ComponentPeer<C extends Component> extends GuiElement {
	private C component;
	
	public ComponentPeer(C component, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.component = component;
	}
	
	public C getComponent() {
		return component;
	}
	
	@Override
	public String toString() {
		return getComponent().toString();
	}
}
