package com.ra4king.circuitsim.integrated;

import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.gates.AndGate;
import com.ra4king.circuitsim.simulator.components.gates.NotGate;
import com.ra4king.circuitsim.simulator.components.gates.OrGate;
import com.ra4king.circuitsim.simulator.components.gates.XorGate;
import com.ra4king.circuitsim.simulator.components.plexers.Multiplexer;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class MultiplexerTest {
	@Test
	public void testMultiplexer() {
		Simulator simulator = new Simulator();
		Circuit circuit = new Circuit("Multiplexer Test", simulator);
		
		Multiplexer mux = circuit.addComponent(new Multiplexer("", 4, 2));
		AndGate andGate = circuit.addComponent(new AndGate("", 4, 2));
		OrGate orGate = circuit.addComponent(new OrGate("", 4, 2));
		XorGate xorGate = circuit.addComponent(new XorGate("", 4, 2));
		NotGate notGate = circuit.addComponent(new NotGate("", 4));
		
		Pin in1 = circuit.addComponent(new Pin("A", 4, true));
		Pin in2 = circuit.addComponent(new Pin("B", 4, true));
		Pin sel = circuit.addComponent(new Pin("Sel", 2, true));
		Pin out = circuit.addComponent(new Pin("Out", 4, false));
		
		in1.getPort(Pin.PORT)
		   .linkPort(andGate.getPort(0))
		   .linkPort(orGate.getPort(0))
		   .linkPort(xorGate.getPort(0))
		   .linkPort(notGate.getOutPort());
		in2.getPort(Pin.PORT)
		   .linkPort(andGate.getPort(1))
		   .linkPort(orGate.getPort(1))
		   .linkPort(xorGate.getPort(1));
		
		andGate.getOutPort().linkPort(mux.getPort(0));
		orGate.getOutPort().linkPort(mux.getPort(1));
		xorGate.getOutPort().linkPort(mux.getPort(2));
		notGate.getOutPort().linkPort(mux.getPort(3));
		
		sel.getPort(Pin.PORT).linkPort(mux.getSelectorPort());
		out.getPort(Pin.PORT).linkPort(mux.getOutPort());
		
		in1.setValue(circuit.getTopLevelState(), WireValue.of(5, 4));
		in2.setValue(circuit.getTopLevelState(), WireValue.of(3, 4));
		simulator.stepAll();
		
		sel.setValue(circuit.getTopLevelState(), WireValue.of(0, 2));
		simulator.stepAll();
		sel.setValue(circuit.getTopLevelState(), WireValue.of(1, 2));
		simulator.stepAll();
		sel.setValue(circuit.getTopLevelState(), WireValue.of(2, 2));
		simulator.stepAll();
		sel.setValue(circuit.getTopLevelState(), WireValue.of(3, 2));
		simulator.stepAll();
	}
}
