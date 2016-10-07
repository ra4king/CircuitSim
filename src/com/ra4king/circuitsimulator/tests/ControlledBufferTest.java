package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.components.ControlledBuffer;
import com.ra4king.circuitsimulator.components.Pin;
import com.ra4king.circuitsimulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.components.gates.XorGate;

/**
 * @author Roi Atalla
 */
public class ControlledBufferTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		
		AndGate andGate = new AndGate(sim, "", 4, 2);
		OrGate orGate = new OrGate(sim, "", 4, 2);
		XorGate xorGate = new XorGate(sim, "", 4, 2);
		ControlledBuffer buffer1 = new ControlledBuffer(sim, "A", 4);
		ControlledBuffer buffer2 = new ControlledBuffer(sim, "B", 4);
		ControlledBuffer buffer3 = new ControlledBuffer(sim, "C", 4);
		Pin in1 = new Pin(sim, "A", 4, true);
		Pin in2 = new Pin(sim, "B", 4, true);
		Pin sel1 = new Pin(sim, "Enable A", 1, true);
		Pin sel2 = new Pin(sim, "Enable B", 1, true);
		Pin sel3 = new Pin(sim, "Enable C", 1, true);
		Pin out = new Pin(sim, "Out", 4, false);
		
		in1.getPort(0).linkPort(andGate.getPort(0)).linkPort(orGate.getPort(0)).linkPort(xorGate.getPort(0));
		in2.getPort(0).linkPort(andGate.getPort(1)).linkPort(orGate.getPort(1)).linkPort(xorGate.getPort(1));
		buffer1.getPort(0).linkPort(andGate.getPort(2));
		buffer1.getPort(1).linkPort(sel1.getPort(0));
		buffer1.getPort(2).linkPort(out.getPort(0));
		
		buffer2.getPort(0).linkPort(orGate.getPort(2));
		buffer2.getPort(1).linkPort(sel2.getPort(0));
		buffer2.getPort(2).linkPort(out.getPort(0));
		
		buffer3.getPort(0).linkPort(xorGate.getPort(2));
		buffer3.getPort(1).linkPort(sel3.getPort(0));
		buffer3.getPort(2).linkPort(out.getPort(0));
		
		System.out.println("Selecting 1:");
		in1.setValue(WireValue.of(5, 4));
		in2.setValue(WireValue.of(3, 4));
		sel1.setValue(WireValue.of(1, 1));
		System.out.println("Stepping:");
		sim.stepAll();
		
		System.out.println("\nSelecting 2:");
		sel1.setValue(WireValue.of(0, 1));
		sel2.setValue(WireValue.of(1, 1));
		System.out.println("Stepping:");
		sim.stepAll();
		
		System.out.println("\nSelecting 3:");
		sel2.setValue(WireValue.of(0, 1));
		sel3.setValue(WireValue.of(1, 1));
		System.out.println("Stepping:");
		sim.stepAll();
	}
}
