package com.ra4king.circuitsim.gui.peers.gates;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyValidators;
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
		this(props, x, y, 4, 4, true);
	}
	
	public GatePeer(Properties props, int x, int y, int width, int height, boolean allowNegatingInputs) {
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
				
				properties.setProperty(new Property<>(propName, PropertyValidators.YESNO_VALIDATOR, negate));
			} else {
				break;
			}
		}
		
		T gate = buildGate(properties);
		int gateNum = gate.getNumInputs();
		
		if (allowNegatingInputs) {
			for (int i = 0; i < Math.max(gateNum, negationCounts); i++) {
				String propName = "Negate " + i;
				
				if (i < gateNum) {
					boolean negate = false;
					
					if (!properties.containsProperty(propName)) {
						properties.setProperty(new Property<>(propName, PropertyValidators.YESNO_VALIDATOR, false));
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
		int minPortX = 0, minPortY = 0, maxPortX = 0, maxPortY = 0;
		Direction direction = getProperties().getValue(Properties.DIRECTION);

		for (int i = 0; i < getConnections().size() - 1; i++) {
			PortConnection portConnection = getConnections().get(i);
			int x = portConnection.getX() * GuiUtils.BLOCK_SIZE;
			int y = portConnection.getY() * GuiUtils.BLOCK_SIZE;

			if (i == 0) {
				minPortX = maxPortX = x;
				minPortY = maxPortY = y;
			} else {
				minPortX = Math.min(minPortX, x);
				minPortY = Math.min(minPortY, y);
				maxPortX = Math.max(maxPortX, x);
				maxPortY = Math.max(maxPortY, y);
			}

			if (getComponent().getNegateInputs()[i]) {
				graphics.setFill(Color.WHITE);
				graphics.setStroke(Color.BLACK);
				graphics.setLineWidth(1.0);

				switch (direction) {
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

				int dx = switch (direction) {
					// The -1 here is to account for the width of these fake wires themselves.
					// Without this, the wires will pass the line that reaches out to ports beyond
					// the width/height of the gate (drawn below)
					case WEST -> -(GuiUtils.BLOCK_SIZE - 1);
					case EAST -> GuiUtils.BLOCK_SIZE - 1;
					default -> 0;
				};
				int dy = switch (direction) {
					case NORTH -> -(GuiUtils.BLOCK_SIZE - 1);
					case SOUTH -> GuiUtils.BLOCK_SIZE - 1;
					default -> 0;
				};

				graphics.strokeLine(x, y, x + dx, y + dy);
			}
		}

		// Reset the line width to the default after possibly drawing some fake wires drawn above
		graphics.setLineWidth(1.0);
		graphics.setStroke(Color.BLACK);

		// Draw a thin black line to reach ports that are beyond the width/height of the gate.
		// This prevents fake wires or ports from hanging off the edge of the gate, which can
		// confuse students.
		int pad;
		if (hasNegatedInput) {
			// Need to account for the bubbles drawn above
			pad = switch (direction) {
				case NORTH, WEST -> -GuiUtils.BLOCK_SIZE;
				case EAST, SOUTH -> GuiUtils.BLOCK_SIZE;
			};
		} else {
			pad = 0;
		}

		pad += switch (direction) {
			case WEST -> getScreenWidth();
			case NORTH -> getScreenHeight();
			default -> 0;
		};

		switch (direction) {
			case WEST, EAST -> {
				if (minPortY < getScreenY()) {
					// The -1s and +1s here are to account for the width of fake wires drawn above
					// (otherwise they overhang this little black line)
					graphics.strokeLine(getScreenX() + pad, minPortY - 1, getScreenX() + pad, getScreenY());
				}
				if (maxPortY > getScreenY() + getScreenHeight()) {
					graphics.strokeLine(getScreenX() + pad, maxPortY + 1, getScreenX() + pad, getScreenY() + getScreenHeight());
				}
			}
			case NORTH, SOUTH -> {
				if (minPortX < getScreenX()) {
					graphics.strokeLine(minPortX - 1, getScreenY() + pad, getScreenX(), getScreenY() + pad);
				}
				if (maxPortX > getScreenX() + getScreenWidth()) {
					graphics.strokeLine(maxPortX + 1, getScreenY() + pad, getScreenX() + getScreenWidth(), getScreenY() + pad);
				}
			}
		}
		
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		GuiUtils.rotateGraphics(this, graphics, direction);
		
		if (hasNegatedInput) {
			graphics.translate(GuiUtils.BLOCK_SIZE, 0);
		}
		
		paintGate(graphics, circuitState);
	}
	
	public abstract void paintGate(GraphicsContext graphics, CircuitState circuitState);
}
