package com.ra4king.circuitsim.gui.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.Properties;

/**
 * @author Roi Atalla
 */
public class FileFormat {
	private static final Gson GSON;
	
	static {
		GSON = new GsonBuilder().setPrettyPrinting().create();
	}
	
	public static String readFile(Reader reader) throws IOException {
		StringBuilder string = new StringBuilder();
		try(BufferedReader bufReader = new BufferedReader(reader)) {
			String line;
			while((line = bufReader.readLine()) != null) {
				string.append(line).append("\n");
			}
			
			return string.toString();
		}
	}
	
	public static String readFile(File file) throws IOException {
		return readFile(new FileReader(file));
	}
	
	public static void writeFile(File file, String contents) throws IOException {
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(contents);
			writer.write('\n');
		}
	}
	
	public static class CircuitFile {
		private final String version = CircuitSim.VERSION;
		
		public final int globalBitSize;
		public final int clockSpeed;
		public final List<String> libraryPaths;
		public final List<CircuitInfo> circuits;
		
		public CircuitFile(int globalBitSize, int clockSpeed, List<String> libraryPaths, List<CircuitInfo> circuits) {
			this.globalBitSize = globalBitSize;
			this.clockSpeed = clockSpeed;
			this.libraryPaths = libraryPaths;
			this.circuits = circuits;
		}
	}
	
	public static class CircuitInfo {
		public final String name;
		public final List<ComponentInfo> components;
		public final List<WireInfo> wires;
		
		public CircuitInfo(String name, List<ComponentInfo> components, List<WireInfo> wires) {
			this.name = name;
			this.components = components;
			this.wires = wires;
		}
	}
	
	public static class ComponentInfo {
		public final String name;
		public final int x;
		public final int y;
		public final Map<String, String> properties;
		
		public ComponentInfo(String name, int x, int y, Map<String, String> properties) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.properties = properties;
		}
		
		public ComponentInfo(String name, int x, int y, Properties properties) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.properties = new HashMap<>();
			properties.forEach(prop -> this.properties.put(prop.name, prop.getStringValue()));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(name, x, y, properties);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof ComponentInfo) {
				ComponentInfo otherComp = (ComponentInfo)other;
				return name.equals(otherComp.name) && x == otherComp.x
					       && y == otherComp.y && properties.equals(otherComp.properties);
			}
			
			return false;
		}
	}
	
	public static class WireInfo {
		public final int x;
		public final int y;
		public final int length;
		public final boolean isHorizontal;
		
		public WireInfo(int x, int y, int length, boolean isHorizontal) {
			this.x = x;
			this.y = y;
			this.length = length;
			this.isHorizontal = isHorizontal;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(x, y, length, isHorizontal);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof WireInfo) {
				WireInfo otherWire = (WireInfo)other;
				return x == otherWire.x && y == otherWire.y
					       && length == otherWire.length && isHorizontal == otherWire.isHorizontal;
			}
			
			return false;
		}
	}
	
	public static void save(File file, CircuitFile circuitFile) throws IOException {
		writeFile(file, stringify(circuitFile));
	}
	
	public static String stringify(CircuitFile circuitFile) {
		return GSON.toJson(circuitFile);
	}
	
	public static CircuitFile load(File file) throws IOException {
		return parse(readFile(file));
	}
	
	public static CircuitFile parse(String contents) {
		return GSON.fromJson(contents, CircuitFile.class);
	}
}
