package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.components.Adder;
import com.ra4king.circuitsimulator.components.Clock;
import com.ra4king.circuitsimulator.components.Pin;
import com.ra4king.circuitsimulator.components.Register;

/**
 * @author Roi Atalla
 */
public class ClockTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Clock clock = new Clock(sim, "");
		Register register = new Register(sim, "", 4);
		Adder adder = new Adder(sim, "", 4);
		Pin din = new Pin(sim, "", 4, true);
		Pin cin = new Pin(sim, "", 1, true);
		Pin out = new Pin(sim, "", 4, false);
		
		register.getPort(Register.PORT_IN).linkPort(adder.getPort(Adder.PORT_OUT));
		register.getPort(Register.PORT_CLK).linkPort(clock.getPort(0));
		adder.getPort(Adder.PORT_A).linkPort(register.getPort(Register.PORT_OUT)).linkPort(out.getPort(0));
		adder.getPort(Adder.PORT_B).linkPort(din.getPort(0));
		adder.getPort(Adder.PORT_CARRY_IN).linkPort(cin.getPort(0));
		
		din.setValue(WireValue.of(1, 4));
		cin.setValue(WireValue.of(0, 1));
		
		clock.startClock(4);
		
		while(true) {
			sim.step();
		}
	}
}
