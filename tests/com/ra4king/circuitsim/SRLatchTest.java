package com.ra4king.circuitsim;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.gates.NorGate;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class SRLatchTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Circuit circuit = new Circuit("SR Latch Test", sim);
		
		NorGate nor1 = circuit.addComponent(new NorGate("A", 1, 2));
		NorGate nor2 = circuit.addComponent(new NorGate("B", 1, 2));
		Pin in1 = circuit.addComponent(new Pin("A", 1, true));
		Pin in2 = circuit.addComponent(new Pin("B", 1, true));
		Pin out1 = circuit.addComponent(new Pin("Out", 1, false));
		Pin out2 = circuit.addComponent(new Pin("~Out", 1, false));
		
		in1.getPort(Pin.PORT).linkPort(nor1.getPort(0));
		in2.getPort(Pin.PORT).linkPort(nor2.getPort(0));
		out1.getPort(Pin.PORT).linkPort(nor1.getOutPort()).linkPort(nor2.getPort(1));
		out2.getPort(Pin.PORT).linkPort(nor2.getOutPort()).linkPort(nor1.getPort(1));
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getLastReceived(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getLastReceived(out2.getPort(0)));
		System.out.println();
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getLastReceived(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getLastReceived(out2.getPort(0)));
		System.out.println();
		
		in2.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getLastReceived(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getLastReceived(out2.getPort(0)));
		System.out.println();
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(1, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getLastReceived(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getLastReceived(out2.getPort(0)));
		System.out.println();
		
		in2.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getLastReceived(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getLastReceived(out2.getPort(0)));
		System.out.println();
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		sim.stepAll();
		
		System.out.println("--------------------------");
		System.out.println(out1 + ": " + circuit.getTopLevelState().getLastReceived(out1.getPort(0)));
		System.out.println(out2 + ": " + circuit.getTopLevelState().getLastReceived(out2.getPort(0)));
		System.out.println();
	}
}
