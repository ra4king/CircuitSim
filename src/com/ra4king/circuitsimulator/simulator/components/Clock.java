package com.ra4king.circuitsimulator.simulator.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.utils.Utils;

/**
 * @author Roi Atalla
 */
public class Clock extends Component {
	private static final Timer timer = new Timer("clock", true);
	private static TimerTask currentClock;
	private static boolean clock;
	private static Set<Clock> clocks = new HashSet<>();
	
	private static List<ClockChangeListener> clockChangeListeners = new ArrayList<>();
	
	public static final int PORT = 0;
	
	public Clock(String name) {
		super(name, Utils.getFilledArray(1, 1));
	}
	
	@Override
	public void setCircuit(Circuit circuit) {
		super.setCircuit(circuit);
		
		if(circuit == null) {
			clocks.remove(this);
		} else {
			clocks.add(this);
		}
	}
	
	public static void tick() {
		synchronized(timer) {
			clock = !clock;
		}
	}
	
	public static void startClock(int hertz) {
		timer.scheduleAtFixedRate(currentClock = new TimerTask() {
			@Override
			public void run() {
				tick();
				WireValue clockValue = WireValue.of(clock ? 1 : 0, 1);
				clocks.forEach(clock -> {
					clock.getCircuit().getCircuitStates()
					     .forEach(state -> state.pushValue(clock.getPort(PORT), clockValue));
				});
				for(ClockChangeListener listener : clockChangeListeners) {
					listener.valueChanged(clockValue);
				}
			}
		}, 10, hertz <= 500 ? 500 / hertz : 1);
	}
	
	public static boolean isRunning() {
		return currentClock != null;
	}
	
	public static void stopClock() {
		if(currentClock != null) {
			currentClock.cancel();
			currentClock = null;
		}
	}
	
	@Override
	public void init(CircuitState circuitState) {
		super.init(circuitState);
		
		circuitState.pushValue(getPort(PORT), WireValue.of(clock ? 1 : 0, 1));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
	
	public static void addChangeListener(ClockChangeListener listener) {
		clockChangeListeners.add(listener);
	}
	
	public static void removeChangeListener(ClockChangeListener listener) {
		clockChangeListeners.remove(listener);
	}
	
	public interface ClockChangeListener {
		void valueChanged(WireValue value);
	}
}
