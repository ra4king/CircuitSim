package com.ra4king.circuitsim.gui.peers.gates;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.CircuitSimVersion;
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
	private static final CircuitSimVersion MAXIMUM_VERSION_FOR_LEGACY_GATE_INPUT_PLACEMENT =
		new CircuitSimVersion("1.8.5");
	private static final Property<Boolean> LEGACY_GATE_INPUT_PLACEMENT = new Property<>(
		"Legacy Gate Input Placement",
		"Legacy Gate Input Placement",
		"If enabled, no offset is " + "used for gates with more " + "than 5 inputs.",
		PropertyValidators.YESNO_VALIDATOR,
		false);
	
	private boolean hasExpandedInputs = false;
	
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
		if (allowNegatingInputs) {
			while (true) {
				String propName = "Negate " + negationCounts++;
				Property<Boolean> property = new Property<>(propName, PropertyValidators.YESNO_VALIDATOR, false);
				if (props.containsProperty(propName)) {
					props.ensureProperty(property);
					properties.setProperty(new Property<>(property, props.getValue(property)));
				} else {
					break;
				}
			}
		}
		
		T gate = buildGate(properties);
		int gateNum = gate.getNumInputs();
		
		boolean hasNegatedInput = false;
		if (allowNegatingInputs) {
			for (int i = 0; i < Math.max(gateNum, negationCounts); i++) {
				String propName = "Negate " + i;
				
				if (i < gateNum) {
					Property<Boolean> property = new Property<>(propName, PropertyValidators.YESNO_VALIDATOR, false);
					if (i == 0) {
						property.display += " Top/Left";
					} else if (i == gateNum - 1) {
						property.display += " Bottom/Right";
					}
					
					boolean negate = props.getValueOrDefault(property, false);
					properties.setProperty(new Property<>(property, negate));
					
					hasNegatedInput |= negate;
				} else {
					properties.clearProperty(propName);
				}
			}
		}
		
		props.ensurePropertyIfExists(LEGACY_GATE_INPUT_PLACEMENT);
		
		boolean forceLegacyInputPlacement =
			props.getVersion().compareTo(MAXIMUM_VERSION_FOR_LEGACY_GATE_INPUT_PLACEMENT) <= 0 ||
			props.getValueOrDefault(LEGACY_GATE_INPUT_PLACEMENT, false);
		
		// Expand the width (in the default configuration) by 1 if any inputs are negated or if there are more than
		// five inputs. This will show the additional line in for each gate to avoid confusing "floating" ports.
		if (hasNegatedInput || (gateNum > 5 && !forceLegacyInputPlacement)) {
			hasExpandedInputs = true;
			setWidth(width + 1);
		} else if (gateNum > 5 && forceLegacyInputPlacement) {
			// Legacy behavior where the file was saved with an older version
			properties.setValue(LEGACY_GATE_INPUT_PLACEMENT, true);
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
			} else if (hasExpandedInputs) {
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
		if (hasExpandedInputs) {
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
					graphics.strokeLine(getScreenX() + pad,
					                    maxPortY + 1,
					                    getScreenX() + pad,
					                    getScreenY() + getScreenHeight());
				}
			}
			case NORTH, SOUTH -> {
				if (minPortX < getScreenX()) {
					graphics.strokeLine(minPortX - 1, getScreenY() + pad, getScreenX(), getScreenY() + pad);
				}
				if (maxPortX > getScreenX() + getScreenWidth()) {
					graphics.strokeLine(maxPortX + 1,
					                    getScreenY() + pad,
					                    getScreenX() + getScreenWidth(),
					                    getScreenY() + pad);
				}
			}
		}
		
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		GuiUtils.rotateGraphics(this, graphics, direction);
		
		if (hasExpandedInputs) {
			graphics.translate(GuiUtils.BLOCK_SIZE, 0);
		}
		
		paintGate(graphics, circuitState);
	}
	
	public abstract void paintGate(GraphicsContext graphics, CircuitState circuitState);
}
