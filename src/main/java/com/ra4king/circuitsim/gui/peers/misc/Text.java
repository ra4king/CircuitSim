package com.ra4king.circuitsim.gui.peers.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ra4king.circuitsim.gui.CircuitManager;
import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class Text extends ComponentPeer<Component> {
	private static Image textImage;
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Misc", "Text"),
		                     textImage = new Image(Text.class.getResourceAsStream("/Text.png")),
		                     new Properties());
	}
	
	private static final Property<String> TEXT = new Property<>("Text", Properties.ANY_STRING_VALIDATOR, "");
	
	private String text;
	private List<String> lines;
	
	public Text(Properties props, int x, int y) {
		super(x, y, 2, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(TEXT);
		properties.mergeIfExists(props);
		
		setText(properties.getValue(TEXT));
		
		Component component = new Component(text, new int[0]) {
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
		};
		
		init(component, properties, new ArrayList<>());
	}
	
	private void setText(String text) {
		this.text = text;
		this.lines = Arrays.asList(text.split("\n", -1));
		
		Bounds bounds = GuiUtils.getBounds(GuiUtils.getFont(13), text, false);
		setWidth(Math.max(2, (int)Math.ceil(bounds.getWidth() / GuiUtils.BLOCK_SIZE)));
		setHeight(Math.max(2, (int)Math.ceil(bounds.getHeight() / GuiUtils.BLOCK_SIZE)));
	}
	
	private Cursor prevCursor;
	private boolean entered;
	
	@Override
	public void mouseEntered(CircuitManager manager, CircuitState state) {
		Scene scene = manager.getSimulatorWindow().getScene();
		prevCursor = scene.getCursor();
		scene.setCursor(Cursor.TEXT);
		
		entered = true;
	}
	
	@Override
	public void mouseExited(CircuitManager manager, CircuitState state) {
		manager.getSimulatorWindow().getScene().setCursor(prevCursor);
		
		entered = false;
	}
	
	@Override
	public boolean keyPressed(CircuitManager manager, CircuitState state, KeyCode keyCode, String text) {
		return keyCode == KeyCode.BACK_SPACE && !this.text.isEmpty();
	}
	
	@Override
	public void keyTyped(CircuitManager manager, CircuitState state, String character) {
		char c = character.charAt(0);
		
		if(c == 8) { // backspace
			if(!text.isEmpty()) {
				String s = text.substring(0, text.length() - 1);
				setText(s);
				getProperties().setValue(TEXT, s);
			}
		} else if(c == 10 || c == 13 || c >= 32 && c <= 126) { // line feed, carriage return, or visible ASCII char 
			if(c == 13) c = 10; // convert \r to \n
			
			String s = text + c;
			setText(s);
			getProperties().setValue(TEXT, s);
		}
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState state) {
		graphics.setFill(Color.BLACK);
		graphics.setStroke(Color.BLACK);
		
		graphics.setLineWidth(2);
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		if(text.isEmpty()) {
			graphics.drawImage(textImage, x, y, width, height);
		} else {
			graphics.setFont(GuiUtils.getFont(13));
			for(int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				Bounds bounds = GuiUtils.getBounds(graphics.getFont(), line, false);
				
				graphics.fillText(line,
				                  x + (width - bounds.getWidth()) * 0.5,
				                  y + 15 * (i + 1));
				
				if(entered && i == lines.size() - 1) {
					double lx = x + (width + bounds.getWidth()) * 0.5 + 3;
					double ly = y + 15 * (i + 1);
					graphics.strokeLine(lx, ly - 10, lx, ly);
				}
			}
		}
	}
}
