package com.ra4king.circuitsim.gui.peers.plexers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.plexers.Demultiplexer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class DemultiplexerPeer extends ComponentPeer<Demultiplexer> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Plexer", "Demux"),
		                     new Image(DemultiplexerPeer.class.getResourceAsStream("/images/Demux.png")),
		                     new Properties());
	}
	
	public DemultiplexerPeer(Properties props, int x, int y) {
		super(x, y, 3, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.SELECTOR_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(Properties.SELECTOR_BITS);
		properties.mergeIfExists(props);
		
		Demultiplexer demux = new Demultiplexer(properties.getValue(Properties.LABEL),
		                                        properties.getValue(Properties.BITSIZE),
		                                        properties.getValue(Properties.SELECTOR_BITS));
		setHeight(demux.getNumOutputs() + 2);
		
		GuiUtils.rotateElementSize(this, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		boolean location = properties.getValue(Properties.SELECTOR_LOCATION);
		
		int outOffset = 0, selOffset = 0;
		List<PortConnection> connections = new ArrayList<>();
		switch (properties.getValue(Properties.DIRECTION)) {
			case EAST:
				outOffset = getWidth();
				selOffset = 1;
			case WEST:
				for (int i = 0; i < demux.getNumOutputs(); i++) {
					connections.add(new PortConnection(this, demux.getPort(i), String.valueOf(i), outOffset, i + 1));
				}
				connections.add(new PortConnection(this,
				                                   demux.getSelectorPort(),
				                                   "Selector",
				                                   getWidth() / 2 + selOffset,
				                                   location ? 0 : getHeight()));
				connections.add(new PortConnection(this,
				                                   demux.getInputPort(),
				                                   "In",
				                                   getWidth() - outOffset,
				                                   getHeight() / 2));
				break;
			case SOUTH:
				outOffset = getHeight();
				selOffset = 1;
			case NORTH:
				for (int i = 0; i < demux.getNumOutputs(); i++) {
					connections.add(new PortConnection(this, demux.getPort(i), String.valueOf(i), i + 1, outOffset));
				}
				connections.add(new PortConnection(this,
				                                   demux.getSelectorPort(),
				                                   "Selector",
				                                   location ? 0 : getWidth(),
				                                   getHeight() / 2 + selOffset));
				connections.add(new PortConnection(this,
				                                   demux.getInputPort(),
				                                   "In",
				                                   getWidth() / 2,
				                                   getHeight() - outOffset));
				break;
		}
		
		init(demux, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		Direction direction = getProperties().getValue(Properties.DIRECTION);
		
		int x = getScreenX();
		int y = getScreenY();
		int width = 3 * GuiUtils.BLOCK_SIZE;
		int height = (getComponent().getNumOutputs() + 2) * GuiUtils.BLOCK_SIZE;
		
		int zeroXOffset = 0;
		
		switch (direction) {
			case SOUTH:
				graphics.translate(x, y);
				graphics.rotate(270);
				graphics.translate(-x - width, -y);
			case WEST:
				zeroXOffset = 2;
				graphics.beginPath();
				graphics.moveTo(x, y);
				graphics.lineTo(x + width, y + Math.min(20, height * 0.2));
				graphics.lineTo(x + width, y + height - Math.min(20, height * 0.2));
				graphics.lineTo(x, y + height);
				graphics.closePath();
				break;
			case NORTH:
				graphics.translate(x, y);
				graphics.rotate(270);
				graphics.translate(-x - width, -y);
			case EAST:
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
		
		graphics.setFill(Color.DARKGRAY);
		graphics.fillText("0", x + zeroXOffset, y + 13);
	}
}
