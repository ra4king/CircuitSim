package com.ra4king.circuitsim.simulator.components.debugging;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Charles Jenkins
 */
public class Breakpoint extends Component {
	public static final int PORT_DATA = 0;
	public static final int PORT_ENABLE = 1;

    private int bitSize;

	public Breakpoint(String name, int bitSize) {
		super(name, new int[] { bitSize, 1 });
		this.bitSize = bitSize;
	}

    @Override
    public void valueChanged(CircuitState state, WireValue value, int portIndex) {

        boolean enabled = state.getLastReceived(getPort(PORT_ENABLE)).getBit(0) != State.ZERO;
        switch(portIndex) {
            case PORT_ENABLE: break;
            case PORT_DATA: 
        }
        
    }
    
}
