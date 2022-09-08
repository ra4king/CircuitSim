package com.ra4king.circuitsim.simulator.components.debugging;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;
import com.ra4king.circuitsim.simulator.components.wiring.Clock;

/**
 * @author Charles Jenkins
 */
public class Breakpoint extends Component {
    public static final int PORT_DATA = 0;
    public static final int PORT_ENABLE = 1;
    
    private final WireValue value;
    
    public Breakpoint(String name, int bitSize, int value) {
        super(name, new int[] { bitSize, 1 });
        this.value = WireValue.of(value, bitSize);
    }

    @Override
    public void valueChanged(CircuitState state, WireValue value, int portIndex) {
        boolean dataChanged = portIndex == PORT_DATA;
        boolean enabled = state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) != State.ZERO;
        // If clock is enabled, the data value changed, and the value is the desired breakpoint value, stop the clock
        if (dataChanged && enabled && state.getLastReceived(getPort(PORT_DATA)).equals(this.value)) {
            Clock.stopClock(getCircuit().getSimulator());
        }
    }
}
