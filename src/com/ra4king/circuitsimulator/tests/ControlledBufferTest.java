package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Circuit;
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
		Circuit circuit = new Circuit(sim);
		
		AndGate andGate = new AndGate(circuit, "", 4, 2);
		OrGate orGate = new OrGate(circuit, "", 4, 2);
		XorGate xorGate = new XorGate(circuit, "", 4, 2);
		ControlledBuffer bufferA = new ControlledBuffer(circuit, "A", 4);
		ControlledBuffer bufferB = new ControlledBuffer(circuit, "B", 4);
		ControlledBuffer bufferC = new ControlledBuffer(circuit, "C", 4);
		Pin inA = new Pin(circuit, "A", 4);
		Pin inB = new Pin(circuit, "B", 4);
		Pin selA = new Pin(circuit, "Enable A", 1);
		Pin selB = new Pin(circuit, "Enable B", 1);
		Pin selC = new Pin(circuit, "Enable C", 1);
		Pin out = new Pin(circuit, "Out", 4);
		
		inA.getPort(0).linkPort(andGate.getPort(0)).linkPort(orGate.getPort(0)).linkPort(xorGate.getPort(0));
		inB.getPort(0).linkPort(andGate.getPort(1)).linkPort(orGate.getPort(1)).linkPort(xorGate.getPort(1));
		bufferA.getPort(0).linkPort(andGate.getPort(2));
		bufferA.getPort(1).linkPort(selA.getPort(0));
		bufferA.getPort(2).linkPort(out.getPort(0));
		
		bufferB.getPort(0).linkPort(orGate.getPort(2));
		bufferB.getPort(1).linkPort(selB.getPort(0));
		bufferB.getPort(2).linkPort(out.getPort(0));
		
		bufferC.getPort(0).linkPort(xorGate.getPort(2));
		bufferC.getPort(1).linkPort(selC.getPort(0));
		bufferC.getPort(2).linkPort(out.getPort(0));
		
		System.out.println("Selecting 1:");
		inA.setValue(circuit.getTopLevelState(), WireValue.of(5, 4));
		inB.setValue(circuit.getTopLevelState(), WireValue.of(3, 4));
		selA.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		System.out.println("Stepping:");
		sim.stepAll();
		
		System.out.println("\nSelecting 2:");
		selA.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		selB.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		System.out.println("Stepping:");
		sim.stepAll();
		
		System.out.println("\nSelecting 3:");
		//selB.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		selC.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		System.out.println("Stepping:");
		sim.stepAll();
	}
}
