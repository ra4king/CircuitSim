package com.ra4king.circuitsimulator.simulator;

import com.ra4king.circuitsimulator.simulator.Port.Link;

/**
 * @author Roi Atalla
 */
public class ShortCircuitException extends RuntimeException {
	private Link link;
	
	public ShortCircuitException(String message, Link link) {
		super(message);
		this.link = link;
	}
	
	public Link getLink() {
		return link;
	}
}
