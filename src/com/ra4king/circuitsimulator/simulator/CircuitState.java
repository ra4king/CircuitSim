package com.ra4king.circuitsimulator.simulator;

import java.util.HashMap;

import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.utils.Utils;

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
	
	public Object getComponentProperty(Component component) {
		return componentProperties.get(component);
	}
	
	public void putComponentProperty(Component component, Object property) {
		componentProperties.put(component, property);
	}
	
	public void removeComponentProperty(Component component) {
		componentProperties.remove(component);
	}
	
	public WireValue getValue(Port port) {
		return getValue(port.getLink());
	}
	
	public WireValue getValue(Link link) {
		return get(link).getLastPropagatedValue();
	}
	
	public WireValue getMergedValue(Port port) {
		return getMergedValue(port.getLink());
	}
	
	public WireValue getMergedValue(Link link) {
		return get(link).getMergedValue();
	}
	
	public WireValue getLastPushedValue(Port port) {
		return get(port.getLink()).getParticipantValues().get(port);
	}
	
	public boolean isShortCircuited(Link link) {
		return get(link).isShortCircuit();
	}
	
	private LinkState get(Link link) {
		if(!linkStates.containsKey(link)) {
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
		get(link1).link(get(link2));
	}
	
	void unlink(Link link, Port port) {
		get(link).unlink(port);
	}
	
	void propagateSignal(Port port) {
		LinkState linkState = get(port.getLink());
		
		WireValue newValue = linkState.getMergedValue();
		if(!newValue.equals(linkState.value)) {
			linkState.getLastPropagatedValue().set(newValue);
			linkState.getParticipantValues().keySet().stream().filter(participantPort -> !participantPort.equals(port))
					.forEach(participantPort -> participantPort.component.valueChanged(this, linkState.value, participantPort.portIndex));
		}
	}
	
	public synchronized void pushValue(Port port, WireValue value) {
		LinkState linkState = get(port.getLink());
		
		Utils.ensureBitSize(this, value, linkState.value.getBitSize());
		
		WireValue currentValue = linkState.participantValues.get(port);
		boolean changed = !value.equals(currentValue);
		currentValue.set(value);
		if(changed) {
			circuit.getSimulator().valueChanged(this, port);
		}
	}
	
	private class LinkState {
		private final Link link;
		private final HashMap<Port, WireValue> participantValues;
		private final WireValue value;
		
		LinkState(Link link) {
			this.link = link;
			
			participantValues = new HashMap<>();
			value = new WireValue(link.getBitSize());
			
			link.getParticipants().forEach(port -> participantValues.put(port, new WireValue(link.getBitSize())));
		}
		
		WireValue getLastPropagatedValue() {
			return value;
		}
		
		WireValue getMergedValue() {
			WireValue newValue = new WireValue(link.getBitSize());
			participantValues.entrySet().forEach(entry -> {
				Utils.ensureCompatible(link, newValue, entry.getValue());
				newValue.merge(entry.getValue());
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
		
		HashMap<Port, WireValue> getParticipantValues() {
			return participantValues;
		}
		
		void link(LinkState other) {
			if(this == other) return;
			
			//Utils.ensureCompatible(link, value, other.value);
			
			if(value.isCompatible(other.value)) {
				WireValue newValue = new WireValue(value);
				newValue.merge(other.value);
				
				if(!newValue.equals(value)) {
					value.set(newValue);
					participantValues.keySet().stream()
							.forEach(port -> port.component.valueChanged(CircuitState.this, newValue, port.portIndex));
				}
				
				if(!newValue.equals(other.value)) {
					other.value.set(newValue);
					other.participantValues.keySet().stream()
							.forEach(port -> port.component.valueChanged(CircuitState.this, newValue, port.portIndex));
				}
			}
			
			participantValues.putAll(other.participantValues);
			linkStates.remove(other.link);
		}
		
		void unlink(Port port) {
			if(!participantValues.containsKey(port)) return;
			
			WireValue value = participantValues.remove(port);
			get(port.getLink()).participantValues.put(port, value);
			get(port.getLink()).value.set(value);
			
			if(!this.value.equals(value)) {
				port.component.valueChanged(CircuitState.this, value, port.portIndex);
			}
			
			if(!isShortCircuit()) {
				WireValue newValue = getMergedValue();
				if(!newValue.equals(this.value)) {
					this.value.set(newValue);
					participantValues.keySet().stream()
							.forEach(port1 -> port1.component.valueChanged(CircuitState.this, newValue, port1.portIndex));
				}
			}
		}
	}
}
