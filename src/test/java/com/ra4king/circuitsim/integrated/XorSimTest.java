package com.ra4king.circuitsim.integrated;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.gui.CircuitBoard;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.peers.wiring.PinPeer;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Patrick Tam
 */
public class XorSimTest {
	@Test
	public void test() throws Exception {
		CircuitSim simulator = new CircuitSim(false);
		simulator.loadCircuits(new File("examples/Xor.sim"));
		
		Simulator sim = simulator.getSimulator();
		
		Map<String, CircuitBoard> circuitBoards = simulator.getCircuitBoards();
		
		CircuitBoard xor = circuitBoards.get("xor");
		
		Optional<Pin> ain = xor
			.getComponents()
			.stream()
			.filter(c -> c instanceof PinPeer)
			.map(c -> ((PinPeer)c).getComponent())
			.filter(p -> p.isInput() && p.getName().equals("a"))
			.findFirst();
		
		if (ain.isEmpty()) {
			throw new IllegalStateException("a input does not exist in circuit");
		}
		
		Optional<Pin> bin = xor
			.getComponents()
			.stream()
			.filter(c -> c instanceof PinPeer)
			.map(c -> ((PinPeer)c).getComponent())
			.filter(p -> p.isInput() && p.getName().equals("b"))
			.findFirst();
		
		if (bin.isEmpty()) {
			throw new IllegalStateException("b input does not exist in circuit");
		}
		
		Optional<Pin> cout = xor
			.getComponents()
			.stream()
			.filter(c -> c instanceof PinPeer)
			.map(c -> ((PinPeer)c).getComponent())
			.filter(p -> !p.isInput() && p.getName().equals("c"))
			.findFirst();
		
		if (cout.isEmpty()) {
			throw new IllegalStateException("c output does not exist in circuit");
		}
		
		final WireValue ONE = WireValue.of(1, 1);
		final WireValue ZERO = WireValue.of(0, 1);
		
		// 0^0 = 0
		ain.get().setValue(xor.getCurrentState(), ONE);
		bin.get().setValue(xor.getCurrentState(), ZERO);
		sim.stepAll();
		assertThat(xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue()).isEqualTo(1);
		
		// 0^1 = 1
		ain.get().setValue(xor.getCurrentState(), ZERO);
		bin.get().setValue(xor.getCurrentState(), ONE);
		sim.stepAll();
		assertThat(xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue()).isEqualTo(1);
		
		
		// 1^0 = 1
		ain.get().setValue(xor.getCurrentState(), ONE);
		bin.get().setValue(xor.getCurrentState(), ZERO);
		sim.stepAll();
		assertThat(xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue()).isEqualTo(1);
		
		
		// 1^1 = 0
		ain.get().setValue(xor.getCurrentState(), ONE);
		bin.get().setValue(xor.getCurrentState(), ONE);
		sim.stepAll();
		assertThat(xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue()).isEqualTo(0);
	}
}
