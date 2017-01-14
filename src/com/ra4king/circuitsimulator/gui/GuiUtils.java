package com.ra4king.circuitsimulator.gui;

import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
	
	public static Bounds getBounds(Font font, String string) {
		Text text = new Text(string);
		text.setFont(font);
		return text.getLayoutBounds();
	}
	
	public interface Drawable {
		void draw(int x, int y, int width, int height);
	}
	
	public static void drawShape(Drawable drawable, GuiElement element) {
		drawable.draw(element.getScreenX(), element.getScreenY(), element.getScreenWidth(), element.getScreenHeight());
	}
	
	public static void setBitColor(GraphicsContext graphics, CircuitState circuitState, LinkWires linkWires) {
		if(linkWires.isLinkValid()) {
			Link link = linkWires.getLink();
			if(link != null) {
				if(circuitState.isShortCircuited(link)) {
					graphics.setStroke(Color.RED);
					graphics.setFill(Color.RED);
				} else {
					setBitColor(graphics, circuitState.getMergedValue(link));
				}
			} else {
				setBitColor(graphics, new WireValue(1));
			}
		} else {
			graphics.setStroke(Color.ORANGE);
			graphics.setFill(Color.ORANGE);
		}
	}
	
	public static void setBitColor(GraphicsContext graphics, WireValue value) {
		setBitColor(graphics, value, Color.BLACK);
	}
	
	public static void setBitColor(GraphicsContext graphics, WireValue value, Color defaultColor) {
		if(value.getBitSize() == 1) {
			setBitColor(graphics, value.getBit(0));
		} else {
			graphics.setStroke(defaultColor);
			graphics.setFill(defaultColor);
		}
	}
	
	public static void setBitColor(GraphicsContext graphics, State bitState) {
		switch(bitState) {
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
	}
}
