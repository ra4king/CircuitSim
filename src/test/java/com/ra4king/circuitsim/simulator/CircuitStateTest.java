package com.ra4king.circuitsim.simulator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.simulator.Port.Link;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class CircuitStateTest {
	private final Simulator simulator = mock(Simulator.class);
	private final Circuit circuit = mock(Circuit.class);
	private CircuitState state;
	
	@BeforeEach
	public void setup() {
		when(circuit.getSimulator()).thenReturn(simulator);
		doAnswer(invocationOnMock -> {
			Runnable r = invocationOnMock.getArgument(0, Runnable.class);
			r.run();
			return null;
		}).when(simulator).runSync(any());
		
		state = new CircuitState(circuit);
	}
	
	@Test
	public void testLinkUnlink() {
		Link link1 = mockLink(mock(Port.class));
		Port port = mock(Port.class);
		Link link2 = mockLink(port);
		
		state.link(link1, link2);
		state.unlink(link1, port);
		
		verify(simulator, times(2)).valueChanged(state, link1);
	}
	
	@Test
	public void testPushValue() {
		Port port = mock(Port.class);
		mockLink(port);
		
		state.pushValue(port, new WireValue(1, State.ONE));
		
		verify(simulator).valueChanged(state, port);
	}
	
	private Link mockLink(Port... ports) {
		Link link = mock(Link.class);
		when(link.getBitSize()).thenReturn(1);
		when(link.getCircuit()).thenReturn(circuit);
		when(link.getParticipants()).thenReturn(new HashSet<>(Arrays.asList(ports)));
		for (Port port : ports) {
			when(port.getLink()).thenReturn(link);
		}
		return link;
	}
}
