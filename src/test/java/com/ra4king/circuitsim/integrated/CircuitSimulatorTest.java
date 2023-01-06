package com.ra4king.circuitsim.integrated;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.gui.CircuitBoard;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsim.gui.peers.memory.RAMPeer;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.components.Subcircuit;
import com.ra4king.circuitsim.simulator.components.memory.RAM;
import com.ra4king.circuitsim.simulator.components.wiring.Clock;

/**
 * @author Roi Atalla
 */
public class CircuitSimulatorTest {
	@Test
	public void testLoadCpu() throws Exception {
		final boolean openWindow = true;
		
		CircuitSim simulator = new CircuitSim(openWindow);
		
		if (openWindow) {
			Thread.sleep(1000);
		}
		
		simulator.createCircuit("Foo");
		
		if (openWindow) {
			Thread.sleep(1000);
		}
		
		simulator.renameCircuit("Foo", "Bar");
		
		if (openWindow) {
			Thread.sleep(1000);
		}
		
		simulator.deleteCircuit("Bar");
		
		simulator.loadCircuits(new File("examples/CPU.sim"));
		Map<String, CircuitBoard> circuitBoards = simulator.getCircuitBoards();
		
		if (openWindow) {
			Clock.tick(simulator.getSimulator());
			
			Thread.sleep(1000);
		}
		
		CircuitBoard memory = circuitBoards.get("Memory");
		if (memory == null) {
			throw new IllegalStateException("Memory circuit not found");
		}
		
		CircuitBoard cpu = circuitBoards.get("CPU");
		if (cpu == null) {
			throw new IllegalStateException("CPU circuit not found");
		}
		
		// Find the Memory Subcircuit inside the CPU circuit
		Optional<Subcircuit> memorySubcircuitOptional = cpu
			.getComponents()
			.stream()
			.filter(c -> c instanceof SubcircuitPeer)
			.map(c -> ((SubcircuitPeer)c).getComponent())
			.filter(s -> s.getSubcircuit() == memory.getCircuit())
			.findFirst();
		
		if (memorySubcircuitOptional.isEmpty()) {
			throw new IllegalStateException("RAM subcircuit not found");
		}
		
		Subcircuit memorySubcircuit = memorySubcircuitOptional.get();
		
		// Get its internal state, with the parent state being the CPU's top-level state
		CircuitState subcircuitState = memorySubcircuit.getSubcircuitState(cpu.getCircuit().getTopLevelState());
		
		// Find the RAM component inside the Memory circuit
		Optional<RAM> ramOptional = memory
			.getComponents()
			.stream()
			.filter(c -> c instanceof RAMPeer)
			.map(c -> ((RAMPeer)c).getComponent())
			.findFirst();
		
		if (ramOptional.isEmpty()) {
			throw new IllegalStateException("RAM component not found");
		}
		
		RAM ram = ramOptional.get();
		
		// Now we can store
		ram.store(subcircuitState, 0x0000, 0x1234);
		
		// And load
		assertThat(ram.load(subcircuitState, 0x0000)).isEqualTo(0x1234);
	}
}
