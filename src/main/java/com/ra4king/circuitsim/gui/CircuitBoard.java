package com.ra4king.circuitsim.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsim.gui.Connection.PortConnection;
import com.ra4king.circuitsim.gui.Connection.WireConnection;
import com.ra4king.circuitsim.gui.EditHistory.EditAction;
import com.ra4king.circuitsim.gui.LinkWires.Wire;
import com.ra4king.circuitsim.gui.PathFinding.LocationPreference;
import com.ra4king.circuitsim.gui.PathFinding.Point;
import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.SimulationException;
import com.ra4king.circuitsim.simulator.Simulator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class CircuitBoard {
	private CircuitManager circuitManager;
	private Circuit circuit;
	private CircuitState currentState;
	
	private Set<ComponentPeer<?>> components;
	private Set<LinkWires> links;
	private Set<LinkWires> badLinks;
	
	private Set<GuiElement> moveElements;
	private Set<Connection> connectedPorts = new HashSet<>();
	
	private Thread computeThread;
	private MoveComputeResult moveResult;
	private boolean addMoveAction;
	private int moveDeltaX, moveDeltaY;
	
	private Map<Pair<Integer, Integer>, Set<Connection>> connectionsMap;
	
	private EditHistory editHistory;
	
	private static class MoveComputeResult {
		final Set<Wire> wiresToAdd;
		final Set<Wire> wiresToRemove;
		
		MoveComputeResult(Set<Wire> wiresToAdd, Set<Wire> wiresToRemove) {
			this.wiresToAdd = wiresToAdd;
			this.wiresToRemove = wiresToRemove;
		}
	}
	
	public CircuitBoard(String name, CircuitManager circuitManager, Simulator simulator, EditHistory editHistory) {
		this.circuitManager = circuitManager;
		
		circuit = new Circuit(name, simulator);
		currentState = circuit.getTopLevelState();
		
		this.editHistory = editHistory;
		
		components = new HashSet<>();
		links = new HashSet<>();
		
		connectionsMap = new HashMap<>();
	}
	
	public String getName() {
		return circuit.getName();
	}
	
	public void setName(String name) {
		circuit.setName(name);
	}
	
	@Override
	public String toString() {
		return "CircuitBoard of " + circuitManager;
	}
	
	public void destroy() {
		try {
			removeElements(components);
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			removeElements(links.stream().flatMap(l -> l.getWires().stream()).collect(Collectors.toSet()));
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		
		badLinks.clear();
		moveElements = null;
		circuit.getSimulator().removeCircuit(circuit);
	}
	
	public Circuit getCircuit() {
		return circuit;
	}
	
	public CircuitState getCurrentState() {
		return currentState;
	}
	
	public void setCurrentState(CircuitState state) {
		if(currentState == null) {
			throw new NullPointerException("CircuitState cannot be null");
		}
		
		if(state.getCircuit() != this.circuit) {
			throw new IllegalArgumentException("The state does not belong to this circuit.");
		}
		
		currentState = state;
	}
	
	public Set<ComponentPeer<?>> getComponents() {
		return components;
	}
	
	public Set<LinkWires> getLinks() {
		return links;
	}
	
	private Exception lastException;
	
	public Exception getLastException() {
		return lastException;
	}
	
	private void updateBadLinks() {
		if((badLinks = links.stream().filter(
			link -> !link.isLinkValid()).collect(Collectors.toSet())).size() > 0) {
			lastException = badLinks.iterator().next().getLastException();
		} else {
			lastException = null;
		}
	}
	
	public boolean isValidLocation(ComponentPeer<?> component) {
		return component.getX() >= 0
			       && component.getY() >= 0
			       && Stream.concat(components.stream(),
			                        moveElements != null
			                        ? moveElements.stream().filter(e -> e instanceof ComponentPeer<?>)
			                        : Stream.empty())
			                .noneMatch(c -> c != component && c.getX() == component.getX()
				                                && c.getY() == component.getY());
	}
	
	public void addComponent(ComponentPeer<?> component) {
		addComponent(component, true);
	}
	
	synchronized void addComponent(ComponentPeer<?> component, boolean splitWires) {
		if(!isValidLocation(component)) {
			throw new SimulationException("Cannot place component here.");
		}
		
		circuit.getSimulator().runSync(() -> {
			// Component must be added here before the circuit as listeners will be triggered to recreate Subcircuits
			components.add(component);
			
			try {
				circuit.addComponent(component.getComponent());
			} catch(Exception exc) {
				components.remove(component);
				throw exc;
			}
			
			try {
				editHistory.disable();
				
				if(splitWires) {
					Set<Wire> toReAdd = new HashSet<>();
					
					for(Connection connection : component.getConnections()) {
						Set<Connection> connections = getConnections(connection.getX(), connection.getY());
						if(connections != null) {
							for(Connection attached : connections) {
								LinkWires linkWires = attached.getLinkWires();
								linkWires.addPort((PortConnection)connection);
								links.add(linkWires);
								
								if(attached instanceof WireConnection) {
									Wire wire = (Wire)attached.getParent();
									if(attached != wire.getStartConnection() && attached != wire.getEndConnection()) {
										toReAdd.add(wire);
									}
								}
							}
						}
						
						addConnection(connection);
					}
					
					for(Wire wire : toReAdd) {
						removeWire(wire);
						addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
					}
					
					rejoinWires();
				}
				
				updateBadLinks();
			} finally {
				editHistory.enable();
			}
		});
		
		editHistory.addAction(EditAction.ADD_COMPONENT, circuitManager, component);
	}
	
	public void updateComponent(ComponentPeer<?> oldComponent, ComponentPeer<?> newComponent) {
		circuit.getSimulator().runSync(() -> {
			try {
				editHistory.disable();
				
				removeComponent(oldComponent, true);
				
				try {
					circuit.updateComponent(oldComponent.getComponent(), newComponent.getComponent(),
					                        () -> components.add(newComponent));
				} catch(Exception exc) {
					components.remove(newComponent);
					throw exc;
				}
				
				addComponent(newComponent);
			} finally {
				editHistory.enable();
				editHistory.addAction(EditAction.UPDATE_COMPONENT, circuitManager, oldComponent, newComponent);
			}
		});
	}
	
	public boolean isMoving() {
		return moveElements != null;
	}
	
	public void initMove(Set<GuiElement> elements) {
		initMove(elements, true);
	}
	
	void initMove(Set<GuiElement> elements, boolean remove) {
		if(moveElements != null) {
			try {
				finalizeMove();
			} catch(Exception exc) {
				circuitManager.getSimulatorWindow().getDebugUtil().logException(exc);
			}
		}
		
		connectedPorts.clear();
		moveElements = new LinkedHashSet<>(elements);
		addMoveAction = remove;
		
		if(remove) {
			for(GuiElement element : elements) {
				for(Connection connection : element.getConnections()) {
					if((connection instanceof PortConnection
						    || connection == ((Wire)connection.getParent()).getEndConnection()
						    || connection == ((Wire)connection.getParent()).getStartConnection())
						   && getConnections(connection.getX(), connection.getY()).size() > 1) {
						connectedPorts.add(connection);
					}
				}
			}
			
			editHistory.beginGroup();
			removeElements(elements, false);
		}
	}
	
	public void moveElements(int dx, int dy, boolean extendWires) {
		if(moveDeltaX == dx && moveDeltaY == dy) {
			return;
		}
		
		for(GuiElement element : moveElements) {
			element.setX(element.getX() + (-moveDeltaX + dx));
			element.setY(element.getY() + (-moveDeltaY + dy));
		}
		
		moveDeltaX = dx;
		moveDeltaY = dy;
		
		CountDownLatch latch = new CountDownLatch(1);
		
		synchronized(CircuitBoard.this) {
			moveResult = null;
			
			if(computeThread != null) {
				computeThread.interrupt();
			}
			
			if(!extendWires) {
				lastException = null;
				return;
			}
			
			List<Connection> connectedPorts = new ArrayList<>(this.connectedPorts);
			connectedPorts.sort((p1, p2) -> {
				if(p1.getX() == p2.getX()) {
					return dy > 0 ? p1.getY() - p2.getY() : p2.getY() - p1.getY();
				}
				
				return dx > 0 ? p1.getX() - p2.getX() : p2.getX() - p1.getX();
			});
			
			computeThread = new Thread(() -> {
				Set<Wire> paths = new HashSet<>();
				
				Set<Pair<Integer, Integer>> portsSeen = new HashSet<>();
				
				for(Connection connectedPort : connectedPorts) {
					if(Thread.currentThread().isInterrupted()) {
						return;
					}
					
					int x = connectedPort.getX();
					int y = connectedPort.getY();
					int sx = x - dx;
					int sy = y - dy;
					
					if(!portsSeen.add(new Pair<>(x, y))) {
						continue;
					}
					
					final LinkWires linkWires;
					
					synchronized(CircuitBoard.this) {
						Set<Connection> otherConnections = getConnections(sx, sy);
						if(otherConnections.isEmpty()) {
							continue;
						}
						
						LinkWires lw = null;
						for(Connection connection : otherConnections) {
							if(connection instanceof PortConnection) {
								if(lw != null && lw != connection.getLinkWires()) {
									throw new IllegalStateException("How is this remotely possible?!");
								}
								
								lw = connection.getLinkWires();
							} else if(connection instanceof WireConnection) {
								Wire wire = (Wire)connection.getParent();
								if(connection == wire.getStartConnection()
									   || connection == wire.getEndConnection()) {
									if(lw != null && lw != connection.getLinkWires()) {
										throw new IllegalStateException("How is this remotely possible?!");
									}
									
									lw = connection.getLinkWires();
								}
							}
						}
						
						linkWires = lw;
					}
					
					Set<ComponentPeer<?>> components = new HashSet<>(getComponents());
					components.addAll(moveElements.stream()
					                              .filter(e -> e instanceof ComponentPeer)
					                              .map(e -> (ComponentPeer<?>)e)
					                              .collect(Collectors.toSet()));
					
					Pair<Set<Wire>, Set<Point>> pair = PathFinding.bestPath(sx, sy, x, y, (px, py, horizontal) -> {
						if(px == x && py == y) {
							return LocationPreference.VALID;
						}
						
						if(px == sx && py == sy) {
							return LocationPreference.VALID;
						}
						
						Set<Connection> connections =
							Stream.concat(connectedPorts.stream(),
							              paths.stream().flatMap(w -> w.getConnections().stream()))
							      .filter(c -> c.getX() == px && c.getY() == py)
							      .collect(Collectors.toSet());
						synchronized(CircuitBoard.this) {
							connections.addAll(getConnections(px, py));
						}
						
						for(Connection connection : connections) {
							if(connection instanceof PortConnection) {
								return LocationPreference.INVALID;
							}
							
							if(connection.getLinkWires() == null
								   || linkWires == null
								   || connection.getLinkWires() == linkWires) {
								return LocationPreference.PREFER;
							}
							
							if(connection instanceof WireConnection) {
								Wire wire = (Wire)connection.getParent();
								if(wire.isHorizontal() == horizontal
									   || connection == wire.getStartConnection()
									   || connection == wire.getEndConnection()) {
									return LocationPreference.INVALID;
								}
							}
						}
						
						for(ComponentPeer<?> component : components) {
							if(component.contains(px, py)) {
								return LocationPreference.INVALID;
							}
						}
						
						return LocationPreference.VALID;
					});
					if(pair != null) {
						paths.addAll(pair.getKey());
					}
				}
				
				synchronized(CircuitBoard.this) {
					Set<Wire> toRemove = new HashSet<>();
					Set<Wire> toAdd = new HashSet<>();
					
					for(Wire newWire : paths) {
						if(wireAlreadyExists(newWire) != null) {
							toRemove.add(newWire);
						} else {
							boolean wasRemoved = false;
							
							for(LinkWires wires : links) {
								for(Wire existing : wires.getWires()) {
									if(existing.getLinkWires() == newWire.getLinkWires()
										   && existing.isHorizontal() == newWire.isHorizontal()) {
										if(existing.equals(newWire)) {
											wasRemoved = true;
											toRemove.add(existing);
											break;
										} else if(existing.isWithin(newWire)) {
											wasRemoved = true;
											toAdd.addAll(spliceWire(newWire, existing));
											toRemove.add(existing);
											break;
										} else if(newWire.isWithin(existing)) {
											wasRemoved = true;
											toAdd.addAll(spliceWire(existing, newWire));
											toRemove.add(newWire);
											break;
										} else if(existing.overlaps(newWire)) {
											Pair<Wire, Pair<Wire, Wire>> pairs =
												spliceOverlappingWire(newWire, existing);
											
											toAdd.add(pairs.getKey());
											toRemove.add(pairs.getValue().getKey());
											
											wasRemoved = true;
											break;
										}
									}
								}
							}
							
							if(!wasRemoved) {
								toAdd.add(newWire);
							}
						}
					}
					
					moveResult = new MoveComputeResult(toAdd, toRemove);
					if(computeThread == Thread.currentThread()) {
						computeThread = null;
					}
					
					lastException = null;
					
					circuitManager.setNeedsRepaint();
				}
				
				latch.countDown();
			});
			computeThread.start();
			
			lastException = new Exception("Computing...");
		}
		
		try {
			latch.await(20, TimeUnit.MILLISECONDS);
		} catch(InterruptedException e) {
			// ignore
		}
	}
	
	/**
	 * Returns the new set of selected elements by wires with actual ones they overlap.
	 * <p>
	 * Note only the ComponentPeers have their coordinates updated after this call. Wires will reset back to their
	 * original coordinates, thus the need for the returned set of updated wires.
	 */
	public Set<GuiElement> finalizeMove() {
		if(moveElements == null) {
			return null;
		}
		
		if(!addMoveAction) {
			editHistory.beginGroup();
		}
		
		MoveComputeResult result;
		
		synchronized(this) {
			if(computeThread != null) {
				computeThread.interrupt();
				computeThread = null;
			}
			
			result = moveResult;
			moveResult = null;
		}
		
		Set<Wire> wiresToAdd = result == null ? new HashSet<>() : result.wiresToAdd;
		Set<Wire> wiresRemoved = result == null ? new HashSet<>() : result.wiresToRemove;
		
		boolean cannotMoveHere = false;
		
		for(GuiElement element : moveElements) {
			if((element instanceof ComponentPeer<?> && !isValidLocation((ComponentPeer<?>)element)) ||
				   (element instanceof Wire && (element.getX() < 0 || element.getY() < 0))) {
				for(GuiElement element1 : moveElements) {
					element1.setX(element1.getX() - moveDeltaX);
					element1.setY(element1.getY() - moveDeltaY);
				}
				
				wiresToAdd.clear();
				wiresRemoved.clear();
				
				cannotMoveHere = true;
				break;
			}
		}
		
		removeElements(wiresRemoved);
		
		List<GuiElement> elements = new ArrayList<>(moveElements.size() + wiresToAdd.size());
		elements.addAll(moveElements);
		elements.addAll(wiresToAdd);
		
		Set<Wire> selectedWires = new HashSet<>();
		
		List<RuntimeException> toThrow = new ArrayList<>();
		
		boolean reset = cannotMoveHere;
		circuit.getSimulator().runSync(() -> {
			for(GuiElement element : elements) {
				if(element instanceof ComponentPeer<?>) {
					ComponentPeer<?> component = (ComponentPeer<?>)element;
					
					try {
						editHistory.beginGroup();
						editHistory.addAction(EditAction.MOVE_ELEMENT, circuitManager, component, moveDeltaX,
						                      moveDeltaY);
						addComponent(component, true);
					} catch(RuntimeException exc) {
						editHistory.clearGroup();
						toThrow.clear();
						toThrow.add(exc);
					} finally {
						editHistory.endGroup();
					}
				} else if(element instanceof Wire) {
					Wire wire = (Wire)element;
					try {
						editHistory.beginGroup();
						addWire(wire.getX(), wire.getY(), wire.getLength(), wire.isHorizontal());
						
						if(!reset) {
							// Make a copy of the wire for later use
							if(!wiresToAdd.contains(wire)) {
								selectedWires.add(new Wire(null, wire));
								
								// Things break in undo/redo if we don't reset wires back
								wire.setX(wire.getX() - moveDeltaX);
								wire.setY(wire.getY() - moveDeltaY);
							}
						}
					} catch(RuntimeException exc) {
						editHistory.clearGroup();
						toThrow.clear();
						toThrow.add(exc);
					} finally {
						editHistory.endGroup();
					}
				}
			}
		});
		
		Set<GuiElement> newSelectedElements;
		
		if(!cannotMoveHere) {
			for(GuiElement element : elements) {
				if(element instanceof ComponentPeer<?>) {
					ComponentPeer<?> component = (ComponentPeer<?>)element;
					// moving components doesn't actually modify the Circuit, so we must trigger the listener directly
					circuitManager.getSimulatorWindow().circuitModified(circuit, component.getComponent(), true);
				}
			}
			
			if(addMoveAction && moveDeltaX == 0 && moveDeltaY == 0) {
				editHistory.clearGroup();
			}
			
			newSelectedElements = new HashSet<>();
			moveElements.forEach(element -> {
				if(element instanceof ComponentPeer) {
					newSelectedElements.add(element);
				}
			});
			if(!selectedWires.isEmpty()) {
				links.forEach(
					linkWires ->
						linkWires.getWires().forEach(wire -> {
							if(selectedWires.contains(wire)) {
								newSelectedElements.add(wire);
							} else {
								selectedWires.forEach(selectedWire -> {
									if(selectedWire.overlaps(wire)) {
										newSelectedElements.add(wire);
									}
								});
							}
						}));
			}
		} else {
			newSelectedElements = null;
			
			if(addMoveAction) {
				editHistory.clearGroup();
			}
		}
		
		// Closes the beginGroup in initMove or in the beginning of this function
		editHistory.endGroup();
		
		moveElements = null;
		wiresToAdd.clear();
		connectedPorts.clear();
		moveDeltaX = 0;
		moveDeltaY = 0;
		
		updateBadLinks();
		
		if(cannotMoveHere) {
			throw new SimulationException("Cannot move components/wires here.");
		}
		
		if(!toThrow.isEmpty()) {
			throw toThrow.get(0);
		}
		
		return newSelectedElements;
	}
	
	public void removeElements(Set<? extends GuiElement> elements) {
		removeElements(elements, true);
	}
	
	synchronized void removeElements(Set<? extends GuiElement> elements, boolean removeFromCircuit) {
		circuit.getSimulator().runSync(() -> {
			try {
				editHistory.beginGroup();
				
				Map<LinkWires, Set<Wire>> wiresToRemove = new HashMap<>();
				
				Set<GuiElement> elementsToRemove = new HashSet<>(elements);
				
				while(!elementsToRemove.isEmpty()) {
					Iterator<GuiElement> iterator = elementsToRemove.iterator();
					GuiElement element = iterator.next();
					iterator.remove();
					
					if(element instanceof ComponentPeer<?>) {
						removeComponent((ComponentPeer<?>)element, removeFromCircuit);
						if(removeFromCircuit) {
							circuit.removeComponent(((ComponentPeer<?>)element).getComponent());
						}
					} else if(element instanceof Wire) {
						Wire wire = (Wire)element;
						
						Set<Wire> toRemove = new HashSet<>();
						for(int i = 0; i < wire.getLength(); i++) {
							int x = wire.isHorizontal() ? wire.getX() + i : wire.getX();
							int y = wire.isHorizontal() ? wire.getY() : wire.getY() + i;
							for(Connection conn : new HashSet<>(getConnections(x, y))) {
								if(conn instanceof WireConnection) {
									Wire w = (Wire)conn.getParent();
									if(w.isHorizontal() == wire.isHorizontal()) {
										if(w.equals(wire)) {
											toRemove.add(w);
											break;
										} else if(w.isWithin(wire)) {
											elementsToRemove.addAll(spliceWire(wire, w));
											
											toRemove.add(w);
											break;
										} else if(wire.isWithin(w)) {
											LinkWires linkWires = w.getLinkWires();
											removeWire(w);
											
											spliceWire(w, wire).forEach(w1 -> addWire(linkWires, w1));
											Wire clone = new Wire(wire);
											addWire(linkWires, clone);
											
											toRemove.add(clone);
											break;
										} else if(w.overlaps(wire)) {
											LinkWires linkWires = w.getLinkWires();
											removeWire(w);
											
											Pair<Wire, Pair<Wire, Wire>> pairs = spliceOverlappingWire(wire, w);
											elementsToRemove.add(pairs.getKey());
											addWire(linkWires, pairs.getValue().getKey());
											addWire(linkWires, pairs.getValue().getValue());
											
											toRemove.add(pairs.getValue().getKey());
											break;
										}
									}
								}
							}
						}
						
						toRemove.forEach(w -> {
							w.getConnections().forEach(this::removeConnection);
							
							LinkWires linkWires = w.getLinkWires();
							Set<Wire> set = wiresToRemove.containsKey(linkWires)
							                ? wiresToRemove.get(linkWires)
							                : new HashSet<>();
							set.add(w);
							wiresToRemove.put(linkWires, set);
						});
					}
				}
				
				wiresToRemove.forEach((linkWires, wires) -> {
					links.remove(linkWires);
					links.addAll(linkWires.splitWires(wires));
					
					wires.forEach(
						(wire) -> editHistory.addAction(EditAction.REMOVE_WIRE, circuitManager, new Wire(null, wire)));
				});
				
				rejoinWires();
				
				updateBadLinks();
			} finally {
				editHistory.endGroup();
			}
		});
	}
	
	private Set<Wire> spliceWire(Wire toSplice, Wire within) {
		if(!within.isWithin(toSplice)) throw new IllegalArgumentException("toSplice must contain within");
		
		Set<Wire> wires = new HashSet<>();
		
		if(toSplice.isHorizontal()) {
			if(toSplice.getX() < within.getX()) {
				wires.add(new Wire(toSplice.getLinkWires(),
				                   toSplice.getX(), toSplice.getY(), within.getX() - toSplice.getX(), true));
			}
			
			int withinEnd = within.getX() + within.getLength();
			int toSpliceEnd = toSplice.getX() + toSplice.getLength();
			if(withinEnd < toSpliceEnd) {
				wires.add(new Wire(toSplice.getLinkWires(),
				                   withinEnd, toSplice.getY(), toSpliceEnd - withinEnd, true));
			}
		} else {
			if(toSplice.getY() < within.getY()) {
				wires.add(new Wire(toSplice.getLinkWires(),
				                   toSplice.getX(), toSplice.getY(), within.getY() - toSplice.getY(), false));
			}
			
			int withinEnd = within.getY() + within.getLength();
			int toSpliceEnd = toSplice.getY() + toSplice.getLength();
			if(withinEnd < toSpliceEnd) {
				wires.add(new Wire(toSplice.getLinkWires(),
				                   toSplice.getX(), withinEnd, toSpliceEnd - withinEnd, false));
			}
		}
		
		return wires;
	}
	
	// returns (overlap leftover, (toSplice pieces))
	private Pair<Wire, Pair<Wire, Wire>> spliceOverlappingWire(Wire toSplice, Wire overlap) {
		if(!toSplice.overlaps(overlap)) throw new IllegalArgumentException("wires must overlap");
		
		if(toSplice.isHorizontal()) {
			Wire left = toSplice.getX() < overlap.getX() ? toSplice : overlap;
			Wire right = toSplice.getX() < overlap.getX() ? overlap : toSplice;
			
			Wire leftPiece =
				new Wire(
					left.getLinkWires(),
					left.getX(),
					left.getY(),
					right.getX() - left.getX(),
					true);
			Wire midPiece =
				new Wire(right.getLinkWires(),
				         right.getX(),
				         right.getY(),
				         left.getX() + left.getLength() - right.getX(),
				         true);
			Wire rightPiece =
				new Wire(right.getLinkWires(),
				         left.getX() + left.getLength(),
				         left.getY(),
				         right.getX() + right.getLength() - left.getX() - left.getLength(),
				         true);
			
			if(left == toSplice) {
				return new Pair<>(leftPiece, new Pair<>(midPiece, rightPiece));
			} else {
				return new Pair<>(rightPiece, new Pair<>(midPiece, leftPiece));
			}
		} else {
			Wire top = toSplice.getY() < overlap.getY() ? toSplice : overlap;
			Wire bottom = toSplice.getY() < overlap.getY() ? overlap : toSplice;
			
			Wire topPiece =
				new Wire(
					top.getLinkWires(),
					top.getX(), top.getY(),
					bottom.getY() - top.getY(),
					false);
			Wire midPiece =
				new Wire(
					bottom.getLinkWires(),
					bottom.getX(),
					bottom.getY(),
					
					top.getY() + top.getLength() - bottom.getY(),
					false);
			Wire bottomPiece =
				new Wire(
					bottom.getLinkWires(),
					top.getX(),
					top.getY() + top.getLength(),
					bottom.getY() + bottom.getLength() - top.getY() - top.getLength(),
					false);
			
			if(top == toSplice) {
				return new Pair<>(topPiece, new Pair<>(midPiece, bottomPiece));
			} else {
				return new Pair<>(bottomPiece, new Pair<>(midPiece, topPiece));
			}
		}
	}
	
	public synchronized void addWire(int x, int y, int length, boolean horizontal) {
		if(x < 0 || y < 0 || (horizontal && x + length < 0) || (!horizontal && y + length < 0)) {
			throw new SimulationException("Wire cannot go into negative space.");
		}
		
		if(length == 0) {
			throw new SimulationException("Length cannot be 0");
		}
		
		circuit.getSimulator().runSync(() -> {
			try {
				editHistory.beginGroup();
				
				LinkWires linkWires = new LinkWires();
				
				Set<Wire> wiresAdded = new LinkedHashSet<>();
				
				// these are wires that would be split in half
				Map<Wire, Connection> toSplit = new HashMap<>();
				
				{
					Set<Connection> connections = getConnections(x, y);
					if(connections != null) {
						for(Connection connection : connections) {
							handleConnection(connection, linkWires);
							
							GuiElement parent = connection.getParent();
							if(connection instanceof WireConnection) {
								Wire wire = (Wire)parent;
								if(connection != wire.getStartConnection() && connection != wire.getEndConnection()) {
									toSplit.put(wire, connection);
								}
							}
						}
					}
				}
				
				int lastX = x;
				int lastY = y;
				
				int sign = length / Math.abs(length);
				for(int i = sign; Math.abs(i) <= Math.abs(length); i += sign) {
					int xOff = horizontal ? i : 0;
					int yOff = horizontal ? 0 : i;
					Connection currConnection = findConnection(x + xOff, y + yOff);
					
					if(currConnection != null &&
						   (i == length || currConnection instanceof PortConnection ||
							    currConnection == ((Wire)currConnection.getParent()).getStartConnection() ||
							    currConnection == ((Wire)currConnection.getParent()).getEndConnection())) {
						int len = horizontal ? currConnection.getX() - lastX
						                     : currConnection.getY() - lastY;
						Wire wire = new Wire(linkWires, lastX, lastY, len, horizontal);
						Wire surrounding = wireAlreadyExists(wire);
						if(surrounding == null) {
							wiresAdded.add(wire);
						}
						
						Set<Connection> connections = i == length ? getConnections(x + xOff, y + yOff)
						                                          : Collections.singleton(currConnection);
						
						for(Connection connection : connections) {
							GuiElement parent = connection.getParent();
							if(connection instanceof WireConnection) {
								Wire connWire = (Wire)parent;
								if(connection != connWire.getStartConnection() &&
									   connection != connWire.getEndConnection()) {
									toSplit.put((Wire)parent, connection);
								}
							}
							
							handleConnection(connection, linkWires);
						}
						
						lastX = currConnection.getX();
						lastY = currConnection.getY();
					} else if(i == length) {
						int len = horizontal ? x + xOff - lastX
						                     : y + yOff - lastY;
						Wire wire = new Wire(linkWires, lastX, lastY, len, horizontal);
						Wire surrounding = wireAlreadyExists(wire);
						if(surrounding == null) {
							wiresAdded.add(wire);
						}
						
						lastX = x + xOff;
						lastY = y + yOff;
					}
				}
				
				for(Wire wire : wiresAdded) {
					addWire(linkWires, wire);
				}
				
				toSplit.forEach(this::splitWire);
				
				rejoinWires();
				
				updateBadLinks();
			} finally {
				editHistory.endGroup();
			}
		});
	}
	
	private Wire wireAlreadyExists(Wire wire) {
		Set<Connection> connections = connectionsMap.get(new Pair<>(wire.getX(), wire.getY()));
		if(connections == null || connections.isEmpty()) {
			return null;
		}
		
		for(Connection connection : connections) {
			if(connection instanceof WireConnection) {
				Wire existing = (Wire)connection.getParent();
				if(wire.isWithin(existing)) {
					return existing;
				}
			}
		}
		
		return null;
	}
	
	private void splitWire(Wire wire, Connection connection) {
		LinkWires links = wire.getLinkWires();
		removeWire(wire);
		
		int len = connection.getX() == wire.getX() ? connection.getY() - wire.getY()
		                                           : connection.getX() - wire.getX();
		Wire wire1 = new Wire(links, wire.getX(), wire.getY(), len, wire.isHorizontal());
		addWire(links, wire1);
		
		Wire wire2 = new Wire(links, connection.getX(), connection.getY(), wire.getLength() - len,
		                      wire.isHorizontal());
		addWire(links, wire2);
	}
	
	private void addWire(LinkWires linkWires, Wire wire) {
		linkWires.addWire(wire);
		links.add(linkWires);
		wire.getConnections().forEach(this::addConnection);
		
		editHistory.addAction(EditAction.ADD_WIRE, circuitManager, wire);
	}
	
	private void removeWire(Wire wire) {
		wire.getConnections().forEach(this::removeConnection);
		
		LinkWires linkWires = wire.getLinkWires();
		if(linkWires == null) {
			return;
		}
		
		linkWires.removeWire(wire);
		
		editHistory.addAction(EditAction.REMOVE_WIRE, circuitManager, new Wire(null, wire));
	}
	
	// For EditHistory usage, so rejoinWires will only need to be called once.
	private boolean rejoinWiresEnabled = true;
	
	void disableRejoinWires() {
		rejoinWiresEnabled = false;
	}
	
	void enableRejoinWires() {
		rejoinWiresEnabled = true;
		rejoinWires();
	}
	
	private synchronized void rejoinWires() {
		if(!rejoinWiresEnabled) {
			return;
		}
		
		editHistory.disable();
		
		try {
			for(LinkWires linkWires : links) {
				Set<Wire> removed = new HashSet<>();
				ArrayList<Wire> wires = new ArrayList<>(linkWires.getWires());
				for(int i = 0; i < wires.size(); i++) {
					Wire wire = wires.get(i);
					
					if(removed.contains(wire)) continue;
					
					Connection start = wire.getStartConnection();
					Connection end = wire.getEndConnection();
					
					int x = wire.getX();
					int y = wire.getY();
					int length = wire.getLength();
					
					Set<Connection> startConns = getConnections(start.getX(), start.getY());
					if(startConns != null && startConns.size() == 2) {
						List<Wire> startWires = startConns.stream()
						                                  .filter(
							                                  conn -> conn != start && conn instanceof WireConnection)
						                                  .map(conn -> (Wire)conn.getParent())
						                                  .filter(w -> w.isHorizontal() == wire.isHorizontal())
						                                  .collect(Collectors.toList());
						
						if(startWires.size() == 1) {
							Wire startWire = startWires.get(0);
							length += startWire.getLength();
							
							if(startWire.getX() < x) {
								x = startWire.getX();
							}
							if(startWire.getY() < y) {
								y = startWire.getY();
							}
							
							removeWire(startWire);
							removed.add(startWire);
						}
					}
					
					Set<Connection> endConns = getConnections(end.getX(), end.getY());
					if(endConns != null && endConns.size() == 2) {
						List<Wire> endWires = endConns.stream()
						                              .filter(conn -> conn != end && conn instanceof WireConnection)
						                              .map(conn -> (Wire)conn.getParent())
						                              .filter(w -> w.isHorizontal() == wire.isHorizontal())
						                              .collect(Collectors.toList());
						
						if(endWires.size() == 1) {
							Wire endWire = endWires.get(0);
							length += endWire.getLength();
							
							removeWire(endWire);
							removed.add(endWire);
						}
					}
					
					if(length != wire.getLength()) {
						removeWire(wire);
						removed.add(wire);
						Wire newWire = new Wire(linkWires, x, y, length, wire.isHorizontal());
						addWire(linkWires, newWire);
						wires.add(newWire);
					}
				}
			}
		} finally {
			editHistory.enable();
		}
	}
	
	private void removeComponent(ComponentPeer<?> component, boolean removeFromComponentsList) {
		if(!components.contains(component)) {
			return;
		}
		
		for(Connection connection : component.getConnections()) {
			removeConnection(connection);
			
			PortConnection portConnection = (PortConnection)connection;
			LinkWires linkWires = portConnection.getLinkWires();
			if(linkWires != null) {
				linkWires.removePort(portConnection);
				if(linkWires.isEmpty()) {
					linkWires.clear();
					links.remove(linkWires);
				}
			}
		}
		
		if(removeFromComponentsList) {
			components.remove(component);
		}
		
		editHistory.addAction(EditAction.REMOVE_COMPONENT, circuitManager, component);
	}
	
	private void handleConnection(Connection connection, LinkWires linkWires) {
		LinkWires linksToMerge = connection.getLinkWires();
		if(linksToMerge == null) {
			if(connection instanceof PortConnection) {
				linkWires.addPort((PortConnection)connection);
			} else if(connection instanceof WireConnection) {
				linkWires.addWire((Wire)connection.getParent());
			}
		} else if(linkWires != linksToMerge) {
			links.remove(linksToMerge);
			linkWires.merge(linksToMerge);
		}
		
		links.add(linkWires);
	}
	
	public Connection findConnection(int x, int y) {
		Pair<Integer, Integer> pair = new Pair<>(x, y);
		return connectionsMap.containsKey(pair) ? connectionsMap.get(pair).iterator().next() : null;
	}
	
	public Set<Connection> getConnections(int x, int y) {
		Pair<Integer, Integer> pair = new Pair<>(x, y);
		return connectionsMap.getOrDefault(pair, Collections.emptySet());
	}
	
	public void paint(GraphicsContext graphics, LinkWires highlightLinkWires) {
		CircuitState currentState = new CircuitState(this.currentState);
		
		components.forEach(component -> {
			if(moveElements == null || !moveElements.contains(component)) {
				paintComponent(graphics, currentState, component);
			}
		});
		
		for(LinkWires linkWires : links) {
			for(Wire wire : linkWires.getWires()) {
				paintWire(graphics, currentState, wire, linkWires == highlightLinkWires);
			}
		}
		
		if(badLinks != null) {
			for(LinkWires badLink : badLinks) {
				Stream.concat(badLink.getPorts().stream(),
				              badLink.getInvalidPorts().stream()).forEach(port -> {
					graphics.setFill(Color.BLACK);
					graphics.fillText(String.valueOf(port.getPort().getLink().getBitSize()),
					                  port.getScreenX() + 11,
					                  port.getScreenY() + 21);
					
					graphics.setStroke(Color.ORANGE);
					graphics.setFill(Color.ORANGE);
					graphics.strokeOval(port.getScreenX() - 2, port.getScreenY() - 2, 10, 10);
					graphics.fillText(String.valueOf(port.getPort().getLink().getBitSize()),
					                  port.getScreenX() + 10,
					                  port.getScreenY() + 20);
				});
			}
		}
		
		if(moveElements != null) {
			graphics.save();
			graphics.setGlobalAlpha(0.5);
			
			for(GuiElement element : moveElements) {
				if(element instanceof ComponentPeer<?>) {
					paintComponent(graphics, currentState, (ComponentPeer<?>)element);
				} else if(element instanceof Wire) {
					paintWire(graphics, currentState, (Wire)element, false);
				}
			}
			
			MoveComputeResult result = this.moveResult;
			if(result != null) {
				graphics.setFill(Color.RED);
				result.wiresToRemove.forEach(wire -> wire.paint(graphics));
				
				graphics.setFill(Color.BLACK);
				result.wiresToAdd.forEach(wire -> wire.paint(graphics));
			}
			
			graphics.restore();
		}
	}
	
	private void paintComponent(GraphicsContext graphics, CircuitState state, ComponentPeer<?> component) {
		graphics.save();
		component.paint(graphics, state);
		graphics.restore();
		
		for(PortConnection connection : component.getConnections()) {
			connection.paint(graphics, state);
		}
	}
	
	private void paintWire(GraphicsContext graphics, CircuitState state, Wire wire, boolean highlight) {
		graphics.save();
		wire.paint(graphics, state, highlight ? 4.0 : 2.0);
		graphics.restore();
		
		Connection startConn = wire.getStartConnection();
		if(getConnections(startConn.getX(), startConn.getY()).size() > 2) {
			startConn.paint(graphics, state);
		}
		Connection endConn = wire.getStartConnection();
		if(getConnections(endConn.getX(), endConn.getY()).size() > 2) {
			endConn.paint(graphics, state);
		}
	}
	
	private synchronized void addConnection(Connection connection) {
		Pair<Integer, Integer> pair = new Pair<>(connection.getX(), connection.getY());
		Set<Connection> set = connectionsMap.containsKey(pair) ? connectionsMap.get(pair) : new HashSet<>();
		set.add(connection);
		connectionsMap.put(pair, set);
	}
	
	private synchronized void removeConnection(Connection connection) {
		Pair<Integer, Integer> pair = new Pair<>(connection.getX(), connection.getY());
		if(!connectionsMap.containsKey(pair)) {
			return;
		}
		Set<Connection> set = connectionsMap.get(pair);
		set.remove(connection);
		if(set.isEmpty()) {
			connectionsMap.remove(pair);
		}
	}
}
