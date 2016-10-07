package com.ra4king.circuitsimulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roi Atalla
 */
public class Simulator {
	private List<Port> linksToUpdate, temp;
	private Set<List<Port>> history;
	
	public Simulator() {
		linksToUpdate = new ArrayList<>();
		temp = new ArrayList<>();
		history = new HashSet<>();
	}
	
	public synchronized void valueChanged(Port port) {
		if(!linksToUpdate.contains(port)) {
			linksToUpdate.add(port);
		}
	}
	
	public synchronized void step() {
		List<Port> tmp = linksToUpdate;
		linksToUpdate = temp;
		temp = tmp;
		
		temp.forEach(Port::propagateSignal);
		temp.clear();
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
