package tests;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import com.ra4king.circuitsimulator.gui.CircuitBoard;
import com.ra4king.circuitsimulator.gui.CircuitSimulator;
import com.ra4king.circuitsimulator.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsimulator.gui.peers.memory.RAMPeer;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.Subcircuit;
import com.ra4king.circuitsimulator.simulator.components.memory.RAM;

/**
 * @author Roi Atalla
 */
public class CircuitSimulatorTest {
	public static void main(String[] args) {
		try {
			CircuitSimulator simulator = new CircuitSimulator(false);
			
			simulator.createCircuit("Foo");
			simulator.renameCircuit("Foo", "Bar");
			simulator.deleteCircuit("Bar");
			
			simulator.loadCircuits(new File("CPU.sim"));
			Map<String, CircuitBoard> circuitBoards = simulator.getCircuitBoards();
			
			CircuitBoard memory = circuitBoards.get("Memory");
			if(memory == null) {
				throw new IllegalStateException("Memory circuit not found");
			}
			
			CircuitBoard cpu = circuitBoards.get("CPU");
			if(cpu == null) {
				throw new IllegalStateException("CPU circuit not found");
			}
			
			// Find the Memory Subcircuit inside the CPU circuit
			Optional<Subcircuit> memorySubcircuitOptional =
					cpu.getComponents().stream()
					   .filter(c -> c instanceof SubcircuitPeer)
					   .map(c -> ((SubcircuitPeer)c).getComponent())
					   .filter(s -> s.getSubcircuit() == memory.getCircuit())
					   .findFirst();
			
			if(!memorySubcircuitOptional.isPresent()) {
				throw new IllegalStateException("RAM subcircuit not found");
			}
			
			Subcircuit memorySubcircuit = memorySubcircuitOptional.get();
			
			// Get its internal state, with the parent state being the CPU's top-level state
			CircuitState subcircuitState = memorySubcircuit.getSubcircuitState(
					cpu.getCircuit().getTopLevelState());
			
			// Find the RAM component inside the Memory circuit
			Optional<RAM> ramOptional = memory.getComponents().stream()
			                                  .filter(c -> c instanceof RAMPeer)
			                                  .map(c -> ((RAMPeer)c).getComponent())
			                                  .findFirst();
			
			if(!ramOptional.isPresent()) {
				throw new IllegalStateException("RAM component not found");
			}
			
			RAM ram = ramOptional.get();
			
			// Now we can store
			ram.store(subcircuitState, 0x0000, 0x1234);
			
			// And load
			System.out.println(ram.load(subcircuitState, 0x0000) == 0x1234);
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		
		System.exit(0);
	}
}
