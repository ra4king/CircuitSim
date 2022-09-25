package com.ra4king.circuitsim.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsim.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.GuiUtils;
import com.ra4king.circuitsim.gui.Properties;
import com.ra4king.circuitsim.gui.Properties.Property;
import com.ra4king.circuitsim.gui.properties.PropertyListValidator;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.arithmetic.BitExtender;
import com.ra4king.circuitsim.simulator.components.arithmetic.BitExtender.ExtensionType;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class BitExtenderPeer extends ComponentPeer<BitExtender> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Arithmetic", "Bit Extender"),
		                     new Image(BitExtenderPeer.class.getResourceAsStream("/images/BitExtender.png")),
		                     new Properties());
	}
	
	public BitExtenderPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.LABEL_LOCATION);
		properties.ensureProperty(new Property<>("Input Bitsize", Properties.BITSIZE.validator, 1));
		properties.ensureProperty(new Property<>("Output Bitsize", Properties.BITSIZE.validator, 1));
		properties.ensureProperty(new Property<>("Extension Type",
		                                         new PropertyListValidator<>(ExtensionType.values()),
		                                         ExtensionType.ZERO));
		properties.mergeIfExists(props);
		
		BitExtender extender = new BitExtender(properties.getValue(Properties.LABEL),
		                                       properties.getValue("Input Bitsize"),
		                                       properties.getValue("Output Bitsize"),
		                                       properties.getValue("Extension Type"));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, extender.getPort(BitExtender.PORT_IN), "Input", 0, 2));
		connections.add(new PortConnection(this, extender.getPort(BitExtender.PORT_OUT), "Output", getWidth(), 2));
		
		init(extender, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		GuiUtils.drawName(graphics, this, getProperties().getValue(Properties.LABEL_LOCATION));
		
		graphics.setStroke(Color.BLACK);
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(GuiUtils.getFont(12, true));
		graphics.setFill(Color.BLACK);
		
		graphics.fillText(
			String.valueOf(getComponent().getInputBitSize()),
			getScreenX() + 3,
			getScreenY() + getScreenHeight() * 0.5 + 5);
		
		
		String outputString = String.valueOf(getComponent().getOutputBitSize());
		Bounds outputBounds = GuiUtils.getBounds(graphics.getFont(), outputString);
		
		graphics.fillText(
			outputString,
			getScreenX() + getScreenWidth() - outputBounds.getWidth() - 3,
			getScreenY() + getScreenHeight() * 0.5 + 5);
		
		String typeString = switch (getComponent().getExtensionType()) {
			case ZERO -> "0";
			case ONE -> "1";
			case SIGN -> "sign";
		};
		
		Bounds typeBounds = GuiUtils.getBounds(graphics.getFont(), typeString);
		graphics.fillText(
			typeString,
			getScreenX() + (getScreenWidth() - typeBounds.getWidth()) * 0.5,
			getScreenY() + typeBounds.getHeight());
		
		graphics.setFont(GuiUtils.getFont(10, true));
		String extendString = "extend";
		Bounds extendBounds = GuiUtils.getBounds(graphics.getFont(), extendString);
		graphics.fillText(
			extendString,
			getScreenX() + (getScreenWidth() - extendBounds.getWidth()) * 0.5,
			getScreenY() + getScreenHeight() - 5);
	}
}
