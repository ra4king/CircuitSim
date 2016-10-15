package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.components.Multiplexer;
import com.ra4king.circuitsimulator.components.Pin;
import com.ra4king.circuitsimulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.components.gates.NotGate;
import com.ra4king.circuitsimulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.components.gates.XorGate;

/**
 * @author Roi Atalla
 */
public class MultiplexerTest {
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		Circuit circuit = new Circuit(simulator);
		
		Multiplexer mux = circuit.addComponent(new Multiplexer("", 4, 2));
		AndGate andGate = circuit.addComponent(new AndGate("", 4, 2));
		OrGate orGate = circuit.addComponent(new OrGate("", 4, 2));
		XorGate xorGate = circuit.addComponent(new XorGate("", 4, 2));
		NotGate notGate = circuit.addComponent(new NotGate("", 4));
		
		Pin in1 = circuit.addComponent(new Pin("A", 4));
		Pin in2 = circuit.addComponent(new Pin("B", 4));
		Pin sel = circuit.addComponent(new Pin("Sel", 2));
		Pin out = circuit.addComponent(new Pin("Out", 4));
		
		in1.getPort(0)
				.linkPort(andGate.getPort(0))
				.linkPort(orGate.getPort(0))
				.linkPort(xorGate.getPort(0))
				.linkPort(notGate.getPort(0));
		in2.getPort(0)
				.linkPort(andGate.getPort(1))
				.linkPort(orGate.getPort(1))
				.linkPort(xorGate.getPort(1));
		
		andGate.getPort(2).linkPort(mux.getPort(0));
		orGate.getPort(2).linkPort(mux.getPort(1));
		xorGate.getPort(2).linkPort(mux.getPort(2));
		notGate.getPort(1).linkPort(mux.getPort(3));
		
		sel.getPort(0).linkPort(mux.getPort(4));
		out.getPort(0).linkPort(mux.getPort(5));
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(5, 4));
		in2.setValue(circuit.getTopLevelState(), WireValue.of(3, 4));
		simulator.stepAll();
		
		sel.setValue(circuit.getTopLevelState(), WireValue.of(0, 2));
		simulator.stepAll();
		sel.setValue(circuit.getTopLevelState(), WireValue.of(1, 2));
		simulator.stepAll();
		sel.setValue(circuit.getTopLevelState(), WireValue.of(2, 2));
		simulator.stepAll();
		sel.setValue(circuit.getTopLevelState(), WireValue.of(3, 2));
		simulator.stepAll();
	}
}
