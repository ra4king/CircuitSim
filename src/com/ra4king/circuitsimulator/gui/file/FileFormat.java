package com.ra4king.circuitsimulator.gui.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ra4king.circuitsimulator.gui.Properties;

/**
 * @author Roi Atalla
 */
public class FileFormat {
	private static final Gson GSON;
	
	public static final double VERSION = 1.0;
	
	static {
		GSON = new GsonBuilder().create();
	}
	
	public static String readFile(Reader reader) {
		String string = "";
		try(BufferedReader bufReader = new BufferedReader(reader)) {
			String line;
			while((line = bufReader.readLine()) != null) {
				string += line + "\n";
			}
			
			return string;
		} catch(Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
	}
	
	public static String readFile(File file) {
		try {
			return readFile(new FileReader(file));
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	public static void writeFile(File file, String contents) throws Exception {
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(contents);
			writer.write('\n');
		}
	}
	
	public static class CircuitFile {
		private final double version = VERSION;
		
		public final int globalBitSize;
		public final int clockSpeed;
		public final List<CircuitInfo> circuits;
		
		public CircuitFile(int globalBitSize, int clockSpeed, List<CircuitInfo> circuits) {
			this.globalBitSize = globalBitSize;
			this.clockSpeed = clockSpeed;
			this.circuits = circuits;
		}
	}
	
	public static class CircuitInfo {
		public final String name;
		public final Set<ComponentInfo> components;
		public final Set<WireInfo> wires;
		
		public CircuitInfo(String name, Set<ComponentInfo> components, Set<WireInfo> wires) {
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
		
		public ComponentInfo(String name, int x, int y, Properties properties) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.properties = new HashMap<>();
			properties.forEach(prop -> this.properties.put(prop.name, prop.getStringValue()));
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
	}
	
	public static void save(File file, CircuitFile circuitFile) throws Exception {
		writeFile(file, stringify(circuitFile));
	}
	
	public static String stringify(CircuitFile circuitFile) throws Exception {
		return GSON.toJson(circuitFile);
	}
	
	public static CircuitFile load(File file) throws Exception {
		return parse(readFile(file));
	}
	
	public static CircuitFile parse(String contents) throws Exception {
		return GSON.fromJson(contents, CircuitFile.class);
	}
}
