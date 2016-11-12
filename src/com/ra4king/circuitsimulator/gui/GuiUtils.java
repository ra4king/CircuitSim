package com.ra4king.circuitsimulator.gui;

import com.ra4king.circuitsimulator.simulator.WireValue;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public class GuiUtils {
	public static final int BLOCK_SIZE = 10;
	
	public static int getCircuitCoord(double a) {
		return ((int)Math.round(a) + BLOCK_SIZE / 2) / BLOCK_SIZE;
	}
	
	public static int getScreenCircuitCoord(double a) {
		return getCircuitCoord(a) * BLOCK_SIZE;
	}
	
	public interface Drawable {
		void draw(int x, int y, int width, int height);
	}
	
	public static void drawShape(Drawable drawable, GuiElement element) {
		drawable.draw(element.getScreenX(), element.getScreenY(), element.getScreenWidth(), element.getScreenHeight());
	}
	
	public static void setBitColor(GraphicsContext graphics, WireValue value) {
		setBitColor(graphics, value, Color.BLACK);
	}
	
	public static void setBitColor(GraphicsContext graphics, WireValue value, Color defaultColor) {
		if(value.getBitSize() == 1) {
			switch(value.getBit(0)) {
				case ONE:
					graphics.setStroke(Color.GREEN.brighter());
					graphics.setFill(Color.GREEN.brighter());
					break;
				case ZERO:
					graphics.setStroke(Color.GREEN.darker());
					graphics.setFill(Color.GREEN.darker());
					break;
				case X:
					graphics.setStroke(Color.BLUE.brighter());
					graphics.setFill(Color.BLUE.brighter());
					break;
			}
		} else {
			graphics.setStroke(defaultColor);
			graphics.setFill(defaultColor);
		}
	}
}
