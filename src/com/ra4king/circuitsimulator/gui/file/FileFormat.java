package com.ra4king.circuitsimulator.gui.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.ra4king.circuitsimulator.gui.Properties;

/**
 * @author Roi Atalla
 */
public class FileFormat {
	private static String saveScript;
	private static String loadScript;
	
	public static final double VERSION = 1.0;
	
	static {
		saveScript = readFile("save.js");
		loadScript = readFile("load.js");
	}
	
	private static String readFile(Reader reader) {
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
	
	private static String readFile(File file) {
		try {
			return readFile(new FileReader(file));
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private static String readFile(String file) {
		return readFile(new InputStreamReader(FileFormat.class.getResourceAsStream(file)));
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
		public final String className;
		public final int x;
		public final int y;
		public final Properties properties;
		
		public ComponentInfo(String className, int x, int y, Properties properties) {
			this.className = className;
			this.x = x;
			this.y = y;
			this.properties = properties;
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
	
	public static void save(File file, List<CircuitInfo> circuits) {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		
		Bindings bindings = new SimpleBindings();
		bindings.put("version", VERSION);
		bindings.put("circuits", circuits);
		
		String json;
		try {
			json = (String)engine.eval(saveScript, bindings);
		} catch(Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
		
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(json + "\n");
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	public static List<CircuitInfo> load(File file) {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		
		Bindings bindings = new SimpleBindings();
		bindings.put("version", VERSION);
		bindings.put("file", readFile(file));
		
		try {
			@SuppressWarnings("unchecked")
			List<CircuitInfo> circuits = (List<CircuitInfo>)engine.eval(loadScript, bindings);
			return circuits;
		} catch(Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
	}
}
