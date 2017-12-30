package com.ra4king.circuitsim.gui.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ra4king.circuitsim.gui.Properties;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class FileFormat {
	private static final Gson GSON;
	
	public static final String VERSION = "1.5.3";
	
	static {
		GSON = new GsonBuilder().setPrettyPrinting().create();
	}
	
	public static String readFile(Reader reader) {
		StringBuilder string = new StringBuilder();
		try(BufferedReader bufReader = new BufferedReader(reader)) {
			String line;
			while((line = bufReader.readLine()) != null) {
				string.append(line).append("\n");
			}
			
			return string.toString();
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
		private final String version = VERSION;
		
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
	
	public static CircuitFile loadLogisim(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		
		List<CircuitInfo> circuits = new ArrayList<>();
		
		NodeList circuitList = document.getElementsByTagName("circuit");
		for(int i = 0; i < circuitList.getLength(); i++) {
			Node item = circuitList.item(i);
			if(item.getNodeType() == Node.ELEMENT_NODE) {
				Element circuit = (Element)item;
				String name = circuit.getAttribute("name");
				
				System.out.println(name);
				
				Set<ComponentInfo> components = new HashSet<>();
				Set<WireInfo> wires = new HashSet<>();
				
				NodeList compList = circuit.getElementsByTagName("comp");
				for(int j = 0; j < compList.getLength(); j++) {
					Node comp = compList.item(j);
					if(comp.getNodeType() == Node.ELEMENT_NODE) {
						Element component = (Element)comp;
						String compName = component.getAttribute("name");
						String loc = component.getAttribute("loc");
						String lib = component.getAttribute("lib");
						
						System.out.println("  " + compName + " " + loc + " " + lib);
						
						Pair<Integer, Integer> coord = parseLoc(loc);
						if(coord == null) continue;
						
						Map<String, String> properties = new HashMap<>();
						
						if(lib.isEmpty()) {
							properties.put("Subcircuit", compName);
							compName = "com.ra4king.circuitsim.gui.peers.SubcircuitPeer";
						} else {
							compName = convertComponentName(compName);
							if(compName == null) continue;
							
							NodeList propertyList = component.getElementsByTagName("a");
							for(int k = 0; k < propertyList.getLength(); k++) {
								Node a = propertyList.item(k);
								if(a.getNodeType() == Node.ELEMENT_NODE) {
									Element property = (Element)a;
									String propName = property.getAttribute("name");
									String propValue = property.getAttribute("val");
									
									System.out.println("    " + propName + " = " + propValue);
									
									convertProperty(properties, propName, propValue);
								}
							}
						}
						
						components.add(new ComponentInfo(compName,
						                                 coord.getKey(), coord.getValue(),
						                                 properties));
					}
				}
				
				NodeList wireList = circuit.getElementsByTagName("wire");
				for(int j = 0; j < wireList.getLength(); j++) {
					Node wireNode = wireList.item(j);
					if(wireNode.getNodeType() == Node.ELEMENT_NODE) {
						Element wire = (Element)wireNode;
						String from = wire.getAttribute("from");
						String to = wire.getAttribute("to");
						
						System.out.println("  wire " + from + " " + to);
						
						Pair<Integer, Integer> fromCoord = parseLoc(from);
						Pair<Integer, Integer> toCoord = parseLoc(to);
						
						if(fromCoord == null || toCoord == null) continue;
						
						int fx = fromCoord.getKey();
						int fy = fromCoord.getValue();
						
						int tx = toCoord.getKey();
						int ty = toCoord.getValue();
						
						if(fx == tx) {
							wires.add(new WireInfo(fx, fy, ty - fy, false));
						} else {
							wires.add(new WireInfo(fx, fy, tx - fx, true));
						}
					}
				}
				
				circuits.add(new CircuitInfo(name, components, wires));
			}
		}
		
		return new CircuitFile(1, 1, circuits);
	}
	
	private static String convertComponentName(String name) {
		switch(name) {
			case "AND Gate":
				return "com.ra4king.circuitsim.gui.peers.gates.AndGatePeer";
			case "Pin":
				return "com.ra4king.circuitsim.gui.peers.wiring.PinPeer";
			default:
				return null;
		}
	}
	
	private static void convertProperty(Map<String, String> properties, String name, String value) {
		switch(name) {
			case "width":
				properties.put("Bitsize", value);
				break;
			case "facing":
				properties.put("Direction", value.toUpperCase());
				break;
			case "inputs":
				properties.put("Number of Inputs", value);
				break;
			case "labelloc":
				properties.put("Label location", value.toUpperCase());
				break;
			case "output":
				properties.put("Is Input?", value.equals("true") ? "No" : "Yes");
				break;
		}
	}
	
	private static Pair<Integer, Integer> parseLoc(String loc) {
		int comma = loc.indexOf(',');
		if(comma == -1) return null;
		
		int x, y;
		try {
			x = Integer.parseInt(loc.substring(1, comma)) / 10;
			y = Integer.parseInt(loc.substring(comma + 1, loc.length() - 1)) / 10;
		} catch(Exception exc) {
			return null;
		}
		
		return new Pair<>(x, y);
	}

//	public static void main(String[] args) {
//		PlatformImpl.startup(() -> {
//			try {
//				FileChooser fileChooser = new FileChooser();
//				fileChooser.setTitle("Choose sim file");
//				fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
//				fileChooser.getExtensionFilters().add(new ExtensionFilter("Logisim Circuit File", "*.circ"));
//				File selectedFile = fileChooser.showOpenDialog(null);
//				if(selectedFile != null) {
//					long now = System.nanoTime();
//					CircuitFile circuitFile = loadLogisim(selectedFile);
//					System.out.printf("Parsed file in %.3f ms\n", (System.nanoTime() - now) / 1e6);
//				}
//			} catch(Exception exc) {
//				exc.printStackTrace();
//			}
//			
//			System.exit(0);
//		});
//	}
}
