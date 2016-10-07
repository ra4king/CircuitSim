package com.ra4king.circuitsimulator.tests;

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
		
		NorGate nor1 = new NorGate(sim, "A", 1, 2);
		NorGate nor2 = new NorGate(sim, "B", 1, 2);
		Pin in1 = new Pin(sim, "A", 1, true);
		Pin in2 = new Pin(sim, "B", 1, true);
		Pin out1 = new Pin(sim, "Out", 1, false);
		Pin out2 = new Pin(sim, "~Out", 1, false);
		
		in1.getPort(0).linkPort(nor1.getPort(0));
		in2.getPort(0).linkPort(nor2.getPort(0));
		out1.getPort(0).linkPort(nor1.getPort(2)).linkPort(nor2.getPort(1));
		out2.getPort(0).linkPort(nor2.getPort(2)).linkPort(nor1.getPort(1));
		
		in1.setValue(WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + out1.getPort(0).getWireValue());
		System.out.println(out2 + ": " + out2.getPort(0).getWireValue());
		System.out.println();
		
		in1.setValue(WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + out1.getPort(0).getWireValue());
		System.out.println(out2 + ": " + out2.getPort(0).getWireValue());
		System.out.println();
		
		in2.setValue(WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + out1.getPort(0).getWireValue());
		System.out.println(out2 + ": " + out2.getPort(0).getWireValue());
		System.out.println();
		
		in1.setValue(WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + out1.getPort(0).getWireValue());
		System.out.println(out2 + ": " + out2.getPort(0).getWireValue());
		System.out.println();
		
		in2.setValue(WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + out1.getPort(0).getWireValue());
		System.out.println(out2 + ": " + out2.getPort(0).getWireValue());
		System.out.println();
		
		in1.setValue(WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + out1.getPort(0).getWireValue());
		System.out.println(out2 + ": " + out2.getPort(0).getWireValue());
		System.out.println();
	}
}
