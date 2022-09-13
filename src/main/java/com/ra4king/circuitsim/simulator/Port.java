package com.ra4king.circuitsim.simulator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Roi Atalla
 */
public class Port {
	private final Component component;
	private final int portIndex;
	private Link link;
	
	public Port(Component component, int portIndex, int bitSize) {
		this.component = component;
		this.portIndex = portIndex;
		
		link = new Link(bitSize);
		link.participants.add(this);
	}
	
	public Component getComponent() {
		return component;
	}
	
	public int getPortIndex() {
		return portIndex;
	}
	
	public Link getLink() {
		return link;
	}
	
	public Port linkPort(Port port) {
		link.linkPort(port);
		return this;
	}
	
	public Port unlinkPort(Port port) {
		link.unlinkPort(port);
		return this;
	}
	
	@Override
	public String toString() {
		return "Port(" + component + "[" + portIndex + "])";
	}
	
	public static class Link {
		private final Set<Port> participants;
		private final int bitSize;
		
		public Link(int bitSize) {
			this.participants = new HashSet<>();
			this.bitSize = bitSize;
		}
		
		public Circuit getCircuit() {
			return participants
				.stream()
				.map(port -> port.component.getCircuit())
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
		}
		
		public int getBitSize() {
			return bitSize;
		}
		
		public Set<Port> getParticipants() {
			return participants;
		}
		
		public Link linkPort(Port port) {
			if (participants.contains(port)) {
				return this;
			}
			
			Circuit circuit = getCircuit();
			
			if (circuit == null) {
				throw new IllegalStateException("Link does not belong to a circuit.");
			}
			
			if (port.getLink().getCircuit() == null) {
				throw new IllegalStateException("Port does not belong to a circuit.");
			}
			
			if (port.getLink().getCircuit() != circuit) {
				throw new IllegalArgumentException("Links belong to different circuits.");
			}
			
			if (port.getLink().bitSize != bitSize) {
				throw new IllegalArgumentException("Links have different bit sizes.");
			}
			
			circuit.forEachState(state -> state.link(this, port.getLink()));
			
			Set<Port> portParticipants = port.getLink().participants;
			participants.addAll(portParticipants);
			
			for (Port p : portParticipants) {
				p.link = this;
			}
			
			return this;
		}
		
		public Link unlinkPort(Port port) {
			if (!participants.contains(port)) {
				return this;
			}
			
			if (participants.size() == 1) {
				return this;
			}
			
			Circuit circuit = getCircuit();
			
			participants.remove(port);
			Link link = new Link(bitSize);
			link.participants.add(port);
			port.link = link;
			
			circuit.forEachState(state -> state.unlink(this, port));
			
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("Link[");
			participants.forEach(port -> builder.append(port).append(","));
			return builder.deleteCharAt(builder.length() - 1).append("]").toString();
		}
	}
}
