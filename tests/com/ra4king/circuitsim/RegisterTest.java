package com.ra4king.circuitsim;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.memory.Register;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class RegisterTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		Circuit circuit = new Circuit("Register Test", sim);
		
		Register register = circuit.addComponent(new Register("", 4));
		Pin dataIn = circuit.addComponent(new Pin("In", 4, true));
		Pin enable = circuit.addComponent(new Pin("Enable", 1, true));
		Pin clk = circuit.addComponent(new Pin("Clk", 1, true));
		Pin dataOut = circuit.addComponent(new Pin("Out", 4, false));
		
		register.getPort(Register.PORT_IN).linkPort(dataIn.getPort(Pin.PORT));
		register.getPort(Register.PORT_ENABLE).linkPort(enable.getPort(Pin.PORT));
		register.getPort(Register.PORT_CLK).linkPort(clk.getPort(Pin.PORT));
		register.getPort(Register.PORT_OUT).linkPort(dataOut.getPort(Pin.PORT));
		
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
