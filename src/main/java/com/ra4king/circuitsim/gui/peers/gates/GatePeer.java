package com.ra4king.circuitsim.gui.peers.gates;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.gates.Gate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Roi Atalla
 */
public abstract class GatePeer<T extends Gate> extends ComponentPeer<T> {
	private boolean hasNegatedInput = false;
	
	public GatePeer(Properties props, int x, int y) {
		this(props, x, y, 4, 4);
	}
	
	public GatePeer(Properties props, int x, int y, int width, int height) {
		super(x, y, width, height);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		ensureProperties(properties);
		properties.mergeIfExists(props);
		
		int negationCounts = 0;
		while (true) {
			String propName = "Negate " + negationCounts++;
			
			if (props.containsProperty(propName)) {
				boolean negate;
				Object value = props.getValue(propName);
				if (value instanceof Boolean) {
					negate = (Boolean)value;
				} else {
					negate = value.equals("Yes");
				}
				
				properties.setProperty(new Property<>(propName, Properties.YESNO_VALIDATOR, negate));
			} else {
				break;
			}
		}
		
		T gate = buildGate(properties);
		int gateNum = gate.getNumInputs();
		
		for (int i = 0; i < Math.max(gateNum, negationCounts); i++) {
			String propName = "Negate " + i;
			
			if (i < gateNum) {
				boolean negate = false;
				
				if (!properties.containsProperty(propName)) {
					properties.setProperty(new Property<>(propName, Properties.YESNO_VALIDATOR, false));
				} else {
					negate = properties.<Boolean>getProperty(propName).value;
				}
				
				if (i == 0) {
					properties.getProperty(propName).display += " Top/Left";
				} else if (i == gateNum - 1) {
					properties.getProperty(propName).display += " Bottom/Right";
				}
				
				if (negate && !hasNegatedInput) {
					hasNegatedInput = true;
					setWidth(width + 1);
				}
			} else {
				properties.clearProperty(propName);
			}
		}
		
		GuiUtils.rotateElementSize(this, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		List<PortConnection> connections = new ArrayList<>();
		
		int inputOffset = 0;
		switch (properties.getValue(Properties.DIRECTION)) {
			case WEST:
				inputOffset = getWidth();
			case EAST:
				for (int i = 0; i < gateNum; i++) {
					int add = (gateNum % 2 == 0 && i >= gateNum / 2) ? 3 : 2;
					connections.add(new PortConnection(this,
					                                   gate.getPort(i),
					                                   inputOffset,
					                                   i + add - gateNum / 2 - (gateNum == 1 ? 1 : 0)));
				}
				
				connections.add(new PortConnection(this,
				                                   gate.getPort(gateNum),
				                                   getWidth() - inputOffset,
				                                   getHeight() / 2));
				break;
			case NORTH:
				inputOffset = getHeight();
			case SOUTH:
				for (int i = 0; i < gateNum; i++) {
					int add = (gateNum % 2 == 0 && i >= gateNum / 2) ? 3 : 2;
					connections.add(new PortConnection(this,
					                                   gate.getPort(i),
					                                   i + add - gateNum / 2 - (gateNum == 1 ? 1 : 0),
					                                   inputOffset));
				}
				
				connections.add(new PortConnection(this,
				                                   gate.getPort(gateNum),
				                                   getWidth() / 2,
				                                   getHeight() - inputOffset));
				break;
		}
		
		init(gate, properties, connections);
	}
	
	protected static boolean[] parseNegatedInputs(int inputs, Properties properties) {
		boolean[] negated = new boolean[inputs];
		
		for (int i = 0; i < negated.length; i++) {
			negated[i] = properties.getValueOrDefault("Negate " + i, false);
		}
		
		return negated;
	}
	
	protected abstract void ensureProperties(Properties properties);
	
	public abstract T buildGate(Properties properties);
	
	@Override
	public final void paint(GraphicsContext graphics, CircuitState circuitState) {
		for (int i = 0; i < getConnections().size() - 1; i++) {
			PortConnection portConnection = getConnections().get(i);
			int x = portConnection.getX() * GuiUtils.BLOCK_SIZE;
			int y = portConnection.getY() * GuiUtils.BLOCK_SIZE;

			if (getComponent().getNegateInputs()[i]) {
				graphics.setFill(Color.WHITE);
				graphics.setStroke(Color.BLACK);
				graphics.setLineWidth(1.0);

				switch (getProperties().getValue(Properties.DIRECTION)) {
					case WEST:
						x -= GuiUtils.BLOCK_SIZE;
					case EAST:
						y -= GuiUtils.BLOCK_SIZE * 0.5;
						break;
					case NORTH:
						y -= GuiUtils.BLOCK_SIZE;
					case SOUTH:
						x -= GuiUtils.BLOCK_SIZE * 0.5;
						break;
				}
				
				graphics.fillOval(x, y, GuiUtils.BLOCK_SIZE, GuiUtils.BLOCK_SIZE);
				graphics.strokeOval(x, y, GuiUtils.BLOCK_SIZE, GuiUtils.BLOCK_SIZE);
			} else if (hasNegatedInput) {
				// Imitate how a wire is drawn in Wire.paint()
				GuiUtils.setBitColor(graphics, circuitState.getLastReceived(portConnection.getPort()));
				graphics.setLineWidth(2.0);

				int dx = switch (getProperties().getValue(Properties.DIRECTION)) {
					case WEST -> -GuiUtils.BLOCK_SIZE;
					case EAST -> GuiUtils.BLOCK_SIZE;
					default -> 0;
				};
				int dy = switch (getProperties().getValue(Properties.DIRECTION)) {
					case NORTH -> -GuiUtils.BLOCK_SIZE;
					case SOUTH -> GuiUtils.BLOCK_SIZE;
					default -> 0;
				};

				graphics.strokeLine(x, y, x + dx, y + dy);
			}
		}

		// Reset the line width to the default after possibly drawing some fake wires above
		graphics.setLineWidth(1.0);
		
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		GuiUtils.rotateGraphics(this, graphics, getProperties().getValue(Properties.DIRECTION));
		
		if (hasNegatedInput) {
			graphics.translate(GuiUtils.BLOCK_SIZE, 0);
		}
		
		paintGate(graphics, circuitState);
	}
	
	public abstract void paintGate(GraphicsContext graphics, CircuitState circuitState);
}
