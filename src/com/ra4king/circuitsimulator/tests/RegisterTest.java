package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Circuit;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.components.Pin;
import com.ra4king.circuitsimulator.components.Register;

/**
 * @author Roi Atalla
 */
public class RegisterTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Circuit circuit = new Circuit(sim);
		
		Register register = circuit.addComponent(new Register("", 4));
		Pin dataIn = circuit.addComponent(new Pin("In", 4));
		Pin enable = circuit.addComponent(new Pin("Enable", 1));
		Pin clk = circuit.addComponent(new Pin("Clk", 1));
		Pin dataOut = circuit.addComponent(new Pin("Out", 4));
		
		register.getPort(Register.PORT_IN).linkPort(dataIn.getPort(0));
		register.getPort(Register.PORT_ENABLE).linkPort(enable.getPort(0));
		register.getPort(Register.PORT_CLK).linkPort(clk.getPort(0));
		register.getPort(Register.PORT_OUT).linkPort(dataOut.getPort(0));
		
		final WireValue ONE = WireValue.of(1, 1);
		final WireValue ZERO = WireValue.of(0, 1);
		
		dataIn.setValue(circuit.getTopLevelState(), WireValue.of(10, 4));
		enable.setValue(circuit.getTopLevelState(), ONE);
		clk.setValue(circuit.getTopLevelState(), ZERO);
		sim.stepAll();
		
		System.out.println("Clk = 1");
		clk.setValue(circuit.getTopLevelState(), ONE);
		sim.stepAll();
		
		System.out.println("Clk = 0");
		clk.setValue(circuit.getTopLevelState(), ZERO);
		dataIn.setValue(circuit.getTopLevelState(), WireValue.of(8, 4));
		sim.stepAll();
		
		System.out.println("Clk = 1");
		clk.setValue(circuit.getTopLevelState(), ONE);
		dataIn.setValue(circuit.getTopLevelState(), WireValue.of(2, 4));
		sim.stepAll();
	}
}
