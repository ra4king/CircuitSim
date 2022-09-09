package com.ra4king.circuitsim.gui.peers.wiring;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.WireValue.State;
import com.ra4king.circuitsim.simulator.components.wiring.Power;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Pair;

/**
 * @author Austin Adams and Roi Atalla
 */
public class PowerPeer extends ComponentPeer<Power> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Power"),
		                     new Image(PowerPeer.class.getResourceAsStream("/images/Power.png")),
		                     new Properties());
	}

	public PowerPeer(Properties props, int x, int y) {
		super(x, y, 2, 3);

		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.mergeIfExists(props);

		Power power = new Power(properties.getValue(Properties.LABEL));

		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, power.getPort(0), getWidth() / 2, getHeight()));

		init(power, properties, connections);
	}

	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));

		int x = getScreenX();
		int y = getScreenY();
		int width = getScreenWidth();
		int height = getScreenHeight();

		GuiUtils.setBitColor(graphics, State.ONE);

		graphics.setLineWidth(2);

		graphics.beginPath();
		graphics.moveTo(x, y);
		graphics.lineTo(x + width, y);
		graphics.moveTo(x + 0.5 * width, y);
		graphics.lineTo(x + 0.5 * width, y + height);
		graphics.stroke();
	}
}
