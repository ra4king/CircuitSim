package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.CircuitManager;
import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Port;
import com.ra4king.circuitsim.simulator.components.wiring.Clock;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ClockPeer extends ComponentPeer<Clock> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Clock"),
		                     new Image(ClockPeer.class.getResourceAsStream("/images/Clock.png")),
		                     new Properties());
	}
	
	public ClockPeer(Properties props, int x, int y) {
		super(x, y, 2, 2);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(Properties.DIRECTION);
		properties.mergeIfExists(props);
		
		Clock clock = new Clock(properties.getValue(Properties.LABEL));
		
		List<PortConnection> connections = new ArrayList<>();
		switch (properties.getValue(Properties.DIRECTION)) {
			case EAST -> connections.add(new PortConnection(this,
			                                                clock.getPort(Clock.PORT),
			                                                getWidth(),
			                                                getHeight() / 2));
			case WEST -> connections.add(new PortConnection(this, clock.getPort(Clock.PORT), 0, getHeight() / 2));
			case NORTH -> connections.add(new PortConnection(this, clock.getPort(Clock.PORT), getWidth() / 2, 0));
			case SOUTH -> connections.add(new PortConnection(this,
			                                                 clock.getPort(Clock.PORT),
			                                                 getWidth() / 2,
			                                                 getHeight()));
		}
		
		init(clock, properties, connections);
	}
	
	@Override
	public void mousePressed(CircuitManager manager, CircuitState state, double x, double y) {
		Clock.tick(getComponent().getCircuit().getSimulator());
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		Port port = getComponent().getPort(Clock.PORT);
		if (circuitState.isShortCircuited(port.getLink())) {
			graphics.setFill(Color.RED);
		} else {
			GuiUtils.setBitColor(graphics, circuitState.getLastPushed(port).getBit(0));
		}
		GuiUtils.drawShape(graphics::fillRect, this);
		graphics.setStroke(Color.BLACK);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setStroke(Color.WHITE);
		graphics.setLineWidth(1.5);
		double offset1 = Clock.getTickState(getComponent().getCircuit().getSimulator()) ? 0.3 : 0;
		double offset2 = Clock.getTickState(getComponent().getCircuit().getSimulator()) ? 0.6 : 0;
		
		// lower line
		graphics.strokeLine(getScreenX() + getScreenWidth() * (0.2 + offset1),
		                    getScreenY() + getScreenHeight() * 0.7,
		                    getScreenX() + getScreenWidth() * (0.5 + offset1),
		                    getScreenY() + getScreenHeight() * 0.7);
		
		// upper line
		graphics.strokeLine(getScreenX() + getScreenWidth() * (0.5 - offset1),
		                    getScreenY() + getScreenHeight() * 0.3,
		                    getScreenX() + getScreenWidth() * (0.8 - offset1),
		                    getScreenY() + getScreenHeight() * 0.3);
		
		// lower vertical line
		graphics.strokeLine(getScreenX() + getScreenWidth() * (0.2 + offset2),
		                    getScreenY() + getScreenHeight() * 0.5,
		                    getScreenX() + getScreenWidth() * (0.2 + offset2),
		                    getScreenY() + getScreenHeight() * 0.7);
		
		// upper vetical line line
		graphics.strokeLine(getScreenX() + getScreenWidth() * (0.8 - offset2),
		                    getScreenY() + getScreenHeight() * 0.3,
		                    getScreenX() + getScreenWidth() * (0.8 - offset2),
		                    getScreenY() + getScreenHeight() * 0.5);
		
		// middle vertical line
		graphics.strokeLine(getScreenX() + getScreenWidth() * 0.5,
		                    getScreenY() + getScreenHeight() * 0.3,
		                    getScreenX() + getScreenWidth() * 0.5,
		                    getScreenY() + getScreenHeight() * 0.7);
	}
}
