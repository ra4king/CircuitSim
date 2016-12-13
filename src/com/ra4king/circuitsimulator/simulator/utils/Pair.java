package com.ra4king.circuitsimulator.simulator.utils;

/**
 * @author Roi Atalla
 */
public class Pair<A, B> {
	public final A first;
	public final B second;
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Pair) {
			Pair pair = (Pair)other;
			return (pair.first == this.first || (pair.first != null && pair.first.equals(this.first)))
					       && (pair.second == this.second || (pair.second != null && pair.second.equals(this.second)));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return first.hashCode() ^ second.hashCode();
	}
	
	@Override
	public String toString() {
		return "Pair(" + first + ", " + second + ")";
	}
}
