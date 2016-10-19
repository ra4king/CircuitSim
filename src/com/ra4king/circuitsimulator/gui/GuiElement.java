package com.ra4king.circuitsimulator.gui;

import java.awt.Graphics2D;
import java.util.List;

import com.ra4king.circuitsimulator.simulator.CircuitState;

/**
 * @author Roi Atalla
 */
public abstract class GuiElement {
	private int x, y;
	private int width, height;
	
	public GuiElement(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public boolean contains(int x, int y) {
		return x >= getX() && x < getX() + getWidth() && y >= getY() && y < getY() + getHeight();
	}
	
	public boolean intersects(int x, int y, int width, int height) {
		return !(x >= getX() + getWidth() ||
				         getX() >= x + width ||
				         y >= getY() + getHeight() ||
				         getY() >= y + height);
	}
	
	public abstract List<Connection> getConnections();
	
	public abstract void paint(Graphics2D g, CircuitState circuitState);
}
