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
import com.ra4king.circuitsim.gui.properties.IntegerString;
import com.ra4king.circuitsim.gui.properties.PropertyValidators;
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
	private static final Map<Circuit, Map<String, Set<Tunnel>>> tunnels = new HashMap<>();
	
	private static final Property<IntegerString> WIDTH =
		new Property<>("Width", "", "", PropertyValidators.INTEGER_VALIDATOR, true, false, new IntegerString(0));
	private static final Property<String> PREVIOUS_TEXT =
		new Property<>("Previous text", "", "", PropertyValidators.ANY_STRING_VALIDATOR, true, true, "");
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(
			new Pair<>("Wiring", "Tunnel"),
			new Image(Tunnel.class.getResourceAsStream("/images/Tunnel.png")),
			new Properties(new Property<>(Properties.DIRECTION, Direction.WEST)));
	}
	
	private final Component tunnel;
	private final String label;
	private final int bitSize;
	
	public Tunnel(Properties props, int x, int y) {
		super(x, y, 0, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(WIDTH);
		properties.mergeIfExists(props);
		
		label = properties.getValue(Properties.LABEL);
		bitSize = properties.getValue(Properties.BITSIZE);
		
		// Avoid recalculating the width if the previous text hasn't changed
		int width;
		if (props.containsProperty(WIDTH) &&
		    (!props.containsProperty(PREVIOUS_TEXT) || props.getValue(PREVIOUS_TEXT).equals(label))) {
			width = properties.getValue(WIDTH).getValue();
		} else {
			Bounds bounds = GuiUtils.getBounds(GuiUtils.getFont(13), label);
			width = Math.max((int)Math.ceil(bounds.getWidth() / GuiUtils.BLOCK_SIZE), 1);
			properties.setValue(WIDTH, new IntegerString(width));
		}
		setWidth(width);
		properties.setValue(PREVIOUS_TEXT, label);
		
		tunnel = new Component(label, new int[] { bitSize }) {
			@Override
			public void setCircuit(Circuit circuit) {
				Circuit oldCircuit = getCircuit();
				
				super.setCircuit(circuit);
				
				if (label.isEmpty()) {
					return;
				}
				
				if (circuit != null) {
					Map<String, Set<Tunnel>> tunnelSet = tunnels.computeIfAbsent(circuit, l -> new HashMap<>());
					Set<Tunnel> toNotify = tunnelSet.computeIfAbsent(label, c -> new HashSet<>());
					toNotify.add(Tunnel.this);
				} else {
					Map<String, Set<Tunnel>> tunnelSet = tunnels.get(oldCircuit);
					if (tunnelSet != null) {
						Set<Tunnel> toNotify = tunnelSet.get(label);
						if (toNotify != null) {
							toNotify.remove(Tunnel.this);
							
							if (toNotify.isEmpty()) {
								tunnelSet.remove(label);
								
								if (tunnelSet.isEmpty()) {
									tunnels.remove(oldCircuit);
								}
							}
						}
					}
				}
			}
			
			@Override
			public void init(CircuitState state, Object lastProperty) {
				if (label.isEmpty()) {
					return;
				}
				
				Map<String, Set<Tunnel>> tunnelSet = tunnels.get(getCircuit());
				if (tunnelSet != null) {
					Set<Tunnel> toNotify = tunnelSet.get(label);
					WireValue value = new WireValue(bitSize);
					
					for (Tunnel tunnel : toNotify) {
						if (tunnel != Tunnel.this) {
							Port port = tunnel.getComponent().getPort(0);
							WireValue portValue = state.getLastReceived(port);
							if (portValue.getBitSize() == value.getBitSize()) {
								try {
									value.merge(portValue);
								} catch (Exception exc) {
									return; // nothing to push, it's a short circuit
								}
							}
						}
					}
					
					state.pushValue(getPort(0), value);
				}
			}
			
			@Override
			public void uninit(CircuitState state) {
				Map<String, Set<Tunnel>> tunnelSet = tunnels.get(getCircuit());
				if (tunnelSet != null) {
					Set<Tunnel> toNotify = tunnelSet.get(label);
					if (toNotify != null) {
						tunnels:
						for (Tunnel tunnel : toNotify) {
							if (tunnel.bitSize == bitSize) {
								WireValue combined = new WireValue(bitSize);
								
								for (Tunnel otherTunnel : toNotify) {
									if (tunnel != otherTunnel && otherTunnel != Tunnel.this) {
										Port port = otherTunnel.getComponent().getPort(0);
										WireValue portValue = state.getLastReceived(port);
										if (portValue.getBitSize() == combined.getBitSize()) {
											try {
												combined.merge(portValue);
											} catch (Exception exc) {
												continue tunnels;
											}
										}
									}
								}
								
								state.pushValue(tunnel.getComponent().getPort(0), combined);
							}
						}
					}
				}
			}
			
			@Override
			public void valueChanged(CircuitState state, WireValue value, int portIndex) {
				Map<String, Set<Tunnel>> tunnelSet = tunnels.get(getCircuit());
				if (tunnelSet != null && tunnelSet.containsKey(label)) {
					Set<Tunnel> toNotify = tunnelSet.get(label);
					
					tunnels:
					for (Tunnel tunnel : toNotify) {
						if (tunnel != Tunnel.this && tunnel.bitSize == bitSize) {
							WireValue combined = value;
							
							if (toNotify.size() > 2) {
								combined = new WireValue(bitSize);
								
								for (Tunnel otherTunnel : toNotify) {
									if (tunnel != otherTunnel) {
										Port port = otherTunnel.getComponent().getPort(0);
										WireValue portValue = state.getLastReceived(port);
										if (portValue.getBitSize() == combined.getBitSize()) {
											try {
												combined.merge(portValue);
											} catch (Exception exc) {
												continue tunnels;
											}
										}
									}
								}
							}
							
							state.pushValue(tunnel.getComponent().getPort(0), combined);
						}
					}
				}
			}
		};
		
		List<PortConnection> connections = new ArrayList<>();
		switch (properties.getValue(Properties.DIRECTION)) {
			case EAST -> {
				setWidth(getWidth() + 2);
				connections.add(new PortConnection(this, tunnel.getPort(0), getWidth(), getHeight() / 2));
			}
			case WEST -> {
				setWidth(getWidth() + 2);
				connections.add(new PortConnection(this, tunnel.getPort(0), 0, getHeight() / 2));
			}
			case NORTH -> {
				setWidth(Math.max(((getWidth() - 1) / 2) * 2 + 2, 2));
				setHeight(3);
				connections.add(new PortConnection(this, tunnel.getPort(0), getWidth() / 2, 0));
			}
			case SOUTH -> {
				setWidth(Math.max(((getWidth() - 1) / 2) * 2 + 2, 2));
				setHeight(3);
				connections.add(new PortConnection(this, tunnel.getPort(0), getWidth() / 2, getHeight()));
			}
		}
		
		init(tunnel, properties, connections);
	}
	
	private boolean isIncompatible() {
		Map<String, Set<Tunnel>> tunnelSet = tunnels.get(tunnel.getCircuit());
		if (tunnelSet != null && tunnelSet.containsKey(label)) {
			for (Tunnel tunnel : tunnelSet.get(label)) {
				if (tunnel.bitSize != bitSize) {
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
		
		switch (direction) {
			case EAST -> {
				xOff = -block;
				graphics.beginPath();
				graphics.moveTo(x + width, y + height * 0.5);
				graphics.lineTo(x + width - block, y + height);
				graphics.lineTo(x, y + height);
				graphics.lineTo(x, y);
				graphics.lineTo(x + width - block, y);
				graphics.closePath();
			}
			case WEST -> {
				xOff = block;
				graphics.beginPath();
				graphics.moveTo(x, y + height * 0.5);
				graphics.lineTo(x + block, y);
				graphics.lineTo(x + width, y);
				graphics.lineTo(x + width, y + height);
				graphics.lineTo(x + block, y + height);
				graphics.closePath();
			}
			case NORTH -> {
				yOff = block;
				graphics.beginPath();
				graphics.moveTo(x + width * 0.5, y);
				graphics.lineTo(x + width, y + block);
				graphics.lineTo(x + width, y + height);
				graphics.lineTo(x, y + height);
				graphics.lineTo(x, y + block);
				graphics.closePath();
			}
			case SOUTH -> {
				yOff = -block;
				graphics.beginPath();
				graphics.moveTo(x + width * 0.5, y + height);
				graphics.lineTo(x, y + height - block);
				graphics.lineTo(x, y);
				graphics.lineTo(x + width, y);
				graphics.lineTo(x + width, y + height - block);
				graphics.closePath();
			}
		}
		
		graphics.fill();
		graphics.stroke();
		
		if (!label.isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), label);
			graphics.setFill(Color.BLACK);
			graphics.fillText(
				label,
				x + xOff + ((width - xOff) - bounds.getWidth()) * 0.5,
				y + yOff + ((height - yOff) + bounds.getHeight()) * 0.4);
		}
		
		if (isIncompatible) {
			PortConnection port = getConnections().get(0);
			
			graphics.setFill(Color.BLACK);
			graphics.fillText(String.valueOf(bitSize), port.getScreenX() + 11, port.getScreenY() + 21);
			
			graphics.setStroke(Color.ORANGE);
			graphics.setFill(Color.ORANGE);
			graphics.strokeOval(port.getScreenX() - 2, port.getScreenY() - 2, 10, 10);
			graphics.fillText(String.valueOf(bitSize), port.getScreenX() + 10, port.getScreenY() + 20);
		}
	}
}
