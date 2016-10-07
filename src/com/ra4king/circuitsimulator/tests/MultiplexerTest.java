package com.ra4king.circuitsimulator.tests;

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
		
		Multiplexer mux = new Multiplexer(simulator, "", 4, 2);
		AndGate andGate = new AndGate(simulator, "", 4, 2);
		OrGate orGate = new OrGate(simulator, "", 4, 2);
		XorGate xorGate = new XorGate(simulator, "", 4, 2);
		NotGate notGate = new NotGate(simulator, "", 4);
		
		Pin in1 = new Pin(simulator, "A", 4, true);
		Pin in2 = new Pin(simulator, "B", 4, true);
		Pin sel = new Pin(simulator, "Sel", 2, true);
		Pin out = new Pin(simulator, "Out", 4, false);
		
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
		
		in1.setValue(WireValue.of(5, 4));
		in2.setValue(WireValue.of(3, 4));
		simulator.stepAll();
		
		sel.setValue(WireValue.of(0, 2));
		simulator.stepAll();
		sel.setValue(WireValue.of(1, 2));
		simulator.stepAll();
		sel.setValue(WireValue.of(2, 2));
		simulator.stepAll();
		sel.setValue(WireValue.of(3, 2));
		simulator.stepAll();
	}
}
