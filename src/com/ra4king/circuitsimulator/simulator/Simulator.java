package com.ra4king.circuitsimulator.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ra4king.circuitsimulator.simulator.utils.Pair;

/**
 * @author Roi Atalla
 */
public class Simulator {
	private Set<Circuit> circuits;
	private List<Pair<CircuitState, Port>> linksToUpdate, temp, shortCircuited;
	private ShortCircuitException lastShortCircuit;
	private final Set<List<Pair<CircuitState, Port>>> history;
	
	public Simulator() {
		circuits = new HashSet<>();
		linksToUpdate = new ArrayList<>();
		temp = new ArrayList<>();
		shortCircuited = new ArrayList<>();
		history = new HashSet<>();
	}
	
	public void addCircuit(Circuit circuit) {
		circuits.add(circuit);
	}
	
	public synchronized void valueChanged(CircuitState state, Port port) {
		linksToUpdate.add(new Pair<>(state, port));
	}
	
	public synchronized void step() {
		List<Pair<CircuitState, Port>> tmp = linksToUpdate;
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
