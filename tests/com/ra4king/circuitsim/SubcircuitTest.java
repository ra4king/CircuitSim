package com.ra4king.circuitsim;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.Subcircuit;
import com.ra4king.circuitsim.simulator.components.arithmetic.Adder;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class SubcircuitTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Circuit circuit = new Circuit("Subcircuit Test - Circuit 1", sim);
		Adder adder = circuit.addComponent(new Adder("", 4));
		Pin inA = circuit.addComponent(new Pin("A", 4, true));
		Pin inB = circuit.addComponent(new Pin("B", 4, true));
		Pin inC = circuit.addComponent(new Pin("C", 1, true));
		Pin out = circuit.addComponent(new Pin("Out", 4, false));
		adder.getPort(Adder.PORT_A).linkPort(inA.getPort(Pin.PORT));
		adder.getPort(Adder.PORT_B).linkPort(inB.getPort(Pin.PORT));
		adder.getPort(Adder.PORT_CARRY_IN).linkPort(inC.getPort(Pin.PORT));
		adder.getPort(Adder.PORT_OUT).linkPort(out.getPort(Pin.PORT));
		
		inA.setValue(circuit.getTopLevelState(), WireValue.of(5, 4));
		inB.setValue(circuit.getTopLevelState(), WireValue.of(3, 4));
		inC.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		Circuit circuit2 = new Circuit("Subcircuit Test - Circuit 2", sim);
		Subcircuit subcircuit = circuit2.addComponent(new Subcircuit("", circuit));
		Pin in2A = circuit2.addComponent(new Pin("A2", 4, true));
		Pin in2B = circuit2.addComponent(new Pin("B2", 4, true));
		Pin in2C = circuit2.addComponent(new Pin("C2", 1, true));
		Pin out2 = circuit2.addComponent(new Pin("Out2", 4, false));
		subcircuit.getPort(inA).linkPort(in2A.getPort(Pin.PORT));
		subcircuit.getPort(inB).linkPort(in2B.getPort(Pin.PORT));
		subcircuit.getPort(inC).linkPort(in2C.getPort(Pin.PORT));
		subcircuit.getPort(out).linkPort(out2.getPort(Pin.PORT));
		
		in2A.setValue(circuit2.getTopLevelState(), WireValue.of(2, 4));
		in2B.setValue(circuit2.getTopLevelState(), WireValue.of(4, 4));
		in2C.setValue(circuit2.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
	}
}
