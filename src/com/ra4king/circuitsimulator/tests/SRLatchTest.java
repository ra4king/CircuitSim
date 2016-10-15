package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.components.Pin;
import com.ra4king.circuitsimulator.components.gates.NorGate;

/**
 * @author Roi Atalla
 */
public class SRLatchTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Circuit circuit = new Circuit(sim);
		
		NorGate nor1 = circuit.addComponent(new NorGate("A", 1, 2));
		NorGate nor2 = circuit.addComponent(new NorGate("B", 1, 2));
		Pin in1 = circuit.addComponent(new Pin("A", 1));
		Pin in2 = circuit.addComponent(new Pin("B", 1));
		Pin out1 = circuit.addComponent(new Pin("Out", 1));
		Pin out2 = circuit.addComponent(new Pin("~Out", 1));
		
		in1.getPort(0).linkPort(nor1.getPort(0));
		in2.getPort(0).linkPort(nor2.getPort(0));
		out1.getPort(0).linkPort(nor1.getPort(2)).linkPort(nor2.getPort(1));
		out2.getPort(0).linkPort(nor2.getPort(2)).linkPort(nor1.getPort(1));
		
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
