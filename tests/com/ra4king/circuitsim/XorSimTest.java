package com.ra4king.circuitsim;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import com.ra4king.circuitsim.gui.CircuitBoard;
import com.ra4king.circuitsim.gui.CircuitSim;
import com.ra4king.circuitsim.gui.peers.SubcircuitPeer;
import com.ra4king.circuitsim.gui.peers.memory.RAMPeer;
import com.ra4king.circuitsim.gui.peers.wiring.PinPeer;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.components.Subcircuit;
import com.ra4king.circuitsim.simulator.components.memory.RAM;
import com.ra4king.circuitsim.simulator.components.wiring.Clock;
import com.ra4king.circuitsim.simulator.components.wiring.Pin;

/**
 * @author Patrick Tam
 */
public class XorSimTest {
    public static void main(String[] args) {

        try {
            CircuitSim simulator = new CircuitSim(false);

            simulator.loadCircuits(new File("Xor.sim"));

            Simulator sim = simulator.getSimulator();

            Map<String, CircuitBoard> circuitBoards = simulator.getCircuitBoards();

            CircuitBoard xor = circuitBoards.get("xor");

            Optional<Pin> ain =
                xor.getComponents().stream()
                    .filter(c -> c instanceof PinPeer)
                    .map(c -> ((PinPeer)c).getComponent())
                    .filter(p -> p.isInput() && p.getName().equals("a"))
                    .findFirst();

            if(!ain.isPresent()) {
                throw new IllegalStateException("a input does not exist in circuit");
            }

            Optional<Pin> bin =
                    xor.getComponents().stream()
                            .filter(c -> c instanceof PinPeer)
                            .map(c -> ((PinPeer)c).getComponent())
                            .filter(p -> p.isInput() && p.getName().equals("b"))
                            .findFirst();

            if(!bin.isPresent()) {
                throw new IllegalStateException("b input does not exist in circuit");
            }

            Optional<Pin> cout =
                    xor.getComponents().stream()
                            .filter(c -> c instanceof PinPeer)
                            .map(c -> ((PinPeer)c).getComponent())
                            .filter(p -> !p.isInput() && p.getName().equals("c"))
                            .findFirst();

            if(!cout.isPresent()) {
                throw new IllegalStateException("c output does not exist in circuit");
            }

            final WireValue ONE = WireValue.of(1, 1);
            final WireValue ZERO = WireValue.of(0, 1);

            // 0^0 = 0
            ain.get().setValue(xor.getCurrentState(), ONE);
            bin.get().setValue(xor.getCurrentState(), ZERO);
            sim.stepAll();
            assert xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue() == 1;

            // 0^1 = 1
            ain.get().setValue(xor.getCurrentState(), ZERO);
            bin.get().setValue(xor.getCurrentState(), ONE);
            sim.stepAll();
            assert xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue() == 1;


            // 1^0 = 1
            ain.get().setValue(xor.getCurrentState(), ONE);
            bin.get().setValue(xor.getCurrentState(), ZERO);
            sim.stepAll();
            assert xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue() == 1;


            // 1^1 = 0
            ain.get().setValue(xor.getCurrentState(), ONE);
            bin.get().setValue(xor.getCurrentState(), ONE);
            sim.stepAll();
            assert xor.getCurrentState().getLastReceived(cout.get().getPort(Pin.PORT)).getValue() == 0;

            System.out.println("remember that asserts have to be enabled for this to be a valid test\n" +
                    " passed Xor.sim passed tests!");

        } catch(Exception exc) {
            exc.printStackTrace();
        }

        System.exit(0);
    }
}
