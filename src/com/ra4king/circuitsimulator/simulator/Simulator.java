package com.ra4king.circuitsimulator.simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ra4king.circuitsimulator.simulator.Port.Link;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class Simulator {
	private Set<Circuit> circuits;
	private Collection<Pair<CircuitState, Link>> linksToUpdate, temp, shortCircuited;
	private ShortCircuitException lastShortCircuit;
	private final Set<Collection<Pair<CircuitState, Link>>> history;
	
	public Simulator() {
		circuits = new HashSet<>();
		linksToUpdate = new ConcurrentLinkedQueue<>();
		temp = new ConcurrentLinkedQueue<>();
		shortCircuited = new ArrayList<>();
		history = new HashSet<>();
	}
	
	public synchronized void reset() {
		circuits.stream().flatMap(circuit -> circuit.getCircuitStates().stream()).forEach(CircuitState::reset);
	}
	
	public Set<Circuit> getCircuits() {
		return Collections.unmodifiableSet(circuits);
	}
	
	public synchronized void addCircuit(Circuit circuit) {
		circuits.add(circuit);
	}
	
	public synchronized void removeCircuit(Circuit circuit) {
		circuits.remove(circuit);
	}
	
	public void valueChanged(CircuitState state, Port port) {
		valueChanged(state, port.getLink());
	}
	
	public void valueChanged(CircuitState state, Link link) {
		linksToUpdate.add(new Pair<>(state, link));
	}
	
	public synchronized void step() {
		Collection<Pair<CircuitState, Link>> tmp = linksToUpdate;
		linksToUpdate = temp;
		temp = tmp;
		
		linksToUpdate.clear();
		shortCircuited.clear();
		
		temp.forEach(pair -> {
			try {
				pair.getKey().propagateSignal(pair.getValue());
			} catch(ShortCircuitException exc) {
				shortCircuited.add(pair);
				lastShortCircuit = exc;
			}
		});
		
		if(shortCircuited.size() > 0 && linksToUpdate.size() == 0) {
			throw lastShortCircuit;
		}
		
		linksToUpdate.addAll(shortCircuited);
	}
	
	public synchronized void stepAll() {
		history.clear();
		
		do {
			if(history.contains(linksToUpdate)) {
				throw new IllegalStateException("Oscillation apparent.");
			}
			
			history.add(new ArrayList<>(linksToUpdate));
			step();
		} while(!linksToUpdate.isEmpty());
	}
}
