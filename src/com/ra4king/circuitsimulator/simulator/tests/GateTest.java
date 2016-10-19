package com.ra4king.circuitsimulator.simulator.tests;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.simulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

/**
 * @author Roi Atalla
 */
public class GateTest {
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		Circuit circuit = new Circuit(simulator);
		
		AndGate andGate = circuit.addComponent(new AndGate("", 1, 2));
		Pin in1 = circuit.addComponent(new Pin("A", 1, true));
		Pin in2 = circuit.addComponent(new Pin("B", 1, true));
		Pin out = circuit.addComponent(new Pin("Out", 1, false));
		
		andGate.getPort(0).linkPort(in1.getPort(Pin.PORT));
		andGate.getPort(1).linkPort(in2.getPort(Pin.PORT));
		andGate.getPort(andGate.PORT_OUT).linkPort(out.getPort(Pin.PORT));
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		simulator.stepAll();
		in2.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		simulator.stepAll();
		in1.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		simulator.stepAll();
		in2.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		simulator.stepAll();
		
		XorGate xorGate = circuit.addComponent(new XorGate("", 1, 2));
		OrGate orGate = circuit.addComponent(new OrGate("", 1, 2));
		xorGate.getPort(0).linkPort(in1.getPort(0));
		xorGate.getPort(1).linkPort(orGate.getPort(orGate.PORT_OUT));
		orGate.getPort(0).linkPort(xorGate.getPort(xorGate.PORT_OUT));
		simulator.stepAll();
	}
}
