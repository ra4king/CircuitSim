package com.ra4king.circuitsim.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Direction;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.arithmetic.Negator;
import com.ra4king.circuitsim.simulator.components.arithmetic.RandomGenerator;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class RandomGeneratorPeer extends ComponentPeer<RandomGenerator> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Arithmetic", "Random Generator"),
		                     new Image(RandomGeneratorPeer.class.getResourceAsStream("/images/RandomGenerator.png")),
		                     new Properties());
	}
	
	private final PortConnection clockConnection;
	
	public RandomGeneratorPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.mergeIfExists(props);
		
		RandomGenerator
			randomGenerator =
			new RandomGenerator(properties.getValue(Properties.LABEL), properties.getValue(Properties.BITSIZE));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(
			clockConnection =
				new PortConnection(this, randomGenerator.getPort(RandomGenerator.PORT_CLK), "Clock", 2, 4));
		connections.add(new PortConnection(this, randomGenerator.getPort(Negator.PORT_OUT), "Out", 4, 2));
		
		init(randomGenerator, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setFill(Color.WHITE);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(GuiUtils.getFont(16, true));
		Bounds bounds = GuiUtils.getBounds(graphics.getFont(), "RNG");
		graphics.setFill(Color.BLACK);
		graphics.fillText("RNG",
		                  getScreenX() + (getScreenWidth() - bounds.getWidth()) * 0.5,
		                  getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.45);
		
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawClockInput(graphics, clockConnection, Direction.SOUTH);
	}
}
