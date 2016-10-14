package com.ra4king.circuitsimulator.tests;

import com.ra4king.circuitsimulator.Circuit;
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
		Simulator simulator = new Simulator();
		Circuit circuit = new Circuit(simulator);
		
		Clock clock = new Clock(circuit, "");
		Register register = new Register(circuit, "", 32);
		Adder adder = new Adder(circuit, "", 32);
		Pin din = new Pin(circuit, "Din", 32);
		Pin cin = new Pin(circuit, "Cin", 1);
		Pin out = new Pin(circuit, "Out", 32);
		
		register.getPort(Register.PORT_IN).linkPort(adder.getPort(Adder.PORT_OUT));
		register.getPort(Register.PORT_CLK).linkPort(clock.getPort(0));
		adder.getPort(Adder.PORT_A).linkPort(register.getPort(Register.PORT_OUT)).linkPort(out.getPort(0));
		adder.getPort(Adder.PORT_B).linkPort(din.getPort(0));
		adder.getPort(Adder.PORT_CARRY_IN).linkPort(cin.getPort(0));
		
		din.setValue(circuit.getTopLevelState(), WireValue.of(1, 32));
		cin.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		
		Clock.startClock(4);
		
		while(true) {
			simulator.step();
		}
	}
}
