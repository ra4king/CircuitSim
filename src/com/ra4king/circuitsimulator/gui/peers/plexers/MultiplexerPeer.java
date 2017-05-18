package com.ra4king.circuitsimulator.gui.peers.plexers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.plexers.Multiplexer;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class MultiplexerPeer extends ComponentPeer<Multiplexer> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Plexer", "Mux"),
		                     new Image(MultiplexerPeer.class.getResourceAsStream("/resources/Mux.png")),
		                     new Properties());
	}
	
	public MultiplexerPeer(Properties props, int x, int y) {
		super(x, y, 3, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.SELECTOR_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.SELECTOR_BITS);
		properties.mergeIfExists(props);
		
		Multiplexer mux = new Multiplexer(properties.getValue(Properties.LABEL),
		                                  properties.getValue(Properties.BITSIZE),
		                                  properties.getValue(Properties.SELECTOR_BITS));
		setHeight(mux.getNumInputs() + 2);
		
		GuiUtils.rotateElement(this, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		boolean location = properties.getValue(Properties.SELECTOR_LOCATION);
		
		int outOffset = 0, selOffset = 0;
		List<PortConnection> connections = new ArrayList<>();
		switch(properties.getValue(Properties.DIRECTION)) {
			case WEST:
				outOffset = getWidth();
				selOffset = 1;
			case EAST:
				for(int i = 0; i < mux.getNumInputs(); i++) {
					connections.add(new PortConnection(this, mux.getPort(i), outOffset, i + 1));
				}
				connections.add(
						new PortConnection(this, mux.getSelectorPort(), "Selector", getWidth() / 2 + selOffset,
						                   location ? 0 : getHeight()));
				connections.add(
						new PortConnection(this, mux.getOutPort(), "Out", getWidth() - outOffset, getHeight() / 2));
				break;
			case NORTH:
				outOffset = getHeight();
				selOffset = 1;
			case SOUTH:
				for(int i = 0; i < mux.getNumInputs(); i++) {
					connections.add(new PortConnection(this, mux.getPort(i), i + 1, outOffset));
				}
				connections.add(
						new PortConnection(this, mux.getSelectorPort(), "Selector", location ? 0 : getWidth(),
						                   getHeight() / 2 + selOffset));
				connections.add(
						new PortConnection(this, mux.getOutPort(), "Out", getWidth() / 2, getHeight() - outOffset));
				break;
		}
		
		init(mux, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(getComponent().getName(),
			                    getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
			                    getScreenY() - 5);
		}
		
		Direction direction = getProperties().getValue(Properties.DIRECTION);
		
		int x = getScreenX();
		int y = getScreenY();
		int width = 3 * GuiUtils.BLOCK_SIZE;
		int height = (getComponent().getNumInputs() + 2) * GuiUtils.BLOCK_SIZE;
		
		int zeroXOffset = 0;
		
		switch(direction) {
			case NORTH:
				graphics.translate(x, y);
				graphics.rotate(270);
				graphics.translate(-x - width, -y);
			case EAST:
				zeroXOffset = 2;
				graphics.beginPath();
				graphics.moveTo(x, y);
				graphics.lineTo(x + width, y + Math.min(20, height * 0.2));
				graphics.lineTo(x + width, y + height - Math.min(20, height * 0.2));
				graphics.lineTo(x, y + height);
				graphics.closePath();
				break;
			case SOUTH:
				graphics.translate(x, y);
				graphics.rotate(270);
				graphics.translate(-x - width, -y);
			case WEST:
				zeroXOffset = width - 10;
				graphics.beginPath();
				graphics.moveTo(x + width, y);
				graphics.lineTo(x, y + Math.min(20, height * 0.2));
				graphics.lineTo(x, y + height - Math.min(20, height * 0.2));
				graphics.lineTo(x + width, y + height);
				graphics.closePath();
				break;
		}
		
		graphics.setFill(Color.WHITE);
		graphics.fill();
		graphics.setStroke(Color.BLACK);
		graphics.stroke();
		
		graphics.setStroke(Color.DARKGRAY);
		graphics.strokeText("0", x + zeroXOffset, y + 13);
	}
}
