package com.ra4king.circuitsim.gui;

import java.util.Collections;
import java.util.List;

import com.ra4king.circuitsim.simulator.CircuitState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;

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
	
	public int getScreenX() {
		return getX() * GuiUtils.BLOCK_SIZE;
	}
	
	public int getScreenY() {
		return getY() * GuiUtils.BLOCK_SIZE;
	}
	
	public int getScreenWidth() {
		return getWidth() * GuiUtils.BLOCK_SIZE;
	}
	
	public int getScreenHeight() {
		return getHeight() * GuiUtils.BLOCK_SIZE;
	}
	
	public boolean containsScreenCoord(int x, int y) {
		return x >= getScreenX() && x < getScreenX() + getScreenWidth() && y >= getScreenY() && y < getScreenY() +
				                                                                                            getScreenHeight();
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
	
	public boolean isWithin(GuiElement element) {
		return isWithin(element.getX(), element.getY(), element.getWidth(), element.getHeight());
	}
	
	public boolean isWithinScreenCoord(int x, int y, int width, int height) {
		int screenX = getScreenX();
		int screenY = getScreenY();
		int screenWidth = getScreenWidth();
		int screenHeight = getScreenHeight();
		return screenX >= x && screenX + screenWidth <= x + width &&
				       screenY >= y && screenY + screenHeight <= y + height;
	}
	
	public boolean isWithin(int x, int y, int width, int height) {
		return getX() >= x && getX() + getWidth() <= x + width && getY() >= y && getY() + getHeight() <= y + height;
	}
	
	public boolean intersects(GuiElement element) {
		return intersects(element.getX(), element.getY(), element.getWidth(), element.getHeight());
	}
	
	public boolean intersectsScreenCoord(int x, int y, int width, int height) {
		return !(x >= getScreenX() + getScreenWidth() ||
				         getScreenX() >= x + width ||
				         y >= getScreenY() + getScreenHeight() ||
				         getScreenY() >= y + height);
	}
	
	public boolean intersects(int x, int y, int width, int height) {
		return !(x >= getX() + getWidth() ||
				         getX() >= x + width ||
				         y >= getY() + getHeight() ||
				         getY() >= y + height);
	}
	
	public void mousePressed(CircuitManager manager, CircuitState state, double x, double y) {}
	
	public void mouseReleased(CircuitManager manager, CircuitState state, double x, double y) {}
	
	public void mouseEntered(CircuitManager manager, CircuitState state) {}
	
	public void mouseExited(CircuitManager manager, CircuitState state) {}
	
	public boolean keyPressed(CircuitManager manager, CircuitState state, KeyCode keyCode, String text) {
		return false;
	}
	
	public void keyTyped(CircuitManager manager, CircuitState state, String character) {}
	
	public void keyReleased(CircuitManager manager, CircuitState state, KeyCode keyCode, String text) {}
	
	public List<MenuItem> getContextMenuItems(CircuitManager circuit) {
		return Collections.emptyList();
	}
	
	public abstract List<? extends Connection> getConnections();
	
	public abstract void paint(GraphicsContext graphics, CircuitState circuitState);
}
