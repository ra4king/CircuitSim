package com.ra4king.circuitsim;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.gates.AndGate;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class GateTest {
	public static void main(String[] args) {
		Simulator simulator = new Simulator();
		Circuit circuit = new Circuit("Gate Test", simulator);
		
		AndGate andGate = circuit.addComponent(new AndGate("", 1, 2));
		Pin in1 = circuit.addComponent(new Pin("A", 1, true));
		Pin in2 = circuit.addComponent(new Pin("B", 1, true));
		Pin out = circuit.addComponent(new Pin("Out", 1, false));
		
		andGate.getPort(0).linkPort(in1.getPort(Pin.PORT));
		andGate.getPort(1).linkPort(in2.getPort(Pin.PORT));
		andGate.getOutPort().linkPort(out.getPort(Pin.PORT));
		
		out.addChangeListener(
				new Pair<>(circuit.getTopLevelState(),
				           (pin, state, value) -> System.out.println("Value changed: " + value)));
		
		System.out.println("Setting in1 to 0");
		in1.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		simulator.stepAll();
		System.out.println("Setting in2 to 0");
		in2.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		simulator.stepAll();
		System.out.println("Setting in1 to 1");
		in1.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		simulator.stepAll();
		System.out.println("Setting in2 to 1");
		in2.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		simulator.stepAll();
		
		System.out.println("Final value: " + circuit.getTopLevelState().getLastReceived(out.getPort(Pin.PORT)));
	}
}
