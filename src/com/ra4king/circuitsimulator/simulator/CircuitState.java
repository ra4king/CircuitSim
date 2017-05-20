package com.ra4king.circuitsimulator.simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

public class CircuitState {
	private final Circuit circuit;
	private final HashMap<Component, Object> componentProperties;
	private final HashMap<Link, LinkState> linkStates;
	
	public CircuitState(Circuit circuit) {
		if(circuit == null) {
			throw new NullPointerException("Circuit cannot be null.");
		}
		
		this.circuit = circuit;
		componentProperties = new HashMap<>();
		linkStates = new HashMap<>();
		
		circuit.getCircuitStates().add(this);
	}
	
	public Circuit getCircuit() {
		return circuit;
	}
	
	public HashMap<Component, Object> getComponentProperties() {
		return componentProperties;
	}
	
	public Object getComponentProperty(Component component) {
		return componentProperties.get(component);
	}
	
	public void putComponentProperty(Component component, Object property) {
		componentProperties.put(component, property);
	}
	
	public Object removeComponentProperty(Component component) {
		return componentProperties.remove(component);
	}
	
	public WireValue getMergedValue(Link link) {
		synchronized(circuit.getSimulator()) {
			return get(link).getMergedValue();
		}
	}
	
	public WireValue getLastReceived(Port port) {
		synchronized(circuit.getSimulator()) {
			return new WireValue(get(port.getLink()).getLastReceived(port));
		}
	}
	
	public WireValue getLastPushedValue(Port port) {
		synchronized(circuit.getSimulator()) {
			return new WireValue(get(port.getLink()).getLastPushed(port));
		}
	}
	
	public boolean isShortCircuited(Link link) {
		synchronized(circuit.getSimulator()) {
			return get(link).isShortCircuit();
		}
	}
	
	private LinkState get(Link link) {
		if(!linkStates.containsKey(link)) {
			if(link.getCircuit() == null) {
				throw new IllegalArgumentException("Link has no circuit!");
			}
			
			if(link.getCircuit() != circuit) {
				throw new IllegalArgumentException("Link not from this circuit.");
			}
			
			LinkState linkState = new LinkState(link);
			linkStates.put(link, linkState);
			return linkState;
		}
		
		return linkStates.get(link);
	}
	
	void link(Link link1, Link link2) {
		synchronized(circuit.getSimulator()) {
			get(link1).link(get(link2));
		}
	}
	
	void unlink(Link link, Port port) {
		synchronized(circuit.getSimulator()) {
			get(link).unlink(port);
		}
	}
	
	void propagateSignal(Link link) {
		synchronized(circuit.getSimulator()) {
			LinkState linkState = get(link);
			
			linkState.participants.forEach((port, info) -> {
				WireValue lastMerged = info.lastMerged;
				WireValue lastPushed = info.lastPushed;
				
				if(!lastMerged.equals(lastPushed)) {
					lastMerged.set(lastPushed);
				}
			});
			
			linkState.propagate();
		}
	}
	
	public void pushValue(Port port, WireValue value) {
		boolean changed;
		synchronized(circuit.getSimulator()) {
			LinkState linkState = get(port.getLink());
			
			Utils.ensureBitSize(this, value, linkState.link.getBitSize());
			
			WireValue lastPushed = linkState.getLastPushed(port);
			changed = !value.equals(lastPushed);
			lastPushed.set(value);
		}
		
		if(changed) {
			circuit.getSimulator().valueChanged(this, port);
		}
	}
	
	public void ensureUnlinked(Component component) {
		for(int i = 0; i < component.getNumPorts(); i++) {
			Port port = component.getPort(i);
			Link link = port.getLink();
			if(link != null && linkStates.containsKey(link) && linkStates.get(link).participants.size() > 1) {
				throw new RuntimeException("Must unlink port before removing it.");
			}
		}
	}
	
	private class LinkState {
		private final Link link;
		private final HashMap<Port, PortStateInfo> participants;
		
		private class PortStateInfo {
			private WireValue lastPushed;
			private WireValue lastMerged;
			private WireValue lastReceived;
			
			PortStateInfo() {
				this(new WireValue(link.getBitSize()),
				     new WireValue(link.getBitSize()),
				     new WireValue(link.getBitSize()));
			}
			
			PortStateInfo(WireValue lastPushed, WireValue lastMerged, WireValue lastReceived) {
				this.lastPushed = lastPushed;
				this.lastMerged = lastMerged;
				this.lastReceived = lastReceived;
			}
		}
		
		LinkState(Link link) {
			this.link = link;
			
			participants = new HashMap<>();
			
			link.getParticipants().forEach(port -> participants.put(port, new PortStateInfo()));
		}
		
		WireValue getLastPushed(Port port) {
			return participants.get(port).lastPushed;
		}
		
		WireValue getLastMerged(Port port) {
			return participants.get(port).lastMerged;
		}
		
		WireValue getLastReceived(Port port) {
			return participants.get(port).lastReceived;
		}
		
		WireValue getIncomingValue(Port port) {
			WireValue newValue = new WireValue(link.getBitSize());
			participants.keySet().stream()
			            .filter(p -> p != port)
			            .map(participants::get)
			            .forEach(info -> {
				            Utils.ensureCompatible(link, newValue, info.lastMerged);
				            newValue.merge(info.lastMerged);
			            });
			return newValue;
		}
		
		WireValue getMergedValue() {
			WireValue newValue = new WireValue(link.getBitSize());
			participants.values().forEach(info -> {
				Utils.ensureCompatible(link, newValue, info.lastMerged);
				newValue.merge(info.lastMerged);
			});
			return newValue;
		}
		
		boolean isShortCircuit() {
			try {
				getMergedValue();
				return false;
			} catch(ShortCircuitException exc) {
				return true;
			} catch(Throwable t) {
				return false;
			}
		}
		
		void propagate() {
			getMergedValue(); // check for short circuit before propagating
			
			Set<Port> toNotify = new HashSet<>();
			
			for(Port participantPort : participants.keySet()) {
				WireValue incomingValue = getIncomingValue(participantPort);
				WireValue lastReceived = getLastReceived(participantPort);
				if(!lastReceived.equals(incomingValue)) {
					lastReceived.set(incomingValue);
					toNotify.add(participantPort);
				}
			}
			
			for(Port participantPort : toNotify) {
				participantPort.component.valueChanged(CircuitState.this,
				                                       getLastReceived(participantPort),
				                                       participantPort.portIndex);
			}
		}
		
		void link(LinkState other) {
			if(this == other) return;
			
			participants.putAll(other.participants);
			
			participants.forEach((port, info) -> info.lastMerged.setAllBits(State.X));
			
			linkStates.remove(other.link);
			
			getCircuit().getSimulator().valueChanged(CircuitState.this, link);
		}
		
		void unlink(Port port) {
			if(!participants.containsKey(port)) return;
			
			PortStateInfo info = participants.remove(port);
			get(port.getLink()).participants.put(port, new PortStateInfo(info.lastPushed,
			                                                             new WireValue(info.lastPushed),
			                                                             new WireValue(link.getBitSize())));
			
			WireValue newValue = new WireValue(link.getBitSize());
			if(!info.lastReceived.equals(newValue)) {
				info.lastReceived.set(newValue);
				port.component.valueChanged(CircuitState.this, newValue, port.portIndex);
			}
			
			if(participants.isEmpty()) {
				linkStates.remove(link);
			} else {
				getCircuit().getSimulator().valueChanged(CircuitState.this, link);
			}
		}
	}
}
