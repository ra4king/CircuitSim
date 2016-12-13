package com.ra4king.circuitsimulator.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ra4king.circuitsimulator.gui.peers.AdderPeer;
import com.ra4king.circuitsimulator.gui.peers.ClockPeer;
import com.ra4king.circuitsimulator.gui.peers.ControlledBufferPeer;
import com.ra4king.circuitsimulator.gui.peers.GatePeer;
import com.ra4king.circuitsimulator.gui.peers.MultiplexerPeer;
import com.ra4king.circuitsimulator.gui.peers.PinPeer;
import com.ra4king.circuitsimulator.gui.peers.RAMPeer;
import com.ra4king.circuitsimulator.gui.peers.RegisterPeer;
import com.ra4king.circuitsimulator.gui.peers.SplitterPeer;
import com.ra4king.circuitsimulator.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.Adder;
import com.ra4king.circuitsimulator.simulator.components.Clock;
import com.ra4king.circuitsimulator.simulator.components.ControlledBuffer;
import com.ra4king.circuitsimulator.simulator.components.Multiplexer;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.RAM;
import com.ra4king.circuitsimulator.simulator.components.Register;
import com.ra4king.circuitsimulator.simulator.components.Splitter;
import com.ra4king.circuitsimulator.simulator.components.Subcircuit;
import com.ra4king.circuitsimulator.simulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NandGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NorGate;
import com.ra4king.circuitsimulator.simulator.components.gates.NotGate;
import com.ra4king.circuitsimulator.simulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XnorGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

/**
 * @author Roi Atalla
 */
public class ComponentManager {
	private List<String> componentsOrder;
	private HashMap<String, ComponentCreatorInternal> components;
	
	public ComponentManager() {
		componentsOrder = new ArrayList<>();
		components = new HashMap<>();
		initComponents();
	}
	
	private void addComponent(String name, ComponentCreatorInternal creatorInternal) {
		components.put(name, creatorInternal);
		componentsOrder.add(name);
	}
	
	private void initComponents() {
		addComponent("Input", (circuit, x, y, bitSize, secondaryOption) -> {
			Pin pin = circuit.addComponent(new Pin("", bitSize, true));
			circuit.getTopLevelState().pushValue(pin.getPort(Pin.PORT), WireValue.of(0, bitSize));
			return new PinPeer(pin, x, y);
		});
		addComponent("Output", (circuit, x, y, bitSize, secondaryOption) -> new PinPeer(circuit.addComponent(new Pin("", bitSize, false)), x, y));
		addComponent("AND", (circuit, x, y, bitSize, secondaryOption) -> new GatePeer(circuit.addComponent(new AndGate("", bitSize, Math.max(2, secondaryOption))), x, y));
		addComponent("NAND", (circuit, x, y, bitSize, secondaryOption) -> new GatePeer(circuit.addComponent(new NandGate("", bitSize, Math.max(2, secondaryOption))), x, y));
		addComponent("OR", (circuit, x, y, bitSize, secondaryOption) -> new GatePeer(circuit.addComponent(new OrGate("", bitSize, Math.max(2, secondaryOption))), x, y));
		addComponent("NOR", (circuit, x, y, bitSize, secondaryOption) -> new GatePeer(circuit.addComponent(new NorGate("", bitSize, Math.max(2, secondaryOption))), x, y));
		addComponent("XOR", (circuit, x, y, bitSize, secondaryOption) -> new GatePeer(circuit.addComponent(new XorGate("", bitSize, Math.max(2, secondaryOption))), x, y));
		addComponent("XNOR", (circuit, x, y, bitSize, secondaryOption) -> new GatePeer(circuit.addComponent(new XnorGate("", bitSize, Math.max(2, secondaryOption))), x, y));
		addComponent("NOT", (circuit, x, y, bitSize, secondaryOption) -> new GatePeer(circuit.addComponent(new NotGate("", bitSize)), x, y));
		addComponent("Buffer", (circuit, x, y, bitSize, secondaryOption) -> new ControlledBufferPeer(circuit.addComponent(new ControlledBuffer("", bitSize)), x, y));
		addComponent("Clock", (circuit, x, y, bitSize, secondaryOption) -> new ClockPeer(circuit.addComponent(new Clock("")), x, y));
		addComponent("Register", (circuit, x, y, bitSize, secondaryOption) -> new RegisterPeer(circuit.addComponent(new Register("", bitSize)), x, y));
		addComponent("Adder", (circuit, x, y, bitSize, secondaryOption) -> new AdderPeer(circuit.addComponent(new Adder("", bitSize)), x, y));
		addComponent("Splitter", (circuit, x, y, bitSize, secondaryOption) -> new SplitterPeer(circuit.addComponent(new Splitter("", bitSize, secondaryOption)), x, y));
		addComponent("Mux", (circuit, x, y, bitSize, secondaryOption) -> new MultiplexerPeer(circuit.addComponent(new Multiplexer("", bitSize, secondaryOption)), x, y));
		addComponent("RAM", (circuit, x, y, bitSize, secondaryOption) -> new RAMPeer(circuit.addComponent(new RAM("", bitSize, secondaryOption)), x, y));
	}
	
	public List<String> getComponentNames() {
		return componentsOrder;
	}
	
	public void addCircuit(String name, CircuitManager circuitManager) {
		addComponent(name, (circuit, x, y, bitSize, secondaryOption) -> {
			if(circuit == circuitManager.getCircuit()) {
				throw new IllegalArgumentException("Cannot create subcircuit inside own circuit.");
			}
			
			return new SubcircuitPeer(circuit.addComponent(new Subcircuit(name, circuitManager.getCircuit())), x, y);
		});
	}
	
	public ComponentCreator getComponentCreator(String component, int bitSize, int secondaryOption) {
		if(component == null) return null;
		
		return (circuit, x, y) -> components.get(component).createComponent(circuit, x, y, bitSize, secondaryOption);
	}
	
	public interface ComponentCreator {
		ComponentPeer<?> createComponent(Circuit circuit, int x, int y);
	}
	
	private interface ComponentCreatorInternal {
		ComponentPeer<?> createComponent(Circuit circuit, int x, int y, int bitSize, int secondaryOption);
	}
}
