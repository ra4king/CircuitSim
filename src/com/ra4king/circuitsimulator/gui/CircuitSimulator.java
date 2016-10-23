package com.ra4king.circuitsimulator.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Connection.WireConnection;
import com.ra4king.circuitsimulator.gui.LinkWires.Wire;
import com.ra4king.circuitsimulator.gui.peers.AdderPeer;
import com.ra4king.circuitsimulator.gui.peers.ClockPeer;
import com.ra4king.circuitsimulator.gui.peers.ControlledBufferPeer;
import com.ra4king.circuitsimulator.gui.peers.GatePeer;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.gui.peers.RegisterPeer;
import com.ra4king.circuitsimulator.gui.peers.SplitterPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.ShortCircuitException;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;
import com.ra4king.circuitsimulator.simulator.components.Adder;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.components.ControlledBuffer;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.Register;
import com.ra4king.circuitsimulator.simulator.components.Splitter;
import com.ra4king.circuitsimulator.simulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NorGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NotGate;
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
	private Map<Link, LinkWires> linkWiresMap;
	private List<LinkWires> unlinkedWires;
	
	private Point lastPosition = new Point(0, 0);
	private ComponentPeer<?> potentialComponent;
	private Circuit dummyCircuit = new Circuit(new Simulator());
	private DummyCircuitState dummyCircuitState = new DummyCircuitState(dummyCircuit);
	
	private Connection startConnection, endConnection;
	private Point startPoint, draggedPoint;
	private boolean isDraggedHorizontally;
	
	private Set<GuiElement> selectedElements = new HashSet<>();
	
	private int bitSize = 1;
	private int componentMode = 0;
	
	public CircuitSimulator() {
		simulator = new Simulator();
		circuit = new Circuit(simulator);
		componentPeers = new ArrayList<>();
		linkWiresMap = new HashMap<>();
		unlinkedWires = new ArrayList<>();
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		setFocusable(true);
		requestFocus();
		
		Clock.addChangeListener(value -> {
			runSim();
			repaint();
		});
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
		
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		g2.setColor(Color.BLACK);
		g2.drawString("Bit Size: " + bitSize, 5, 15);
		if(potentialComponent != null) {
			g2.drawString("Component: " + potentialComponent.getComponent().toString(), 5, 30);
		}
		
		for(ComponentPeer<?> peer : componentPeers) {
			peer.paint((Graphics2D)g2.create(), circuit.getTopLevelState());
			
			for(Connection connection : peer.getConnections()) {
				connection.paint((Graphics2D)g2.create(), circuit.getTopLevelState());
			}
		}
		
		Stream.concat(linkWiresMap.values().stream(), unlinkedWires.stream()).forEach(linkWire -> {
			linkWire.paint((Graphics2D)g2.create(), circuit.getTopLevelState());
			
			for(Wire wire : linkWire.getWires()) {
				wire.paint((Graphics2D)g2.create(), circuit.getTopLevelState());
				
				List<Connection> connections = wire.getConnections();
				connections.get(0).paint((Graphics2D)g2.create(), circuit.getTopLevelState());
				connections.get(connections.size() - 1).paint((Graphics2D)g2.create(), circuit.getTopLevelState());
			}
		});
		
		if(startConnection != null) {
			Stroke old = g2.getStroke();
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.GREEN);
			
			g.drawOval(startConnection.getX() - 2, startConnection.getY() - 2, 10, 10);
			
			if(endConnection != null) {
				g.drawOval(endConnection.getX() - 2, endConnection.getY() - 2, 10, 10);
			}
			
			if(draggedPoint != null) {
				g2.setColor(Color.BLACK);
				int selectedMidX = startConnection.getX() + startConnection.getWidth() / 2;
				int selectedMidY = startConnection.getY() + startConnection.getHeight() / 2;
				if(isDraggedHorizontally) {
					g.drawLine(selectedMidX, selectedMidY, draggedPoint.x, selectedMidY);
					g.drawLine(draggedPoint.x, selectedMidY, draggedPoint.x, draggedPoint.y);
				} else {
					g.drawLine(selectedMidX, selectedMidY, selectedMidX, draggedPoint.y);
					g.drawLine(selectedMidX, draggedPoint.y, draggedPoint.x, draggedPoint.y);
				}
			}
			
			g2.setStroke(old);
		} else if(potentialComponent != null) {
			potentialComponent.paint((Graphics2D)g2.create(), dummyCircuitState);
		} else if(startPoint != null) {
			int startX = startPoint.x < draggedPoint.x ? startPoint.x : draggedPoint.x;
			int startY = startPoint.y < draggedPoint.y ? startPoint.y : draggedPoint.y;
			int width = Math.abs(draggedPoint.x - startPoint.x);
			int height = Math.abs(draggedPoint.y - startPoint.y);
			
			g.setColor(Color.GREEN.darker());
			g.drawRect(startX, startY, width, height);
		}
		
		for(GuiElement selectedElement : selectedElements) {
			g.setColor(Color.RED);
			GuiUtils.drawShape(g2::drawRect, selectedElement);
		}
	}
	
	public void runSim() {
		try {
			simulator.stepAll();
		} catch(ShortCircuitException exc) {
			exc.printStackTrace();
		}
	}
	
	private ComponentPeer<?> createComponent(Circuit circuit, int x, int y) {
		switch(componentMode) {
			case 1:
				return new GatePeer(circuit.addComponent(new AndGate("", bitSize, 2)), x, y);
			case 2:
				return new PinPeer(circuit.addComponent(new Pin("", bitSize, true)), x, y);
			case 3:
				return new PinPeer(circuit.addComponent(new Pin("", bitSize, false)), x, y);
			case 4:
				return new GatePeer(circuit.addComponent(new OrGate("", bitSize, 2)), x, y);
			case 5:
				return new GatePeer(circuit.addComponent(new NorGate("", bitSize, 2)), x, y);
			case 6:
				return new GatePeer(circuit.addComponent(new XorGate("", bitSize, 2)), x, y);
			case 7:
				return new GatePeer(circuit.addComponent(new NotGate("", bitSize)), x, y);
			case 8:
				return new ControlledBufferPeer(circuit.addComponent(new ControlledBuffer("", bitSize)), x, y);
			case 9:
				return new ClockPeer(circuit.addComponent(new Clock("")), x, y);
			case 10:
				return new RegisterPeer(circuit.addComponent(new Register("", bitSize)), x, y);
			case 11:
				return new AdderPeer(circuit.addComponent(new Adder("", bitSize)), x, y);
			case 12:
				return new SplitterPeer(circuit.addComponent(new Splitter("", bitSize, bitSize)), x, y);
		}
		
		return null;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_0:
			case KeyEvent.VK_1:
				int value = e.getKeyCode() - KeyEvent.VK_0;
				
				GuiElement selectedElem;
				if(selectedElements.size() == 1 && (selectedElem = selectedElements.iterator().next()) instanceof PinPeer) {
					PinPeer selectedPin = (PinPeer)selectedElem;
					WireValue currentValue = new WireValue(circuit.getTopLevelState()
							                                      .getCurrentValue(selectedPin
									                                                       .getComponent()
									                                                       .getPort(Pin.PORT)));
					for(int i = currentValue.getBitSize() - 1; i > 0; i--) {
						currentValue.setBit(i, currentValue.getBit(i - 1));
					}
					currentValue.setBit(0, value == 1 ? State.ONE : State.ZERO);
					selectedPin.getComponent().setValue(circuit.getTopLevelState(), currentValue);
					runSim();
					break;
				}
			case KeyEvent.VK_2:
			case KeyEvent.VK_3:
			case KeyEvent.VK_4:
			case KeyEvent.VK_5:
			case KeyEvent.VK_6:
			case KeyEvent.VK_7:
			case KeyEvent.VK_8:
			case KeyEvent.VK_9:
				if(startConnection == null) {
					value = e.getKeyCode() - KeyEvent.VK_0;
					if(value > 0)
						bitSize = value;
				}
				break;
			case KeyEvent.VK_SPACE:
				if(Clock.isRunning()) {
					Clock.stopClock();
				} else {
					Clock.startClock(1);
				}
				break;
			case KeyEvent.VK_DELETE:
				for(GuiElement selectedElement : selectedElements) {
					List<Connection> connections = selectedElement.getConnections();
					if(selectedElement instanceof ComponentPeer<?>) {
						for(Connection connection : connections) {
							PortConnection portConnection = (PortConnection)connection;
							Link link = portConnection.getLink();
							LinkWires linkWires = linkWiresMap.get(link);
							if(linkWires != null) {
								linkWires.removePort(portConnection);
								if(linkWires.getLink() == null) {
									linkWiresMap.remove(link);
									unlinkedWires.add(linkWires);
								}
							}
						}
						componentPeers.remove(selectedElement);
						circuit.removeComponent(((ComponentPeer)selectedElement).getComponent());
					} else if(selectedElement instanceof Wire) {
						Wire wire = (Wire)selectedElement;
						LinkWires linkWires = wire.getLinkWires();
						if(linkWires.getLink() != null) {
							linkWiresMap.remove(linkWires.getLink(), linkWires);
						} else {
							unlinkedWires.remove(linkWires);
						}
						
						List<LinkWires> newLinkWires = linkWires.removeWire(wire);
						for(LinkWires wires : newLinkWires) {
							if(wires.getLink() == null) {
								unlinkedWires.add(wires);
							} else {
								linkWiresMap.put(wires.getLink(), wires);
							}
						}
					}
					runSim();
				}
			case KeyEvent.VK_ESCAPE:
				componentMode = 0;
				selectedElements.clear();
				startConnection = null;
				endConnection = null;
				startPoint = null;
				draggedPoint = null;
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
			case KeyEvent.VK_R:
				componentMode = 5;
				break;
			case KeyEvent.VK_X:
				componentMode = 6;
				break;
			case KeyEvent.VK_N:
				componentMode = 7;
				break;
			case KeyEvent.VK_B:
				componentMode = 8;
				break;
			case KeyEvent.VK_C:
				componentMode = 9;
				break;
			case KeyEvent.VK_G:
				componentMode = 10;
				break;
			case KeyEvent.VK_D:
				componentMode = 11;
				break;
			case KeyEvent.VK_S:
				componentMode = 12;
				break;
		}
		
		dummyCircuit.getComponents().clear();
		potentialComponent = createComponent(dummyCircuit, lastPosition.x, lastPosition.y);
		
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
		int x = GuiUtils.getNearestCoord(e.getX());
		int y = GuiUtils.getNearestCoord(e.getY());
		
		if(startConnection != null) {
			draggedPoint = new Point(e.getX(), e.getY());
		} else if(potentialComponent != null) {
			for(ComponentPeer<?> component : componentPeers) {
				if(component.contains(x, y)) {
					return;
				}
			}
			
			ComponentPeer<?> peer = createComponent(circuit, x, y);
			
			if(peer != null) {
				for(Connection connection : peer.getConnections()) {
					Connection attached = findConnection(connection.getX(), connection.getY());
					if(attached != null) {
						if(attached instanceof WireConnection) {
							LinkWires linkWires = ((WireConnection)attached).getLinkWires();
							linkWires.addPort((PortConnection)connection);
						}
					}
				}
				
				componentPeers.add(peer);
			}
			
			runSim();
		} else {
			startPoint = new Point(x, y);
			draggedPoint = new Point(x, y);
			
			Optional<GuiElement> clickedComponent = Stream.concat(componentPeers.stream(), linkWiresMap.values().stream().flatMap(link -> link.getWires().stream()))
					                               .filter(peer -> peer.contains(e.getX(), e.getY()))
					                               .findAny();
			if(clickedComponent.isPresent()) {
				GuiElement selectedElement = clickedComponent.get();
				selectedElements.clear();
				selectedElements.add(selectedElement);
				if(selectedElement instanceof PinPeer && ((PinPeer)selectedElement).isInput()) {
					Pin pin = ((PinPeer)selectedElement).getComponent();
					WireValue value = circuit.getTopLevelState().getValue(pin.getPort(Pin.PORT));
					if(value.getBitSize() == 1) {
						pin.setValue(circuit.getTopLevelState(), new WireValue(1, value.getBit(0) == State.ONE ? State.ZERO : State.ONE));
					}
					runSim();
				}
			} else {
				selectedElements.clear();
			}
		}
		repaint();
	}
	
	private void handleConnection(Connection connection, LinkWires linkWires) {
		if(connection instanceof PortConnection) {
			linkWires.addPort((PortConnection)connection);
			linkWiresMap.put(linkWires.getLink(), linkWires);
		} else if(connection instanceof WireConnection) {
			LinkWires selectedLink = ((Wire)connection.getParent()).getLinkWires();
			Link link = selectedLink.getLink();
			if(link != null) {
				linkWiresMap.remove(link);
			} else {
				unlinkedWires.remove(selectedLink);
			}
			
			link = linkWires.getLink();
			if(link != null) {
				linkWiresMap.remove(link);
			} else {
				unlinkedWires.remove(linkWires);
			}
			
			linkWires.merge(selectedLink);
			link = linkWires.getLink();
			if(link != null) {
				linkWiresMap.put(link, linkWires);
			} else {
				unlinkedWires.add(linkWires);
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(draggedPoint != null && startConnection != null) {
			LinkWires link = new LinkWires();
			
			boolean createLink = true;
			
			int selectedMidX = startConnection.getX() + startConnection.getWidth() / 2;
			int selectedMidY = startConnection.getY() + startConnection.getHeight() / 2;
			int endMidX = endConnection == null ? draggedPoint.x : endConnection.getX() + endConnection.getWidth() / 2;
			int endMidY = endConnection == null ? draggedPoint.y : endConnection.getY() + endConnection.getHeight() / 2;
			
			if(endMidX - selectedMidX != 0 && endMidY - selectedMidY != 0) {
				if(isDraggedHorizontally) {
					link.addWire(link.new Wire(selectedMidX, selectedMidY, endMidX - selectedMidX, true));
					link.addWire(link.new Wire(endMidX, selectedMidY, endMidY - selectedMidY, false));
				} else {
					link.addWire(link.new Wire(selectedMidX, selectedMidY, endMidY - selectedMidY, false));
					link.addWire(link.new Wire(selectedMidX, endMidY, endMidX - selectedMidX, true));
				}
			}
			else if(endMidX - selectedMidX != 0) {
				link.addWire(link.new Wire(selectedMidX, selectedMidY, endMidX - selectedMidX, true));
			}
			else if(endMidY - selectedMidY != 0) {
				link.addWire(link.new Wire(endMidX, selectedMidY, endMidY - selectedMidY, false));
			} else {
				createLink = false;
			}
			
			if(createLink) {
				handleConnection(startConnection, link);
				
				if(endConnection != null) {
					handleConnection(endConnection, link);
				}
				
				runSim();
			}
			
			startConnection = null;
			endConnection = null;
		}
		
		startPoint = null;
		draggedPoint = null;
		mouseMoved(e);
		repaint();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(startPoint != null) {
			int startX = startPoint.x < draggedPoint.x ? startPoint.x : draggedPoint.x;
			int startY = startPoint.y < draggedPoint.y ? startPoint.y : draggedPoint.y;
			int width = Math.abs(draggedPoint.x - startPoint.x);
			int height = Math.abs(draggedPoint.y - startPoint.y);
			
			selectedElements =
					Stream.concat(componentPeers.stream(),
							Stream.concat(linkWiresMap.values().stream(), unlinkedWires.stream())
									.flatMap(link -> link.getWires().stream()))
							.filter(peer -> peer.intersects(startX, startY, width, height)).collect(Collectors.toSet());
		}
		
		if(draggedPoint != null) {
			if(startConnection != null) {
				int currDiffX = e.getX() - startConnection.getX();
				int prevDiffX = draggedPoint.x - startConnection.getX();
				int currDiffY = e.getY() - startConnection.getY();
				int prevDiffY = draggedPoint.y - startConnection.getY();
				
				if(currDiffX == 0 || prevDiffX == 0 || currDiffX / Math.abs(currDiffX) != prevDiffX / Math.abs(prevDiffX)) {
					isDraggedHorizontally = false;
				}
				
				if(currDiffY == 0 || prevDiffY == 0 || currDiffY / Math.abs(currDiffY) != prevDiffY / Math.abs(prevDiffY)) {
					isDraggedHorizontally = true;
				}
			}
			
			draggedPoint.setLocation(GuiUtils.getNearestCoord(e.getX()), GuiUtils.getNearestCoord(e.getY()));
			endConnection = findConnection(e.getX(), e.getY());
			repaint();
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		startConnection = findConnection(e.getX(), e.getY());
		
		int x = GuiUtils.getNearestCoord(e.getX());
		int y = GuiUtils.getNearestCoord(e.getY());
		
		lastPosition.setLocation(x, y);
		if(potentialComponent != null) {
			potentialComponent.setX(x);
			potentialComponent.setY(y);
		}
		
		repaint();
	}
	
	private Connection findConnection(int x, int y) {
		Optional<Connection> optionalSelected =
				Stream.concat(
						Stream.concat(linkWiresMap.values().stream(), unlinkedWires.stream())
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
