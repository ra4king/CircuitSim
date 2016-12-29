package com.ra4king.circuitsimulator.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ra4king.circuitsimulator.gui.peers.AdderPeer;
import com.ra4king.circuitsimulator.gui.peers.ClockPeer;
import com.ra4king.circuitsimulator.gui.peers.ControlledBufferPeer;
import com.ra4king.circuitsimulator.gui.peers.MultiplexerPeer;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.gui.peers.RAMPeer;
import com.ra4king.circuitsimulator.gui.peers.RegisterPeer;
import com.ra4king.circuitsimulator.gui.peers.SplitterPeer;
import com.ra4king.circuitsimulator.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsimulator.gui.peers.gates.AndGatePeer;
import com.ra4king.circuitsimulator.gui.peers.gates.NandGatePeer;
import com.ra4king.circuitsimulator.gui.peers.gates.NorGatePeer;
import com.ra4king.circuitsimulator.gui.peers.gates.NotGatePeer;
import com.ra4king.circuitsimulator.gui.peers.gates.OrGatePeer;
import com.ra4king.circuitsimulator.gui.peers.gates.XnorGatePeer;
import com.ra4king.circuitsimulator.gui.peers.gates.XorGatePeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.Pin;

/**
 * @author Roi Atalla
 */
public class ComponentManager {
	private List<String> componentsOrder;
	private HashMap<String, ComponentCreator> components;
	
	public ComponentManager() {
		componentsOrder = new ArrayList<>();
		components = new HashMap<>();
		initComponents();
	}
	
	private static <T extends ComponentPeer<?>> ComponentCreator<T> forClass(Class<T> clazz) {
		return (circuit, properties, x, y) -> {
			try {
				return clazz.getConstructor(Circuit.class, Properties.class, Integer.TYPE, Integer.TYPE)
				            .newInstance(circuit, properties, x, y);
			} catch(Exception exc) {
				throw new RuntimeException(exc);
			}
		};
	}
	
	private void addComponent(String name, ComponentCreator creator) {
		components.put(name, creator);
		componentsOrder.add(name);
	}
	
	private void initComponents() {
		addComponent("Input", (circuit, properties, x, y) -> {
			properties.setValue(PinPeer.IS_INPUT, "Yes");
			PinPeer pinPeer = forClass(PinPeer.class).createComponent(circuit, properties, x, y);
			Pin pin = pinPeer.getComponent();
			circuit.getTopLevelState().pushValue(pin.getPort(Pin.PORT), WireValue.of(0, pin.getBitSize()));
			return pinPeer;
		});
		addComponent("Output", (circuit, properties, x, y) -> {
			properties.setValue(PinPeer.IS_INPUT, "No");
			return forClass(PinPeer.class).createComponent(circuit, properties, x, y);
		});
		addComponent("AND", forClass(AndGatePeer.class));
		addComponent("NAND", forClass(NandGatePeer.class));
		addComponent("OR", forClass(OrGatePeer.class));
		addComponent("NOR", forClass(NorGatePeer.class));
		addComponent("XOR", forClass(XorGatePeer.class));
		addComponent("XNOR", forClass(XnorGatePeer.class));
		addComponent("NOT", forClass(NotGatePeer.class));
		addComponent("Buffer", forClass(ControlledBufferPeer.class));
		addComponent("Clock", forClass(ClockPeer.class));
		addComponent("Register", forClass(RegisterPeer.class));
		addComponent("Adder", forClass(AdderPeer.class));
		addComponent("Splitter", forClass(SplitterPeer.class));
		addComponent("Mux", forClass(MultiplexerPeer.class));
		addComponent("RAM", forClass(RAMPeer.class));
	}
	
	public List<String> getComponentNames() {
		return componentsOrder;
	}
	
	public void addCircuit(String name, CircuitManager circuitManager) {
		addComponent(name, (circuit, properties, x, y) -> {
			if(circuit == circuitManager.getCircuit()) {
				throw new IllegalArgumentException("Cannot create subcircuit inside own circuit.");
			}
			
			return new SubcircuitPeer(circuit, properties, circuitManager.getCircuit(), x, y);
		});
	}
	
	public ComponentCreator getComponentCreator(String component) {
		if(component == null) return null;
		return components.get(component);
	}
	
	public interface ComponentCreator<T extends ComponentPeer<?>> {
		default T createComponent(Circuit circuit, int bitSize, int x, int y) {
			Properties properties = new Properties();
			properties.setValue(Properties.BITSIZE, String.valueOf(bitSize));
			
			return createComponent(circuit, properties, x, y);
		}
		
		T createComponent(Circuit circuit, Properties properties, int x, int y);
	}
}
