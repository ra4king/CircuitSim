package com.ra4king.circuitsim.integrated;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.gates.AndGate;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class GatesTest {
	@Test
	public void testAndGate() {
		Simulator simulator = new Simulator();
		Circuit circuit = new Circuit("Gate Test", simulator);
		
		AndGate andGate = circuit.addComponent(new AndGate("", 1, 2));
		Pin in1 = circuit.addComponent(new Pin("A", 1, true));
		Pin in2 = circuit.addComponent(new Pin("B", 1, true));
		Pin out = circuit.addComponent(new Pin("Out", 1, false));
		
		andGate.getPort(0).linkPort(in1.getPort(Pin.PORT));
		andGate.getPort(1).linkPort(in2.getPort(Pin.PORT));
		andGate.getOutPort().linkPort(out.getPort(Pin.PORT));
		
		List<WireValue> valueChanges = new ArrayList<>();
		out.addChangeListener(circuit.getTopLevelState(), (pin, state, value) -> valueChanges.add(value));
		
		CircuitState state = circuit.getTopLevelState();
		
		in1.setValue(circuit.getTopLevelState(), new WireValue(1).set(0));
		simulator.stepAll();
		
		assertThat(state.getLastReceived(out.getPort())).isEqualTo(new WireValue(1).set(0));
		assertThat(valueChanges).containsExactly(new WireValue(1).set(0));
		valueChanges.clear();
		
		in2.setValue(circuit.getTopLevelState(), new WireValue(1).set(0));
		simulator.stepAll();
		
		assertThat(state.getLastReceived(out.getPort())).isEqualTo(new WireValue(1).set(0));
		assertThat(valueChanges).isEmpty();
		
		in1.setValue(circuit.getTopLevelState(), new WireValue(1).set(1));
		simulator.stepAll();
		
		assertThat(state.getLastReceived(out.getPort())).isEqualTo(new WireValue(1).set(0));
		assertThat(valueChanges).isEmpty();
		
		in2.setValue(circuit.getTopLevelState(), new WireValue(1).set(1));
		simulator.stepAll();
		
		assertThat(state.getLastReceived(out.getPort())).isEqualTo(new WireValue(1).set(1));
		assertThat(valueChanges).containsExactly(new WireValue(1).set(1));
	}
}
