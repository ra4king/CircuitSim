package com.ra4king.circuitsimulator;

import java.util.HashMap;

/**
 * @author Roi Atalla
 */
public class Port {
	private final Simulator simulator;
	private final Component component;
	private final int port;
	private Link link;
	
	public Port(Simulator simulator, Component component, int port, int bitSize) {
		this.simulator = simulator;
		this.component = component;
		this.port = port;
		
		link = new Link(new HashMap<>(), new WireValue(bitSize));
		link.participants.put(this, new WireValue(bitSize));
	}
	
	public WireValue getWireValue() {
		return link.value;
	}
	
	public Port linkPort(Port port) {
		link.linkPort(port);
		propagateSignal();
		return this;
	}
	
	public Port unlinkPort(Port port) {
		link.unlinkPort(port);
		propagateSignal();
		return this;
	}
	
	public void propagateSignal() {
		WireValue newValue = new WireValue(link.value.getBitSize());
		link.participants.entrySet().stream().forEach(entry -> {
			Utils.ensureCompatible(Port.this, newValue, entry.getValue());
			newValue.merge(entry.getValue());
		});
		
		if(!newValue.equals(link.value)) {
			link.value.set(newValue);
			link.participants.keySet().stream().filter(port -> !port.equals(this))
					.forEach(port -> port.component.valueChanged(link.value, port.port));
		}
	}
	
	@Override
	public int hashCode() {
		return component.hashCode() ^ port;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Port) {
			Port port = (Port)other;
			return port.component == this.component && port.port == this.port;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "Link(" + component + "[" + port + "])";
	}
	
	public synchronized void pushValue(WireValue value) {
		Utils.ensureBitSize(this, value, link.value.getBitSize());
		
		WireValue currentValue = link.participants.get(this);
		if(!value.equals(currentValue)) {
			currentValue.set(value);
			simulator.valueChanged(this);
		}
	}
	
	public static class Link {
		private HashMap<Port, WireValue> participants;
		private WireValue value;
		
		Link(HashMap<Port, WireValue> participants, WireValue value) {
			this.participants = participants;
			this.value = value;
		}
		
		private Link merge(Link other) {
			if(this == other) throw new IllegalArgumentException("Link cannot merge with itself.");
			
			Utils.ensureCompatible(this, value, other.value);
			value.merge(other.value);
			participants.putAll(other.participants);
			return this;
		}
		
		public Link linkPort(Port port) {
			if(participants.containsKey(port)) return this;
			
			boolean propagateSignal = !port.link.value.equals(value);
			HashMap<Port, WireValue> portParticipants = new HashMap<>(port.link.participants);
			
			port.link = merge(port.link);
			
			if(propagateSignal) {
				portParticipants.keySet().stream().forEach(p -> p.component.valueChanged(value, p.port));
			}
			
			return this;
		}
		
		public Link unlinkPort(Port port) {
			if(!participants.containsKey(port)) return this;
			
			WireValue lastWireValue = participants.remove(port);
			port.link = new Link(new HashMap<>(), lastWireValue);
			port.link.participants.put(port, lastWireValue);
			
			return this;
		}
	}
}
