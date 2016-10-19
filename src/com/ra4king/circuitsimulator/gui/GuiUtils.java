package com.ra4king.circuitsimulator.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class GuiUtils {
	public interface Drawable {
		void draw(int x, int y, int width, int height);
	}
	
	public static void drawShape(Drawable drawable, GuiElement element) {
		drawable.draw(element.getX(), element.getY(), element.getWidth(), element.getHeight());
	}
	
	public static void setBitColor(Graphics2D g, WireValue value) {
		setBitColor(g, value, Color.BLACK);
	}
	
	public static void setBitColor(Graphics2D g, WireValue value, Color defaultColor) {
		if(value.getBitSize() == 1) {
			switch(value.getBit(0)) {
				case ONE:
					g.setColor(Color.GREEN);
					break;
				case ZERO:
					g.setColor(Color.GREEN.darker().darker());
					break;
				case X:
					g.setColor(Color.BLUE.brighter().brighter());
					break;
			}
		} else {
			g.setColor(defaultColor);
		}
	}
}
