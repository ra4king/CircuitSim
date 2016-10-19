package com.ra4king.circuitsimulator.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.peers.GatePeer;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NorGate;
import com.ra4king.circuitsimulator.simulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

/**
 * @author Roi Atalla
 */
public class CircuitSimulator extends JComponent implements KeyListener, MouseListener, MouseMotionListener {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Circuit Simulator");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		CircuitSimulator canvas = new CircuitSimulator();
		canvas.setPreferredSize(new Dimension(800, 600));
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
	
	private Simulator simulator;
	private Circuit circuit;
	
	private List<ComponentPeer<?>> componentPeers;
	private List<LinkWires> links;
	
	private Connection selected, draggedSelected;
	private Point dragged;
	
	private int inputMode = 1;
	private int componentMode = 1;
	
	public CircuitSimulator() {
		simulator = new Simulator();
		circuit = new Circuit(simulator);
		componentPeers = new ArrayList<>();
		links = new ArrayList<>();
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		setFocusable(true);
		requestFocus();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		g2.setColor(Color.BLACK);
		g2.drawString("Input Mode " + inputMode, 5, 15);
		g2.drawString("Component Mode " + componentMode, 5, 30);
		
		for(ComponentPeer<?> peer : componentPeers) {
			peer.paint(g2, circuit.getTopLevelState());
			
			for(Connection connection : peer.getConnections()) {
				connection.paint(g2, circuit.getTopLevelState());
			}
		}
		
		for(LinkWires linkWires : links) {
			linkWires.paint(g2, circuit.getTopLevelState());
			
			for(Wire wire : linkWires.getWires()) {
				wire.paint(g2, circuit.getTopLevelState());
				
				List<Connection> connections = wire.getConnections();
				connections.get(0).paint(g2, circuit.getTopLevelState());
				connections.get(connections.size() - 1).paint(g2, circuit.getTopLevelState());
			}
		}
		
		if(selected != null) {
			Stroke old = g2.getStroke();
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.GREEN);
			
			GuiUtils.drawShape(g2::drawOval, selected);
			
			if(draggedSelected != null) {
				GuiUtils.drawShape(g2::drawOval, draggedSelected);
			}
			
			if(dragged != null) {
				g2.setColor(Color.BLACK);
				int selectedMidX = selected.getX() + selected.getWidth() / 2;
				int selectedMidY = selected.getY() + selected.getHeight() / 2;
				g.drawLine(selectedMidX, selectedMidY, dragged.x, selectedMidY);
				g.drawLine(dragged.x, selectedMidY, dragged.x, dragged.y);
			}
			
			g2.setStroke(old);
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_1:
				inputMode = 1;
				break;
			case KeyEvent.VK_2:
				inputMode = 2;
				break;
			case KeyEvent.VK_A:
				componentMode = 1;
				break;
			case KeyEvent.VK_I:
				componentMode = 2;
				break;
			case KeyEvent.VK_U:
				componentMode = 3;
				break;
			case KeyEvent.VK_O:
				componentMode = 4;
				break;
			case KeyEvent.VK_N:
				componentMode = 5;
				break;
			case KeyEvent.VK_X:
				componentMode = 6;
				break;
		}
		
		repaint();
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(selected != null) {
			dragged = new Point(e.getX(), e.getY());
		} else if(inputMode == 1) {
			for(ComponentPeer<?> component : componentPeers) {
				if(component.contains(e.getX(), e.getY())) {
					return;
				}
			}
			
			switch(componentMode) {
				case 1:
					componentPeers.add(new GatePeer(circuit.addComponent(new AndGate("", 1, 2)), e.getX(), e.getY()));
					break;
				case 2:
					componentPeers.add(new PinPeer(circuit.addComponent(new Pin("", 1, true)), e.getX(), e.getY()));
					break;
				case 3:
					componentPeers.add(new PinPeer(circuit.addComponent(new Pin("", 1, false)), e.getX(), e.getY()));
					break;
				case 4:
					componentPeers.add(new GatePeer(circuit.addComponent(new OrGate("", 1, 2)), e.getX(), e.getY()));
					break;
				case 5:
					componentPeers.add(new GatePeer(circuit.addComponent(new NorGate("", 1, 2)), e.getX(), e.getY()));
					break;
				case 6:
					componentPeers.add(new GatePeer(circuit.addComponent(new XorGate("", 1, 2)), e.getX(), e.getY()));
					break;
			}
			
			simulator.stepAll();
		} else if(inputMode == 2) {
			Optional<PinPeer> clickedPin = componentPeers.stream()
					                               .filter(peer -> peer.contains(e.getX(), e.getY()) && peer instanceof PinPeer)
					                               .map(peer -> (PinPeer)peer).filter(PinPeer::isInput).findAny();
			if(clickedPin.isPresent()) {
				Pin pin = clickedPin.get().getComponent();
				WireValue value = circuit.getTopLevelState().getValue(pin.getPort(0));
				if(value.getBitSize() == 1) {
					pin.setValue(circuit.getTopLevelState(), new WireValue(1, value.getBit(0) == State.ONE ? State.ZERO : State.ONE));
				}
				simulator.stepAll();
			}
		}
		repaint();
	}
	
	private void handleConnection(Connection connection, LinkWires link) {
		if(connection instanceof PortConnection) {
			link.addPort(((PortConnection)connection).getPort());
		} else if(connection instanceof WireConnection) {
			LinkWires selectedLink = ((Wire)connection.getParent()).getLinkWires();
			link.merge(selectedLink);
			links.remove(selectedLink);
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(dragged != null) {
			LinkWires link = new LinkWires();
			
			handleConnection(selected, link);
			
			if(draggedSelected != null) {
				handleConnection(draggedSelected, link);
			}
			
			int selectedMidX = selected.getX() + selected.getWidth() / 2;
			int selectedMidY = selected.getY() + selected.getHeight() / 2;
			int endMidX = draggedSelected == null ? dragged.x : draggedSelected.getX() + draggedSelected.getWidth() / 2;
			int endMidY = draggedSelected == null ? dragged.y : draggedSelected.getY() + draggedSelected.getHeight() / 2; 
			if(endMidX - selectedMidX != 0) {
				link.addWire(link.new Wire(selectedMidX, selectedMidY, endMidX - selectedMidX, true));
			}
			if(endMidY - selectedMidY != 0) {
				link.addWire(link.new Wire(endMidX, selectedMidY, endMidY - selectedMidY, false));
			}
			
			links.add(link);
			
			simulator.stepAll();
			
			selected = null;
			dragged = null;
			draggedSelected = null;
			repaint();
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(dragged != null) {
			dragged.setLocation(e.getX(), e.getY());
			draggedSelected = connectionSelected(e.getX(), e.getY());
			repaint();
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if(dragged != null)
			return;
		
		Connection currSelected = connectionSelected(e.getX(), e.getY());
		if(currSelected != selected) {
			selected = currSelected;
			repaint();
		}
	}
	
	private Connection connectionSelected(int x, int y) {
		Optional<Connection> optionalSelected =
				Stream.concat(
						links.stream()
								.flatMap(link -> link.getWires().stream())
								.flatMap(wire -> wire.getConnections().stream()), 
						componentPeers.stream().flatMap(peer -> peer.getConnections().stream()))
						.filter(c -> c.contains(x, y)).findAny();
		
		if(optionalSelected.isPresent()) {
			return optionalSelected.get();
		} else {
			return null;
		}
	}
}
