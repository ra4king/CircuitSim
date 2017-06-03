package com.ra4king.circuitsimulator.gui;

import static com.ra4king.circuitsimulator.gui.Properties.Direction.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
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
	
	private static class FontInfo {
		int size;
		boolean bold;
		boolean oblique;
		
		FontInfo(int size, boolean bold, boolean oblique) {
			this.size = size;
			this.bold = bold;
			this.oblique = oblique;
		}
		
		@Override
		public int hashCode() {
			return size ^ (bold ? 0x1000 : 0) ^ (oblique ? 0x2000 : 0);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other == null || !(other instanceof FontInfo)) {
				throw new IllegalArgumentException("Cannot be null or instance other than FontInfo");
			}
			
			FontInfo info = (FontInfo)other;
			return info.size == this.size && info.bold == this.bold && info.oblique == this.oblique;
		}
	}
	
	private static Map<FontInfo, Font> fonts = new HashMap<>();
	
	public static Font getFont(int size) {
		return getFont(size, false, false);
	}
	
	public static Font getFont(int size, boolean bold) {
		return getFont(size, bold, false);
	}
	
	public static Font getFont(int size, boolean bold, boolean oblique) {
		FontInfo info = new FontInfo(size, bold, oblique);
		
		if(fonts.containsKey(info)) {
			return fonts.get(info);
		} else {
			String fontFile;
			if(bold && oblique) {
				fontFile = "/resources/DejaVuSansMono-BoldOblique.ttf";
			} else if(bold) {
				fontFile = "/resources/DejaVuSansMono-Bold.ttf";
			} else if(oblique) {
				fontFile = "/resources/DejaVuSansMono-Oblique.ttf";
			} else {
				fontFile = "/resources/DejaVuSansMono.ttf";
			}
			
			Font font = Font.loadFont(GuiUtils.class.getResourceAsStream(fontFile), size);
			fonts.put(info, font);
			return font;
		}
	}
	
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
	
	public static void drawName(GraphicsContext graphics, ComponentPeer<?> component, Direction direction) {
		if(!component.getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), component.getComponent().getName());
			
			double x, y;
			switch(direction) {
				case EAST:
					x = component.getScreenX() + component.getScreenWidth() + 5;
					y = component.getScreenY() + (component.getScreenHeight() + bounds.getHeight()) * 0.4;
					break;
				case WEST:
					x = component.getScreenX() - bounds.getWidth() - 3;
					y = component.getScreenY() + (component.getScreenHeight() + bounds.getHeight()) * 0.4;
					break;
				case SOUTH:
					x = component.getScreenX() + (component.getScreenWidth() - bounds.getWidth()) * 0.5;
					y = component.getScreenY() + component.getScreenHeight() + bounds.getHeight();
					break;
				case NORTH:
					x = component.getScreenX() + (component.getScreenWidth() - bounds.getWidth()) * 0.5;
					y = component.getScreenY() - 5;
					break;
				default:
					throw new IllegalArgumentException("How can Direction be anything else??");
			}
			
			graphics.setFill(Color.BLACK);
			graphics.fillText(component.getComponent().getName(), x, y);
		}
	}
	
	public static void drawValue(GraphicsContext graphics, String string, int x, int y, int width) {
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), string);
		
		if(string.length() == 1) {
			graphics.fillText(string, x + (width - bounds.getWidth()) * 0.5, y + bounds.getHeight() * 0.75 + 1);
		} else {
			for(int i = 0, row = 1; i < string.length(); row++) {
				String sub = string.substring(i, i + Math.min(8, string.length() - i));
				i += sub.length();
				graphics.fillText(sub, x + 1, y + bounds.getHeight() * 0.75 * row + 1);
			}
		}
	}
	
	public static void setBitColor(GraphicsContext graphics, CircuitState circuitState, LinkWires linkWires) {
		if(linkWires.isLinkValid()) {
			Link link = linkWires.getLink();
			if(link != null && circuitState != null) {
				if(circuitState.isShortCircuited(link)) {
					graphics.setStroke(Color.RED);
					graphics.setFill(Color.RED);
				} else {
					try {
						setBitColor(graphics, circuitState.getMergedValue(link));
					} catch(Exception exc) {
						graphics.setStroke(Color.RED);
						graphics.setFill(Color.RED);
					}
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
	
	private static final Color ONE_COLOR = Color.GREEN.brighter();
	private static final Color ZERO_COLOR = Color.GREEN.darker();
	private static final Color X_COLOR = Color.BLUE.brighter();
	
	public static void setBitColor(GraphicsContext graphics, State bitState) {
		switch(bitState) {
			case ONE:
				graphics.setStroke(ONE_COLOR);
				graphics.setFill(ONE_COLOR);
				break;
			case ZERO:
				graphics.setStroke(ZERO_COLOR);
				graphics.setFill(ZERO_COLOR);
				break;
			case X:
				graphics.setStroke(X_COLOR);
				graphics.setFill(X_COLOR);
				break;
		}
	}
	
	public static PortConnection rotatePortCCW(PortConnection connection, boolean useWidth) {
		int x = connection.getXOffset();
		int y = connection.getYOffset();
		int width = useWidth ? connection.getParent().getWidth() : connection.getParent().getHeight();
		
		return new PortConnection(connection.getParent(),
		                          connection.getPort(),
		                          connection.getName(),
		                          y, width - x);
	}
	
	public static void rotatePorts(List<PortConnection> connections,
	                               Direction source,
	                               Direction destination) {
		List<Direction> order = Arrays.asList(Direction.EAST, NORTH, Direction.WEST, Direction.SOUTH);
		
		Stream<PortConnection> stream = connections.stream();
		
		int index = order.indexOf(source);
		boolean useWidth = true;
		while(order.get(index++ % order.size()) != destination) {
			boolean temp = useWidth;
			stream = stream.map(port -> rotatePortCCW(port, temp));
			useWidth = !useWidth;
		}
		
		List<PortConnection> newConns = stream.collect(Collectors.toList());
		connections.clear();
		connections.addAll(newConns);
	}
	
	public static void rotateElementSize(GuiElement element, Direction source, Direction destination) {
		List<Direction> order = Arrays.asList(Direction.EAST, NORTH, Direction.WEST, Direction.SOUTH);
		
		int index = order.indexOf(source);
		while(order.get(index++ % order.size()) != destination) {
			int width = element.getWidth();
			int height = element.getHeight();
			element.setWidth(height);
			element.setHeight(width);
		}
	}
	
	/**
	 * Source orientation is assumed EAST
	 */
	public static void rotateGraphics(ComponentPeer<?> component, GraphicsContext graphics, Direction direction) {
		int x = component.getScreenX();
		int y = component.getScreenY();
		int width = component.getScreenWidth();
		int height = component.getScreenHeight();
		
		graphics.translate(x + width * 0.5, y + height * 0.5);
		switch(direction) {
			case NORTH:
				graphics.rotate(270);
				graphics.translate(-x - height * 0.5, -y - width * 0.5);
				break;
			case SOUTH:
				graphics.rotate(90);
				graphics.translate(-x - height * 0.5, -y - width * 0.5);
				break;
			case WEST:
				graphics.rotate(180);
			default:
				graphics.translate(-x - width * 0.5, -y - height * 0.5);
		}
	}
}
