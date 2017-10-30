package com.ra4king.circuitsim;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.gates.AndGate;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;
import com.ra4king.circuitsim.simulator.components.wiring.Splitter;

/**
 * @author Roi Atalla
 */
public class SplitterTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		
		final int bits = 10;
		final int inputs = 3;
		
		Circuit circuit = new Circuit("Splitter Test", sim);
		AndGate[] ands = new AndGate[bits];
		for(int i = 0; i < ands.length; i++) {
			ands[i] = circuit.addComponent(new AndGate(String.valueOf(i), 1, inputs));
		}
		
		Pin[] ins = new Pin[inputs];
		for(int i = 0; i < ins.length; i++) {
			ins[i] = circuit.addComponent(new Pin(String.valueOf(i), bits, true));
		}
		
		Splitter[] splitters = new Splitter[inputs];
		for(int i = 0; i < splitters.length; i++) {
			splitters[i] = circuit.addComponent(new Splitter(String.valueOf(i), bits, bits));
		}
		
		Splitter joiner = circuit.addComponent(new Splitter("Joiner", bits, bits));
		Pin out = circuit.addComponent(new Pin("Out", bits, false));
		
		out.getPort(Pin.PORT).linkPort(joiner.getPort(joiner.PORT_JOINED));
		
		for(int i = 0; i < ins.length; i++) {
			ins[i].getPort(Pin.PORT).linkPort(splitters[i].getPort(splitters[i].PORT_JOINED));
		}
		
		for(int i = 0; i < ands.length; i++) {
			for(int j = 0; j < splitters.length; j++) {
				splitters[j].getPort(i).linkPort(ands[i].getPort(j));
			}
			
			ands[i].getOutPort().linkPort(joiner.getPort(i));
		}
		
		ins[0].setValue(circuit.getTopLevelState(), WireValue.of(0xFF, 10));
		ins[1].setValue(circuit.getTopLevelState(), WireValue.of(0x55, 10));
		ins[2].setValue(circuit.getTopLevelState(), WireValue.of(0x15, 10));
		
		
		sim.stepAll();
	}
}
