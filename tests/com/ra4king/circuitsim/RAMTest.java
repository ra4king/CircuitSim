package com.ra4king.circuitsim;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.memory.RAM;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class RAMTest {
	public static void main(String[] args) {
		Simulator sim = new Simulator();
		
		Circuit circuit = new Circuit("RAM Test", sim);
		RAM ram = circuit.addComponent(new RAM("", 8, 8, false));
		Pin address = circuit.addComponent(new Pin("Address", 8, true));
		Pin clk = circuit.addComponent(new Pin("clk", 1, true));
		Pin data = circuit.addComponent(new Pin("data", 8, false));
		Pin load = circuit.addComponent(new Pin("load", 1, false));
		
		ram.getPort(RAM.PORT_ADDRESS).linkPort(address.getPort(Pin.PORT));
		ram.getPort(RAM.PORT_CLK).linkPort(clk.getPort(Pin.PORT));
		ram.getPort(RAM.PORT_DATA).linkPort(data.getPort(Pin.PORT));
		ram.getPort(RAM.PORT_LOAD).linkPort(load.getPort(Pin.PORT));
		
		int sum = 0;
		for(int i = 0; i < 100; i++) {
			ram.store(circuit.getTopLevelState(), i, i * 2 + 5);
			sum += i * 2;
		}
		System.out.println("Expected sum: " + sum);
		
		final WireValue ONE = WireValue.of(1, 1);
		final WireValue ZERO = WireValue.of(0, 1);
		
		load.setValue(circuit.getTopLevelState(), ZERO);
		
		address.setValue(circuit.getTopLevelState(), WireValue.of(100, 8));
		data.setValue(circuit.getTopLevelState(), WireValue.of(250, 8));
		clk.setValue(circuit.getTopLevelState(), ONE);
		sim.stepAll();
		
		load.setValue(circuit.getTopLevelState(), ONE);
		data.setValue(circuit.getTopLevelState(), new WireValue(8));
		sim.stepAll();
		
		for(int i = 0; i < 101; i++) {
			address.setValue(circuit.getTopLevelState(), WireValue.of(i, 8));
			sim.stepAll();
		}
	}
}
