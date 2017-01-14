package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.WireValue;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class Tunnel extends ComponentPeer<Component> {
	private static Map<String, List<Tunnel>> tunnels = new HashMap<>();
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Tunnel"),
		                     new Image(Tunnel.class.getResourceAsStream("/resources/Tunnel.png")),
		                     new Properties());
	}
	
	public Tunnel(Properties props, int x, int y) {
		super(x, y, 0, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		String label = properties.getValue(Properties.LABEL);
		int bitsize = properties.getIntValue(Properties.BITSIZE);
		
		Bounds bounds = GuiUtils.getBounds(new Font("monospace", 15), label);
		setWidth(1 + Math.max((int)Math.ceil(bounds.getWidth() / GuiUtils.BLOCK_SIZE), 1));
		
		Component tunnel = new Component(label, new int[] { bitsize }) {
			@Override
			public void setCircuit(Circuit circuit) {
				super.setCircuit(circuit);
				
				if(circuit == null) {
					List<Tunnel> tunnelList = tunnels.get(label);
					if(tunnelList != null) {
						tunnelList.remove(Tunnel.this);
						if(tunnelList.isEmpty()) {
							tunnels.remove(label);
						}
					}
				} else if(!label.isEmpty()) {
					System.out.println("Tunnel registered");
					List<Tunnel> tunnelList = tunnels.getOrDefault(label, new ArrayList<>());
					tunnelList.add(Tunnel.this);
					tunnels.put(label, tunnelList);
				}
			}
			
			@Override
			public void init(CircuitState state) {
				List<Tunnel> toNotify = tunnels.get(label);
				if(toNotify != null) {
					WireValue value = new WireValue(getComponent().getPort(0).getLink().getBitSize());
					
					for(Tunnel tunnel : toNotify) {
						if(tunnel != Tunnel.this
								   && tunnel.getComponent().getCircuit() == getComponent().getCircuit()) {
							Port port = tunnel.getComponent().getPort(0);
							try {
								value.merge(state.getMergedValue(port.getLink()));
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
				List<Tunnel> toNotify = tunnels.get(label);
				if(toNotify != null) {
					for(Tunnel tunnel : toNotify) {
						if(tunnel != Tunnel.this
								   && tunnel.getComponent().getCircuit() == getComponent().getCircuit()) {
							state.pushValue(tunnel.getComponent().getPort(0), value);
						}
					}
				}
			}
		};
		
		List<Connection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, tunnel.getPort(0), 0, getHeight() / 2));
		
		init(tunnel, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setStroke(Color.BLACK);
		graphics.setFill(Color.WHITE);
		
		int block = GuiUtils.BLOCK_SIZE;
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		graphics.beginPath();
		graphics.moveTo(x, y + height * 0.5);
		graphics.lineTo(x + block, y);
		graphics.lineTo(x + width, y);
		graphics.lineTo(x + width, y + height);
		graphics.lineTo(x + block, y + height);
		graphics.closePath();
		
		graphics.fill();
		graphics.stroke();
		
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(getComponent().getName(),
			                    x + block + (width - bounds.getWidth()) * 0.5,
			                    y + (height + bounds.getHeight()) * 0.4);
		}
	}
}
