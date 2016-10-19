package com.ra4king.circuitsimulator.simulator.tests;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.gates.NorGate;

/**
 * @author Roi Atalla
 */
public class SRLatchTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Circuit circuit = new Circuit(sim);
		
		NorGate nor1 = circuit.addComponent(new NorGate("A", 1, 2));
		NorGate nor2 = circuit.addComponent(new NorGate("B", 1, 2));
		Pin in1 = circuit.addComponent(new Pin("A", 1, true));
		Pin in2 = circuit.addComponent(new Pin("B", 1, true));
		Pin out1 = circuit.addComponent(new Pin("Out", 1, false));
		Pin out2 = circuit.addComponent(new Pin("~Out", 1, false));
		
		in1.getPort(Pin.PORT).linkPort(nor1.getPort(0));
		in2.getPort(Pin.PORT).linkPort(nor2.getPort(0));
		out1.getPort(Pin.PORT).linkPort(nor1.getPort(nor1.PORT_OUT)).linkPort(nor2.getPort(1));
		out2.getPort(Pin.PORT).linkPort(nor2.getPort(nor2.PORT_OUT)).linkPort(nor1.getPort(1));
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getValue(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getValue(out2.getPort(0)));
		System.out.println();
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getValue(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getValue(out2.getPort(0)));
		System.out.println();
		
		in2.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getValue(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getValue(out2.getPort(0)));
		System.out.println();
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getValue(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getValue(out2.getPort(0)));
		System.out.println();
		
		in2.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getValue(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getValue(out2.getPort(0)));
		System.out.println();
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getValue(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getValue(out2.getPort(0)));
		System.out.println();
	}
}
