package com.ra4king.circuitsimulator.gui;

import java.util.List;

import com.ra4king.circuitsimulator.simulator.CircuitState;

import javafx.scene.canvas.GraphicsContext;

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
	
	public boolean contains(GuiElement element) {
		return contains(element.getX(), element.getY(), element.getWidth(), element.getHeight());
	}
	
	public boolean contains(int x, int y, int width, int height) {
		return x >= getX() && x + width <= getX() + getWidth() && y >= getY() && y + height <= getY() + getHeight();
	}
	
	public boolean isContainedIn(GuiElement element) {
		return isContainedIn(element.getX(), element.getY(), element.getWidth(), element.getHeight());
	}
	
	public boolean isContainedIn(int x, int y, int width, int height) {
		return getX() >= x && getX() + getWidth() <= x + width && getY() >= y && getY() + getHeight() <= y + height;
	}
	
	public boolean intersects(GuiElement element) {
		return intersects(element.getX(), element.getY(), element.getWidth(), element.getHeight());
	}
	
	public boolean intersects(int x, int y, int width, int height) {
		return !(x >= getX() + getWidth() ||
				         getX() >= x + width ||
				         y >= getY() + getHeight() ||
				         getY() >= y + height);
	}
	
	public abstract List<Connection> getConnections();
	
	public abstract void paint(GraphicsContext graphics, CircuitState circuitState);
}
