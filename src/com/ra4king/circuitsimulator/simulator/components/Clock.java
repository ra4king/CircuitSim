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
import com.ra4king.circuitsimulator.simulator.Utils;
import com.ra4king.circuitsimulator.simulator.WireValue;

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
	
	private static long lastTime;
	private static int tickCount;
	
	public static boolean getTickState() {
		return clock;
	}
	
	public static void tick() {
		long now = System.nanoTime();
		if(now - lastTime >= 1e9) {
			System.out.println((tickCount >> 1) + " Hz");
			tickCount = 0;
			lastTime = now;
		}
		
		tickCount++;
		
		clock = !clock;
		WireValue clockValue = WireValue.of(clock ? 1 : 0, 1);
		clocks.forEach(clock ->
				               clock.getCircuit().getCircuitStates()
				                    .forEach(state -> state.pushValue(clock.getPort(PORT), clockValue)));
		for(ClockChangeListener listener : clockChangeListeners) {
			listener.valueChanged(clockValue);
		}
	}
	
	public static void startClock(int hertz) {
		lastTime = System.nanoTime();
		tickCount = 0;
		
		stopClock();
		timer.scheduleAtFixedRate(currentClock = new TimerTask() {
			@Override
			public void run() {
				tick();
			}
		}, 10, hertz <= 500 ? Math.round(500.0 / hertz) : 1);
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
