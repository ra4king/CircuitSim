package com.ra4king.circuitsim.integrated;


import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.gui.CircuitBoard;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.ComponentPeer;
import com.ra4king.circuitsim.gui.peers.wiring.PinPeer;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Roi Atalla
 */
public class AdderGraderTest {
	private CircuitSim sim;
	
	@BeforeEach
	public void setUp() {
		sim = new CircuitSim(false);
	}
	
	@Test
	public void testAdder() throws Exception {
		Simulator simulator = sim.getSimulator();
		
		sim.loadCircuits(new File("examples/Adder.sim"));
		
		Map<String, CircuitBoard> circuitBoards = sim.getCircuitBoards();
		assertTrue(circuitBoards.containsKey("16-bit adder"), "Missing circuit named '16-bit adder'");
		
		CircuitBoard adderBoard = circuitBoards.get("16-bit adder");
		CircuitState adderState = adderBoard.getCurrentState();
		
		Pin pinA = find(
			adderBoard,
			PinPeer.class,
			"Cannot find input pin labeled 'A'",
			p -> p.getComponent().isInput() && p.getComponent().getName().equals("A")).getComponent();
		assertEquals(pinA.getBitSize(), 16, "Pin 'A' is not 16 bits.");
		
		Pin pinB = find(
			adderBoard,
			PinPeer.class,
			"Cannot find input pin labeled 'B'",
			p -> p.getComponent().isInput() && p.getComponent().getName().equals("B")).getComponent();
		assertEquals(16, pinB.getBitSize(), "Pin 'B' is not 16 bits.");
		
		Pin pinC = find(
			adderBoard,
			PinPeer.class,
			"Cannot find input pin labeled 'C'",
			p -> p.getComponent().isInput() && p.getComponent().getName().equals("C")).getComponent();
		assertEquals(1, pinC.getBitSize(), "Pin 'C' is not 1 bit.");
		
		Pin pinOut = find(
			adderBoard,
			PinPeer.class,
			"Cannot find output pin labeled 'Out'",
			p -> !p.getComponent().isInput() && p.getComponent().getName().equals("Out")).getComponent();
		assertEquals(16, pinOut.getBitSize(), "Pin 'Out' is not 16 bits.");
		
		Pin pinCarry = find(
			adderBoard,
			PinPeer.class,
			"Cannot find output pin labeled 'Carry'",
			p -> !p.getComponent().isInput() && p.getComponent().getName().equals("Carry")).getComponent();
		assertEquals(1, pinCarry.getBitSize(), "Pin 'Carry' is not 1 bit.");
		
		for (int a = 0; a < (1 << 16); a += Math.random() * 500) {
			for (int b = 0; b < (1 << 16); b += Math.random() * 500) {
				for (int c = 0; c < 2; c++) {
					testAddition(simulator, adderState, pinA, pinB, pinC, pinOut, pinCarry, a, b, c);
					// a way to initially switch to this tab and refresh
					sim.switchToCircuit(adderBoard.getCircuit(), adderState);
				}
			}
		}
	}
	
	public static <T extends ComponentPeer<?>> T find(
		CircuitBoard circuit, Class<T> type, String error, Predicate<T> test) {
		Optional<T> opt =
			circuit.getComponents().stream().filter(type::isInstance).map(type::cast).filter(test).findFirst();
		assertTrue(opt.isPresent(), error);
		return opt.get();
	}
	
	public static void testAddition(
		Simulator simulator,
		CircuitState state,
		Pin pinA,
		Pin pinB,
		Pin pinC,
		Pin pinOut,
		Pin pinCarry,
		int a,
		int b,
		int c) {
		state.pushValue(pinA.getPort(0), WireValue.of(a, 16));
		state.pushValue(pinB.getPort(0), WireValue.of(b, 16));
		state.pushValue(pinC.getPort(0), WireValue.of(c, 1));
		
		simulator.stepAll();
		
		WireValue outValue = state.getLastReceived(pinOut.getPort(0));
		WireValue carryValue = state.getLastReceived(pinCarry.getPort(0));
		int outActual = outValue.getValue();
		int outExpected = (a + b + c) % (1 << 16);
		
		int carryActual = carryValue.getValue();
		int carryExpected = ((a + b + c) >= (1 << 16) ? 1 : 0);
		
		assertWithMessage("Adder output is not correct. For inputs a=" + a + ", b=" + b + ", c=" + c)
			.that(outActual)
			.isEqualTo(outExpected);
		assertWithMessage("Adder carry is not correct. For inputs a=" + a + ", b=" + b + ", c=" + c)
			.that(carryActual)
			.isEqualTo(carryExpected);
	}
}
