package com.ra4king.circuitsimulator.tests;

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
		Register register = new Register(sim, "", 4);
		Pin dataIn = new Pin(sim, "In", 4, true);
		Pin enable = new Pin(sim, "Enable", 1, true);
		Pin clk = new Pin(sim, "Clk", 1, true);
		Pin dataOut = new Pin(sim, "Out", 4, false);
		
		register.getPort(Register.PORT_IN).linkPort(dataIn.getPort(0));
		register.getPort(Register.PORT_ENABLE).linkPort(enable.getPort(0));
		register.getPort(Register.PORT_CLK).linkPort(clk.getPort(0));
		register.getPort(Register.PORT_OUT).linkPort(dataOut.getPort(0));
		
		final WireValue ONE = WireValue.of(1, 1);
		final WireValue ZERO = WireValue.of(0, 1);
		
		dataIn.setValue(WireValue.of(10, 4));
		enable.setValue(ONE);
		clk.setValue(ZERO);
		sim.stepAll();
		
		System.out.println("Clk = 1");
		clk.setValue(ONE);
		sim.stepAll();
		
		System.out.println("Clk = 0");
		clk.setValue(ZERO);
		dataIn.setValue(WireValue.of(8, 4));
		sim.stepAll();
		
		System.out.println("Clk = 1");
		clk.setValue(ONE);
		dataIn.setValue(WireValue.of(2, 4));
		sim.stepAll();
	}
}
