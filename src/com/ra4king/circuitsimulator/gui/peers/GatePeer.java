package com.ra4king.circuitsimulator.gui.peers;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.simulator.components.gates.Gate;
import com.ra4king.circuitsimulator.simulator.components.gates.NandGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NorGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NotGate;
import com.ra4king.circuitsimulator.simulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XnorGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * @author Roi Atalla
 */
public class GatePeer extends ComponentPeer<Gate> {
	private List<Connection> connections = new ArrayList<>();
	
	public GatePeer(Gate gate, int x, int y) {
		super(gate, x, y, 4, gate.getNumPorts() == 2 ? 2 : 4);
		
		int gates = gate.getNumPorts() - 1;
		for(int i = 0; i < gates; i++) {
			int add = (gates % 2 == 0 && i >= gates / 2) ? 3 : 2;
			connections.add(new PortConnection(this, gate.getPort(i), 0, i + add - gates / 2 - (gates == 1 ? 1 : 0)));
		}
		
		connections.add(new PortConnection(this, gate.getPort(gates), getWidth(), getHeight() / 2));
	}
	
	@Override
	public List<Connection> getConnections() {
		return connections;
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		if(getComponent().getClass() == AndGate.class) {
			graphics.beginPath();
			graphics.moveTo(getScreenX(), getScreenY());
			graphics.lineTo(getScreenX(), getScreenY() + getScreenHeight());
			graphics.arc(getScreenX() + getScreenWidth() * 0.5, getScreenY() + getScreenHeight() * 0.5, getScreenWidth() * 0.5, getScreenHeight() * 0.5, 270, 180);
			graphics.closePath();
			
			graphics.setFill(Color.WHITE);
			graphics.setStroke(Color.BLACK);
			graphics.fill();
			graphics.stroke();
		} else if(getComponent().getClass() == NandGate.class) {
			graphics.beginPath();
			graphics.moveTo(getScreenX(), getScreenY());
			graphics.lineTo(getScreenX(), getScreenY() + getScreenHeight());
			graphics.arc(getScreenX() + getScreenWidth() * 0.3, getScreenY() + getScreenHeight() * 0.5, getScreenWidth() * 0.5, getScreenHeight() * 0.5, 270, 180);
			graphics.closePath();
			
			graphics.setFill(Color.WHITE);
			graphics.setStroke(Color.BLACK);
			graphics.fill();
			graphics.stroke();
			
			graphics.fillOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
			graphics.strokeOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
		} else if(getComponent().getClass() == OrGate.class) {
			graphics.beginPath();
			graphics.moveTo(getScreenX(), getScreenY() + getScreenHeight());
			graphics.arc(getScreenX(), getScreenY() + getScreenHeight() * 0.5, getScreenWidth() * 0.25, getScreenHeight() * 0.5, 270, 180);
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth() * 1.25, getScreenY() + getScreenHeight(), getScreenWidth());
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(), getScreenX(), getScreenY() + getScreenHeight(), getScreenWidth());
			graphics.closePath();
			
			graphics.setFill(Color.WHITE);
			graphics.setStroke(Color.BLACK);
			graphics.fill();
			graphics.stroke();
		} else if(getComponent().getClass() == NorGate.class) {
			graphics.beginPath();
			graphics.moveTo(getScreenX(), getScreenY() + getScreenHeight());
			graphics.arc(getScreenX(), getScreenY() + getScreenHeight() * 0.5, getScreenWidth() * 0.25, getScreenHeight() * 0.5, 270, 180);
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth(), getScreenY() + getScreenHeight() * 1.3, getScreenWidth() * 0.7);
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(), getScreenX(), getScreenY() + getScreenHeight(), getScreenWidth() * 0.7);
			graphics.closePath();
			
			graphics.setFill(Color.WHITE);
			graphics.setStroke(Color.BLACK);
			graphics.fill();
			graphics.stroke();
			
			graphics.fillOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
			graphics.strokeOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
		} else if(getComponent().getClass() == XorGate.class) {
			graphics.beginPath();
			graphics.moveTo(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight());
			graphics.arc(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight() * 0.5, getScreenWidth() * 0.25, getScreenHeight() * 0.5, 270, 180);
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth() * 1.25, getScreenY() + getScreenHeight(), getScreenWidth());
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(), getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight(), getScreenWidth());
			graphics.closePath();
			
			graphics.setFill(Color.WHITE);
			graphics.setStroke(Color.BLACK);
			graphics.fill();
			graphics.stroke();
			
			graphics.strokeArc(getScreenX() - getScreenWidth() * 0.3, getScreenY(), getScreenWidth() * 0.5, getScreenHeight(), 270, 180, ArcType.OPEN);
		} else if(getComponent().getClass() == XnorGate.class) {
			graphics.beginPath();
			graphics.moveTo(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight());
			graphics.arc(getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight() * 0.5, getScreenWidth() * 0.25, getScreenHeight() * 0.5, 270, 180);
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY(), getScreenX() + getScreenWidth(), getScreenY() + getScreenHeight() * 1.3, getScreenWidth() * 0.7);
			graphics.arcTo(getScreenX() + getScreenWidth() * 0.66, getScreenY() + getScreenHeight(), getScreenX() + getScreenWidth() * 0.1, getScreenY() + getScreenHeight(), getScreenWidth() * 0.7);
			graphics.closePath();
			
			graphics.setFill(Color.WHITE);
			graphics.setStroke(Color.BLACK);
			graphics.fill();
			graphics.stroke();
			
			graphics.strokeArc(getScreenX() - getScreenWidth() * 0.3, getScreenY(), getScreenWidth() * 0.5, getScreenHeight(), 270, 180, ArcType.OPEN);
			
			graphics.fillOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
			graphics.strokeOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
		} else if(getComponent().getClass() == NotGate.class) {
			graphics.beginPath();
			graphics.moveTo(getScreenX(), getScreenY());
			graphics.lineTo(getScreenX(), getScreenY() + getScreenHeight());
			graphics.lineTo(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5);
			graphics.closePath();
			
			graphics.setFill(Color.WHITE);
			graphics.setStroke(Color.BLACK);
			graphics.fill();
			graphics.stroke();
			
			graphics.fillOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
			graphics.strokeOval(getScreenX() + getScreenWidth() * 0.8, getScreenY() + getScreenHeight() * 0.5 - getScreenWidth() * 0.1, getScreenWidth() * 0.2, getScreenWidth() * 0.2);
		} else {
			graphics.setFill(Color.WHITE);
			GuiUtils.drawShape(graphics::fillRect, this);
			
			graphics.setStroke(Color.BLACK);
			GuiUtils.drawShape(graphics::strokeRect, this);
		}
	}
}
