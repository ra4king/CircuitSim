package com.ra4king.circuitsim.simulator;

/**
 * @author Roi Atalla
 */
public class SimulationException extends RuntimeException {
	public SimulationException(String message) {
		super(message);
	}
	
	public SimulationException(String message, Throwable cause) {
		super(message, cause);
	}
}
