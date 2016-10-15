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
		
		AndGate andGate = circuit.addComponent(new AndGate("", 4, 2));
		OrGate orGate = circuit.addComponent(new OrGate("", 4, 2));
		XorGate xorGate = circuit.addComponent(new XorGate("", 4, 2));
		ControlledBuffer bufferA = circuit.addComponent(new ControlledBuffer("A", 4));
		ControlledBuffer bufferB = circuit.addComponent(new ControlledBuffer("B", 4));
		ControlledBuffer bufferC = circuit.addComponent(new ControlledBuffer("C", 4));
		Pin inA = circuit.addComponent(new Pin("A", 4));
		Pin inB = circuit.addComponent(new Pin("B", 4));
		Pin selA = circuit.addComponent(new Pin("Enable A", 1));
		Pin selB = circuit.addComponent(new Pin("Enable B", 1));
		Pin selC = circuit.addComponent(new Pin("Enable C", 1));
		Pin out = circuit.addComponent(new Pin("Out", 4));
		
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
		selB.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		selC.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		System.out.println("Stepping:");
		sim.stepAll();
	}
}
