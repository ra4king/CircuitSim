package com.ra4king.circuitsimulator.simulator.tests;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.Simulator;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.ControlledBuffer;
import com.ra4king.circuitsimulator.simulator.components.Pin;
import com.ra4king.circuitsimulator.simulator.components.gates.AndGate;
import com.ra4king.circuitsimulator.simulator.components.gates.OrGate;
import com.ra4king.circuitsimulator.simulator.components.gates.XorGate;

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
		Pin inA = circuit.addComponent(new Pin("A", 4, true));
		Pin inB = circuit.addComponent(new Pin("B", 4, true));
		Pin selA = circuit.addComponent(new Pin("Enable A", 1, true));
		Pin selB = circuit.addComponent(new Pin("Enable B", 1, true));
		Pin selC = circuit.addComponent(new Pin("Enable C", 1, true));
		Pin out = circuit.addComponent(new Pin("Out", 4, false));
		
		inA.getPort(Pin.PORT).linkPort(andGate.getPort(0)).linkPort(orGate.getPort(0)).linkPort(xorGate.getPort(0));
		inB.getPort(Pin.PORT).linkPort(andGate.getPort(1)).linkPort(orGate.getPort(1)).linkPort(xorGate.getPort(1));
		bufferA.getPort(ControlledBuffer.PORT_IN).linkPort(andGate.getPort(andGate.PORT_OUT));
		bufferA.getPort(ControlledBuffer.PORT_ENABLE).linkPort(selA.getPort(Pin.PORT));
		bufferA.getPort(ControlledBuffer.PORT_OUT).linkPort(out.getPort(Pin.PORT));
		
		bufferB.getPort(ControlledBuffer.PORT_IN).linkPort(orGate.getPort(orGate.PORT_OUT));
		bufferB.getPort(ControlledBuffer.PORT_ENABLE).linkPort(selB.getPort(Pin.PORT));
		bufferB.getPort(ControlledBuffer.PORT_OUT).linkPort(out.getPort(Pin.PORT));
		
		bufferC.getPort(ControlledBuffer.PORT_IN).linkPort(xorGate.getPort(xorGate.PORT_OUT));
		bufferC.getPort(ControlledBuffer.PORT_ENABLE).linkPort(selC.getPort(Pin.PORT));
		bufferC.getPort(ControlledBuffer.PORT_OUT).linkPort(out.getPort(Pin.PORT));
		
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
