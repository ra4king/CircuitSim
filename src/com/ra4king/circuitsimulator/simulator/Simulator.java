package com.ra4king.circuitsimulator.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.utils.Pair;

/**
 * @author Roi Atalla
 */
public class Simulator {
	private Set<Circuit> circuits;
	private List<Pair<CircuitState, Link>> linksToUpdate, temp, shortCircuited;
	private ShortCircuitException lastShortCircuit;
	private final Set<List<Pair<CircuitState, Link>>> history;
	
	public Simulator() {
		circuits = new HashSet<>();
		linksToUpdate = new ArrayList<>();
		temp = new ArrayList<>();
		shortCircuited = new ArrayList<>();
		history = new HashSet<>();
	}
	
	public synchronized void addCircuit(Circuit circuit) {
		circuits.add(circuit);
	}
	
	public synchronized void valueChanged(CircuitState state, Port port) {
		valueChanged(state, port.getLink());
	}
	
	public synchronized void valueChanged(CircuitState state, Link link) {
		if(link.getParticipants().isEmpty()) {
			System.out.println("What the fuck are you doing mate?");
		}
		
		linksToUpdate.add(new Pair<>(state, link));
	}
	
	public synchronized void step() {
		List<Pair<CircuitState, Link>> tmp = linksToUpdate;
		linksToUpdate = temp;
		temp = tmp;
		
		linksToUpdate.clear();
		shortCircuited.clear();
		
		temp.forEach(pair -> {
			try {
				pair.first.propagateSignal(pair.second);
			}
			catch(ShortCircuitException exc) {
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
		history.add(new ArrayList<>(linksToUpdate));
		step();
		while(!linksToUpdate.isEmpty()) {
			history.add(new ArrayList<>(linksToUpdate));
			step();
			
			if(history.contains(linksToUpdate)) {
				throw new IllegalStateException("Oscillation apparent.");
			}
		}
		
		history.clear();
	}
}
