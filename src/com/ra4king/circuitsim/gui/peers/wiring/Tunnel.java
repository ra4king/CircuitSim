package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.WireValue;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class Tunnel extends ComponentPeer<Component> {
	private static Map<Circuit, Map<String, Set<Tunnel>>> tunnels = new HashMap<>();
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Tunnel"),
		                     new Image(Tunnel.class.getResourceAsStream("/resources/Tunnel.png")),
		                     new Properties(new Property<>(Properties.DIRECTION, Direction.WEST)));
	}
	
	private final Component tunnel;
	
	public Tunnel(Properties props, int x, int y) {
		super(x, y, 0, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		String label = properties.getValue(Properties.LABEL);
		int bitSize = properties.getValue(Properties.BITSIZE);
		
		Bounds bounds = GuiUtils.getBounds(GuiUtils.getFont(13), label);
		setWidth(Math.max((int)Math.ceil(bounds.getWidth() / GuiUtils.BLOCK_SIZE), 1));
		
		tunnel = new Component(label, new int[] { bitSize }) {
			@Override
			public void setCircuit(Circuit circuit) {
				Circuit oldCircuit = getCircuit();
				
				super.setCircuit(circuit);
				
				if(circuit == null) {
					Map<String, Set<Tunnel>> tunnelSet = tunnels.get(oldCircuit);
					if(tunnelSet != null) {
						Set<Tunnel> tunnels = tunnelSet.get(label);
						if(tunnels == null || !tunnels.remove(Tunnel.this)) {
							throw new IllegalStateException("This is impossible: Tunnel should be in its circuit's list");
						}
					}
				} else if(!label.isEmpty()) {
					Map<String, Set<Tunnel>> tunnelSet = tunnels.computeIfAbsent(circuit, l -> new HashMap<>());
					tunnelSet.computeIfAbsent(label, c -> new HashSet<>()).add(Tunnel.this);
				}
			}
			
			@Override
			public void init(CircuitState state, Object lastProperty) {
				Map<String, Set<Tunnel>> tunnelSet = tunnels.get(getCircuit());
				Set<Tunnel> toNotify = tunnelSet.get(label);
				if(toNotify != null) {
					WireValue value = new WireValue(bitSize);
					
					for(Tunnel tunnel : toNotify) {
						if(tunnel != Tunnel.this
								   && tunnel.getComponent().getCircuit() == getComponent().getCircuit()) {
							Port port = tunnel.getComponent().getPort(0);
							try {
								WireValue portValue = state.getMergedValue(port.getLink());
								if(portValue.getBitSize() == value.getBitSize()) {
									value.merge(portValue);
								}
							} catch(Exception exc) {
								break;
							}
						}
					}
					
					state.pushValue(getPort(0), value);
				}
			}
			
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {
				Map<String, Set<Tunnel>> tunnelSet = tunnels.get(getCircuit());
				Set<Tunnel> toNotify = tunnelSet.get(label);
				if(toNotify != null) {
					for(Tunnel tunnel : toNotify) {
						if(tunnel != Tunnel.this) {
							state.pushValue(tunnel.getComponent().getPort(0), value);
						}
					}
				}
			}
		};
		
		List<PortConnection> connections = new ArrayList<>();
		switch(properties.getValue(Properties.DIRECTION)) {
			case EAST:
				setWidth(getWidth() + 2);
				connections.add(new PortConnection(this, tunnel.getPort(0), getWidth(), getHeight() / 2));
				break;
			case WEST:
				setWidth(getWidth() + 2);
				connections.add(new PortConnection(this, tunnel.getPort(0), 0, getHeight() / 2));
				break;
			case NORTH:
				setWidth(Math.max(((getWidth() - 1) / 2) * 2 + 2, 2));
				setHeight(3);
				connections.add(new PortConnection(this, tunnel.getPort(0), getWidth() / 2, 0));
				break;
			case SOUTH:
				setWidth(Math.max(((getWidth() - 1) / 2) * 2 + 2, 2));
				setHeight(3);
				connections.add(new PortConnection(this, tunnel.getPort(0), getWidth() / 2, getHeight()));
				break;
		}
		
		init(tunnel, properties, connections);
	}
	
	private boolean isIncompatible() {
		String label = getComponent().getName();
		int bitSize = getComponent().getPort(0).getLink().getBitSize();
		
		Map<String, Set<Tunnel>> tunnelSet = tunnels.get(tunnel.getCircuit());
		if(tunnelSet.containsKey(label)) {
			for(Tunnel tunnel : tunnelSet.get(label)) {
				if(tunnel.getComponent().getCircuit() == getComponent().getCircuit() &&
						   tunnel.getComponent().getPort(0).getLink().getBitSize() != bitSize) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		Direction direction = getProperties().getValue(Properties.DIRECTION);
		
		boolean isIncompatible = isIncompatible();
		
		graphics.setStroke(Color.BLACK);
		graphics.setFill(isIncompatible ? Color.ORANGE : Color.WHITE);
		
		int block = GuiUtils.BLOCK_SIZE;
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		int xOff = 0;
		int yOff = 0;
		
		switch(direction) {
			case EAST:
				xOff = -block;
				graphics.beginPath();
				graphics.moveTo(x + width, y + height * 0.5);
				graphics.lineTo(x + width - block, y + height);
				graphics.lineTo(x, y + height);
				graphics.lineTo(x, y);
				graphics.lineTo(x + width - block, y);
				graphics.closePath();
				break;
			case WEST:
				xOff = block;
				graphics.beginPath();
				graphics.moveTo(x, y + height * 0.5);
				graphics.lineTo(x + block, y);
				graphics.lineTo(x + width, y);
				graphics.lineTo(x + width, y + height);
				graphics.lineTo(x + block, y + height);
				graphics.closePath();
				break;
			case NORTH:
				yOff = block;
				graphics.beginPath();
				graphics.moveTo(x + width * 0.5, y);
				graphics.lineTo(x + width, y + block);
				graphics.lineTo(x + width, y + height);
				graphics.lineTo(x, y + height);
				graphics.lineTo(x, y + block);
				graphics.closePath();
				break;
			case SOUTH:
				yOff = -block;
				graphics.beginPath();
				graphics.moveTo(x + width * 0.5, y + height);
				graphics.lineTo(x, y + height - block);
				graphics.lineTo(x, y);
				graphics.lineTo(x + width, y);
				graphics.lineTo(x + width, y + height - block);
				graphics.closePath();
				break;
		}
		
		graphics.fill();
		graphics.stroke();
		
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setFill(Color.BLACK);
			graphics.fillText(getComponent().getName(),
			                  x + xOff + ((width - xOff) - bounds.getWidth()) * 0.5,
			                  y + yOff + ((height - yOff) + bounds.getHeight()) * 0.4);
		}
		
		if(isIncompatible) {
			PortConnection port = getConnections().get(0);
			
			graphics.setFill(Color.BLACK);
			graphics.fillText(String.valueOf(port.getPort().getLink().getBitSize()),
			                  port.getScreenX() + 11,
			                  port.getScreenY() + 21);
			
			graphics.setStroke(Color.ORANGE);
			graphics.setFill(Color.ORANGE);
			graphics.strokeOval(port.getScreenX() - 2, port.getScreenY() - 2, 10, 10);
			graphics.fillText(String.valueOf(port.getPort().getLink().getBitSize()),
			                  port.getScreenX() + 10,
			                  port.getScreenY() + 20);
		}
	}
}
