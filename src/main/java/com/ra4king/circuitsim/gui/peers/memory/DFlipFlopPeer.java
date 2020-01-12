package com.ra4king.circuitsim.gui.peers.memory;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.WireValue.State;
import com.ra4king.circuitsim.simulator.components.memory.DFlipFlop;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class DFlipFlopPeer extends ComponentPeer<DFlipFlop> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Memory", "D Flip-Flop"),
		                     new Image(DFlipFlopPeer.class.getResourceAsStream("/DFlipFlop.png")),
		                     new Properties());
	}
	
	private final PortConnection clockConnection;
	
	public DFlipFlopPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.mergeIfExists(props);
		
		DFlipFlop flipFlop = new DFlipFlop(properties.getValue(Properties.LABEL));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(
			clockConnection = new PortConnection(this, flipFlop.getPort(DFlipFlop.PORT_CLOCK), "Clock", 0, 1));
		connections.add(new PortConnection(this, flipFlop.getPort(DFlipFlop.PORT_D), "Data input", 0, 3));
		connections.add(new PortConnection(this, flipFlop.getPort(DFlipFlop.PORT_PRESET), "Preset", 1, 4));
		connections.add(new PortConnection(this, flipFlop.getPort(DFlipFlop.PORT_ENABLE), "Enable", 2, 4));
		connections.add(new PortConnection(this, flipFlop.getPort(DFlipFlop.PORT_CLEAR), "Clear", 3, 4));
		connections.add(new PortConnection(this, flipFlop.getPort(DFlipFlop.PORT_Q), "Current state", 4, 1));
		connections.add(new PortConnection(this, flipFlop.getPort(DFlipFlop.PORT_QN), "NOT of current state", 4, 3));
		
		init(flipFlop, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState state) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();
		
		State bit = state.getLastPushed(getComponent().getPort(DFlipFlop.PORT_Q)).getBit(0);
		GuiUtils.setBitColor(graphics, bit);
		graphics.fillOval(x + width * 0.5 - 10, y + height * 0.5 - 10, 20, 20);
		
		graphics.setFill(Color.WHITE);
		graphics.setFont(GuiUtils.getFont(16));
		graphics.fillText(String.valueOf(bit.repr), x + width * 0.5 - 5, y + height * 0.5 + 6);
		
		graphics.setFill(Color.GRAY);
		graphics.setFont(GuiUtils.getFont(10));
		graphics.fillText("D", x + 3, y + height - 7);
		graphics.fillText("Q", x + width - 10, y + 13);
		graphics.fillText("1", x + 7, y + height - 4);
		graphics.fillText("en", x + width * 0.5 - 6, y + height - 4);
		graphics.fillText("0", x + width - 13, y + height - 4);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawClockInput(graphics, clockConnection, Direction.WEST);
	}
}
