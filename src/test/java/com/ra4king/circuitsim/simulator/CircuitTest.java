package com.ra4king.circuitsim.simulator;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.simulator.Circuit.CircuitChangeListener;

/**
 * @author Roi Atalla
 */
public class CircuitTest {
	private final Simulator simulator = mock(Simulator.class);
	private Circuit circuit;
	
	@BeforeEach
	public void setup() {
		doAnswer(invocationOnMock -> {
			Runnable r = invocationOnMock.getArgument(0, Runnable.class);
			r.run();
			return null;
		}).when(simulator).runSync(any());
		
		circuit = new Circuit("", simulator);
	}
	
	@Test
	public void testAddComponent() {
		CircuitChangeListener listener = mock(CircuitChangeListener.class);
		circuit.addListener(listener);
		
		Component component = mock(Component.class);
		circuit.addComponent(component);
		
		assertThat(circuit.getComponents().iterator().next()).isEqualTo(component);
		
		verify(component).setCircuit(circuit);
		verify(component).init(circuit.getTopLevelState(), null);
		verify(listener).circuitChanged(circuit, component, true);
	}
	
	@Test
	public void testAddComponent_alreadyAdded() {
		Component component = mock(Component.class);
		when(component.getCircuit()).thenReturn(mock(Circuit.class));
		
		assertThrows(IllegalArgumentException.class, () -> circuit.addComponent(component));
	}
	
	@Test
	public void testUpdateComponent() {
		CircuitState state = mock(CircuitState.class);
		CircuitChangeListener listener = mock(CircuitChangeListener.class);
		Component oldComponent = mock(Component.class);
		circuit.addState(state);
		circuit.addListener(listener);
		circuit.addComponent(oldComponent);
		
		Object property = mock(Object.class);
		when(state.getComponentProperty(oldComponent)).thenReturn(property);
		
		Component newComponent = mock(Component.class);
		Runnable runnable = mock(Runnable.class);
		doAnswer((invocationOnMock) -> {
			assertThat(circuit.getComponents()).isEmpty();
			assertThat(oldComponent.getCircuit()).isNull();
			return null;
		}).when(runnable).run();
		
		circuit.updateComponent(oldComponent, newComponent, runnable);
		
		verify(state).ensureUnlinked(oldComponent, false);
		verify(oldComponent).uninit(circuit.getTopLevelState());
		verify(oldComponent).uninit(state);
		verify(oldComponent).setCircuit(null);
		verify(listener).circuitChanged(circuit, oldComponent, false);
		
		verify(runnable).run();
		
		verify(newComponent).setCircuit(circuit);
		verify(oldComponent).init(circuit.getTopLevelState(), null);
		verify(newComponent).init(state, property);
		verify(listener).circuitChanged(circuit, newComponent, true);
	}
	
	@Test
	public void testRemoveComponent() {
		CircuitState state = mock(CircuitState.class);
		CircuitChangeListener listener = mock(CircuitChangeListener.class);
		Component component = mock(Component.class);
		circuit.addState(state);
		circuit.addListener(listener);
		circuit.addComponent(component);
		
		circuit.removeComponent(component);
		
		assertThat(circuit.getComponents()).isEmpty();
		
		verify(state).ensureUnlinked(component, true);
		verify(component).uninit(circuit.getTopLevelState());
		verify(component).uninit(state);
		verify(component).setCircuit(null);
		verify(listener).circuitChanged(circuit, component, false);
	}
}
