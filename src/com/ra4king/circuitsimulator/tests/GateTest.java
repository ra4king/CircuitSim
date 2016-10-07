package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.components.Pin;
import com.ra4king.circuitsimulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.components.gates.XorGate;

/**
 * @author Roi Atalla
 */
public class GateTest {
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		
		AndGate andGate = new AndGate(simulator, "", 1, 2);
		Pin in1 = new Pin(simulator, "A", 1, true);
		Pin in2 = new Pin(simulator, "B", 1, true);
		Pin out = new Pin(simulator, "Out", 1, false);
		
		andGate.getPort(0).linkPort(in1.getPort(0));
		andGate.getPort(1).linkPort(in2.getPort(0));
		andGate.getPort(2).linkPort(out.getPort(0));
		
		in1.setValue(WireValue.of(0, 1));
		simulator.stepAll();
		in2.setValue(WireValue.of(0, 1));
		simulator.stepAll();
		in1.setValue(WireValue.of(1, 1));
		simulator.stepAll();
		in2.setValue(WireValue.of(1, 1));
		simulator.stepAll();
		
		XorGate xorGate = new XorGate(simulator, "", 1, 2);
		OrGate orGate = new OrGate(simulator, "", 1, 2);
		xorGate.getPort(0).linkPort(in1.getPort(0));
		xorGate.getPort(1).linkPort(orGate.getPort(2));
		orGate.getPort(0).linkPort(xorGate.getPort(2));
		simulator.stepAll();
	}
}
