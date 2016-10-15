package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.components.Adder;
import com.ra4king.circuitsimulator.components.Pin;
import com.ra4king.circuitsimulator.components.Subcircuit;

/**
 * @author Roi Atalla
 */
public class SubcircuitTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Circuit circuit = new Circuit(sim);
		Adder adder = circuit.addComponent(new Adder("", 4));
		Pin inA = circuit.addComponent(new Pin("A", 4));
		Pin inB = circuit.addComponent(new Pin("B", 4));
		Pin inC = circuit.addComponent(new Pin("C", 1));
		Pin out = circuit.addComponent(new Pin("Out", 4));
		adder.getPort(Adder.PORT_A).linkPort(inA.getPort(0));
		adder.getPort(Adder.PORT_B).linkPort(inB.getPort(0));
		adder.getPort(Adder.PORT_CARRY_IN).linkPort(inC.getPort(0));
		adder.getPort(Adder.PORT_OUT).linkPort(out.getPort(0));
		
		inA.setValue(circuit.getTopLevelState(), WireValue.of(5, 4));
		inB.setValue(circuit.getTopLevelState(), WireValue.of(3, 4));
		inC.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		Circuit circuit2 = new Circuit(sim);
		Subcircuit subcircuit = circuit2.addComponent(new Subcircuit("", circuit));
		Pin in2A = circuit2.addComponent(new Pin("A2", 4));
		Pin in2B = circuit2.addComponent(new Pin("B2", 4));
		Pin in2C = circuit2.addComponent(new Pin("C2", 1));
		Pin out2 = circuit2.addComponent(new Pin("Out2", 4));
		subcircuit.getPort(inA).linkPort(in2A.getPort(0));
		subcircuit.getPort(inB).linkPort(in2B.getPort(0));
		subcircuit.getPort(inC).linkPort(in2C.getPort(0));
		subcircuit.getPort(out).linkPort(out2.getPort(0));
		
		in2A.setValue(circuit2.getTopLevelState(), WireValue.of(2, 4));
		in2B.setValue(circuit2.getTopLevelState(), WireValue.of(4, 4));
		in2C.setValue(circuit2.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
	}
}
