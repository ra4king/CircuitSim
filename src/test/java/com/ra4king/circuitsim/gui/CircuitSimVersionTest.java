package com.ra4king.circuitsim.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CircuitSimVersionTest {
	@Test
	public void versionDiff_same() {
		assertEquals(0, new CircuitSimVersion("1.2.3").compareTo(new CircuitSimVersion("1.2.3")));
		assertEquals(0, new CircuitSimVersion("1.2.3b").compareTo(new CircuitSimVersion("1.2.3b")));
	}
	
	@Test
	public void versionDiff_older() {
		CircuitSimVersion older = new CircuitSimVersion("1.2.3");
		assertEquals(-1, older.compareTo(new CircuitSimVersion("1.2.4")));
		assertEquals(-1, older.compareTo(new CircuitSimVersion("1.3.0")));
		assertEquals(-1, older.compareTo(new CircuitSimVersion("2.0.0")));
		assertEquals(-1, older.compareTo(new CircuitSimVersion("1.2.11")));
		assertEquals(-1, older.compareTo(new CircuitSimVersion("1.11.3")));
		assertEquals(-1, older.compareTo(new CircuitSimVersion("11.2.3")));
		assertEquals(-1, older.compareTo(new CircuitSimVersion("1.2.4b")));
		
		assertEquals(-1, new CircuitSimVersion("1.2.3b").compareTo(new CircuitSimVersion("11.2.3")));
	}
	
	@Test
	public void versionDiff_newer() {
		CircuitSimVersion newer = new CircuitSimVersion("1.2.3");
		assertEquals(1, newer.compareTo(new CircuitSimVersion("1.2.2")));
		assertEquals(1, newer.compareTo(new CircuitSimVersion("1.1.5")));
		assertEquals(1, newer.compareTo(new CircuitSimVersion("0.9.9")));
		assertEquals(1, newer.compareTo(new CircuitSimVersion("1.2.3b")));
	}
}
