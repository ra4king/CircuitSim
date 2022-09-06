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
import com.ra4king.circuitsim.simulator.components.plexers.PriorityEncoder;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Elliott Childre
 */
public class PriorityEncoderPeer extends ComponentPeer<PriorityEncoder> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Plexer", "Priority Encoder"),
		                     new Image(PriorityEncoderPeer.class.getResourceAsStream("/images/PriorityEncoder.png")),
		                     new Properties());
	}
	
	private static final byte ENABLED_INOUT_SIDE_LEN = 4;
	
	public PriorityEncoderPeer(Properties props, int x, int y) {
		super(x, y, ENABLED_INOUT_SIDE_LEN, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.SELECTOR_BITS);
		properties.setValue(Properties.SELECTOR_BITS, 3);
		properties.mergeIfExists(props);
		
		PriorityEncoder
			pEncoder =
			new PriorityEncoder(properties.getValue(Properties.LABEL), properties.getValue(Properties.SELECTOR_BITS));
		int numInputs = 1 << pEncoder.getNumSelectBits();
		int inputSideLen = numInputs + 1;
		setHeight(inputSideLen);
		
		GuiUtils.rotateElementSize(this, Direction.EAST, properties.getValue(Properties.DIRECTION));
		
		List<PortConnection> connections = new ArrayList<>(numInputs + 4);
		int i;
		switch (properties.getValue(Properties.DIRECTION)) {
			case EAST -> {
				for (i = 0; i < numInputs; i++) {
					connections.add(new PortConnection(this, pEncoder.getPort(i), String.valueOf(i), 0, i + 1));
				}
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledInPort(),
				                                   "Enable In",
				                                   ENABLED_INOUT_SIDE_LEN >> 1,
				                                   inputSideLen));
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledOutPort(),
				                                   "Enable Out",
				                                   ENABLED_INOUT_SIDE_LEN >> 1,
				                                   0));
				connections.add(new PortConnection(this,
				                                   pEncoder.getGroupSignalPort(),
				                                   "Group Signal",
				                                   ENABLED_INOUT_SIDE_LEN,
				                                   (inputSideLen >> 1) + 1));
				connections.add(new PortConnection(this,
				                                   pEncoder.getOutputPort(),
				                                   "Output",
				                                   ENABLED_INOUT_SIDE_LEN,
				                                   inputSideLen >> 1));
			}
			case WEST -> {
				for (i = 0; i < numInputs; i++) {
					connections.add(new PortConnection(this,
					                                   pEncoder.getPort(i),
					                                   String.valueOf(i),
					                                   ENABLED_INOUT_SIDE_LEN,
					                                   i + 1));
				}
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledInPort(),
				                                   "Enable In",
				                                   ENABLED_INOUT_SIDE_LEN >> 1,
				                                   inputSideLen));
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledOutPort(),
				                                   "Enable Out",
				                                   ENABLED_INOUT_SIDE_LEN >> 1,
				                                   0));
				connections.add(new PortConnection(this,
				                                   pEncoder.getGroupSignalPort(),
				                                   "Group Signal",
				                                   0,
				                                   (inputSideLen >> 1) + 1));
				connections.add(new PortConnection(this, pEncoder.getOutputPort(), "Output", 0, inputSideLen >> 1));
			}
			case SOUTH -> {
				for (i = 0; i < numInputs; i++) {
					connections.add(new PortConnection(this, pEncoder.getPort(i), String.valueOf(i), i + 1, 0));
				}
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledInPort(),
				                                   "Enable In",
				                                   0,
				                                   ENABLED_INOUT_SIDE_LEN >> 1));
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledOutPort(),
				                                   "Enable Out",
				                                   inputSideLen,
				                                   ENABLED_INOUT_SIDE_LEN >> 1));
				connections.add(new PortConnection(this,
				                                   pEncoder.getGroupSignalPort(),
				                                   "Group Signal",
				                                   inputSideLen >> 1,
				                                   ENABLED_INOUT_SIDE_LEN));
				connections.add(new PortConnection(this,
				                                   pEncoder.getOutputPort(),
				                                   "Output",
				                                   (inputSideLen >> 1) + 1,
				                                   ENABLED_INOUT_SIDE_LEN));
			}
			case NORTH -> {
				for (i = 0; i < numInputs; i++) {
					connections.add(new PortConnection(this,
					                                   pEncoder.getPort(i),
					                                   String.valueOf(i),
					                                   i + 1,
					                                   ENABLED_INOUT_SIDE_LEN));
				}
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledInPort(),
				                                   "Enable In",
				                                   inputSideLen,
				                                   ENABLED_INOUT_SIDE_LEN >> 1));
				connections.add(new PortConnection(this,
				                                   pEncoder.getEnabledOutPort(),
				                                   "Enable Out",
				                                   0,
				                                   ENABLED_INOUT_SIDE_LEN >> 1));
				connections.add(new PortConnection(this,
				                                   pEncoder.getGroupSignalPort(),
				                                   "Group Signal",
				                                   (inputSideLen >> 1) + 1,
				                                   0));
				connections.add(new PortConnection(this, pEncoder.getOutputPort(), "Output", inputSideLen >> 1, 0));
			}
			default -> throw new RuntimeException("Unknown Direction");
		}
		init(pEncoder, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		Direction direction = getProperties().getValue(Properties.DIRECTION);
		graphics.translate(getScreenX(), getScreenY());
		
		int height;
		int width;
		
		int inputSideLength = ((1 << getComponent().getNumSelectBits()) + 1) * GuiUtils.BLOCK_SIZE;
		int enabledSideLength = ENABLED_INOUT_SIDE_LEN * GuiUtils.BLOCK_SIZE;
		
		double zeroX = GuiUtils.BLOCK_SIZE >> 1;
		double zeroY;
		
		if (direction == Direction.EAST || direction == Direction.WEST) {
			height = inputSideLength;
			width = enabledSideLength;
			zeroY = GuiUtils.BLOCK_SIZE * 1.5;
			if (direction == Direction.WEST) {
				zeroX = width - GuiUtils.BLOCK_SIZE;
			}
		} else {
			height = enabledSideLength;
			width = inputSideLength;
			zeroY = height - (GuiUtils.BLOCK_SIZE >> 1);
			if (direction == Direction.SOUTH) {
				zeroY = GuiUtils.BLOCK_SIZE * 1.5;
			}
		}
		
		graphics.setStroke(Color.BLACK);
		graphics.strokeRect(0, 0, width, height);
		
		graphics.setFill(Color.WHITE);
		graphics.fillRect(0, 0, width, height);
		
		graphics.setFill(Color.DARKGRAY);
		graphics.fillText("0", zeroX, zeroY);
		
		graphics.setFill(Color.BLACK);
		graphics.fillText("Pri",
		                  (width >> 1) - graphics.getFont().getSize(),
		                  (height >> 1) + 0.5 * GuiUtils.BLOCK_SIZE);
	}
}
