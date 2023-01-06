package com.ra4king.circuitsim.integrated;

import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.arithmetic.Adder;
import com.ra4king.circuitsim.simulator.components.memory.Register;
import com.ra4king.circuitsim.simulator.components.wiring.Clock;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class ClockTest {
	@Test
	public void testClock() {
		Simulator simulator = new Simulator();
		Circuit circuit = new Circuit("Clock Test", simulator);
		
		Clock clock = circuit.addComponent(new Clock("Clock"));
		Register register = circuit.addComponent(new Register("Register", 32));
		Adder adder = circuit.addComponent(new Adder("Adder", 32));
		Pin din = circuit.addComponent(new Pin("Din", 32, true));
		Pin cin = circuit.addComponent(new Pin("Cin", 1, true));
		Pin out = circuit.addComponent(new Pin("Out", 32, false));
		
		register.getPort(Register.PORT_IN).linkPort(adder.getPort(Adder.PORT_OUT));
		register.getPort(Register.PORT_CLK).linkPort(clock.getPort(Clock.PORT));
		adder.getPort(Adder.PORT_A).linkPort(register.getPort(Register.PORT_OUT)).linkPort(out.getPort(Pin.PORT));
		adder.getPort(Adder.PORT_B).linkPort(din.getPort(Pin.PORT));
		adder.getPort(Adder.PORT_CARRY_IN).linkPort(cin.getPort(Pin.PORT));
		
		out.addChangeListener(circuit.getTopLevelState(), (pin, state, value) -> {
			int v = value.getValue();
			if (v % 100000 == 0) {
				System.out.println(
					value + " - " + value.getValue() + " - " + Clock.getLastTickCount(simulator) + " Hz");
			}
		});
		
		din.setValue(circuit.getTopLevelState(), WireValue.of(1, 32));
		cin.setValue(circuit.getTopLevelState(), WireValue.of(0, 1));
		simulator.stepAll();
		
		Clock.addChangeListener(simulator, value -> simulator.stepAll());
		Clock.startClock(simulator, 1000000);
	}
}
