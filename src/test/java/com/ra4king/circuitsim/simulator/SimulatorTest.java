package com.ra4king.circuitsim.simulator;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.simulator.Port.Link;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class SimulatorTest {
	private final Simulator simulator = new Simulator();
	private Circuit circuit;
	
	@BeforeEach
	public void setup() {
		circuit = mock(Circuit.class);
	}
	
	@Test
	public void testAddRemoveCircuit() {
		simulator.addCircuit(circuit);
		assertThat(simulator.getCircuits()).contains(circuit);
		
		simulator.removeCircuit(circuit);
		assertThat(simulator.getCircuits()).isEmpty();
	}
	
	@Test
	public void testValueChangedAndStep() {
		CircuitState state = mockCircuitState();
		Link link = mockLink();
		
		simulator.valueChanged(state, link);
		
		assertThat(simulator.getLinksToUpdate()).contains(new Pair<>(state, link));
		
		simulator.step();
		
		verify(state).propagateSignal(link);
	}
	
	@Test
	public void testStepAndShortCircuit() {
		CircuitState state = mockCircuitState();
		Link link = mockLink();
		
		simulator.valueChanged(state, link);
		
		ShortCircuitException shortCircuitException = new ShortCircuitException(new WireValue(), new WireValue());
		doThrow(shortCircuitException).when(state).propagateSignal(link);
		doThrow(shortCircuitException).when(state).getMergedValue(link);
		when(state.isShortCircuited(link)).thenReturn(true);
		
		ShortCircuitException exception = assertThrows(ShortCircuitException.class, simulator::step);
		assertThat(exception).isEqualTo(shortCircuitException);
	}
	
	@Test
	public void testStepAll() {
		CircuitState state1 = mockCircuitState();
		Link link1 = mockLink();
		
		CircuitState state2 = mockCircuitState();
		Link link2 = mockLink();
		
		simulator.valueChanged(state1, link1);
		
		doAnswer(invocationOnMock -> {
			simulator.valueChanged(state2, link2);
			return null;
		}).when(state1).propagateSignal(link1);
		
		simulator.stepAll();
		
		verify(state2).propagateSignal(link2);
	}
	
	@Test
	public void testStepAllOscillation() {
		CircuitState state1 = mockCircuitState();
		Link link1 = mockLink();
		
		CircuitState state2 = mockCircuitState();
		Link link2 = mockLink();
		
		simulator.valueChanged(state1, link1);
		
		doAnswer(invocationOnMock -> {
			simulator.valueChanged(state2, link2);
			return null;
		}).when(state1).propagateSignal(link1);
		
		doAnswer(invocationOnMock -> {
			simulator.valueChanged(state1, link1);
			return null;
		}).when(state2).propagateSignal(link2);
		
		assertThrows(OscillationException.class, simulator::stepAll);
	}
	
	private CircuitState mockCircuitState() {
		CircuitState state = mock(CircuitState.class);
		when(state.getCircuit()).thenReturn(circuit);
		when(circuit.containsState(state)).thenReturn(true);
		return state;
	}
	
	private Link mockLink() {
		Link link = mock(Link.class);
		when(link.getCircuit()).thenReturn(circuit);
		return link;
	}
}
