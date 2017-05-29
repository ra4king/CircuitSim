package com.ra4king.circuitsimulator.simulator.components.wiring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.Utils;
import com.ra4king.circuitsimulator.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Clock extends Component {
	private static Thread currentClock;
	private static boolean clock;
	private static final Map<Clock, Object> clocks = new ConcurrentHashMap<>();
	
	private static Map<ClockChangeListener, Object> clockChangeListeners = new ConcurrentHashMap<>();
	
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
			clocks.put(this, this);
		}
	}
	
	private static long lastTickTime;
	private static long lastPrintTime;
	private static int tickCount;
	private static int lastTickCount;
	
	public static boolean getTickState() {
		return clock;
	}
	
	public static int getLastTickCount() {
		return lastTickCount;
	}
	
	public static void reset() {
		Thread clockThread = currentClock;
		stopClock();
		while(clockThread != null && clockThread.isAlive()) ;
		
		if(clock) {
			tick();
		}
	}
	
	public static void tick() {
		clock = !clock;
		WireValue clockValue = WireValue.of(clock ? 1 : 0, 1);
		clocks.forEach((clock, o) -> {
			if(clock.getCircuit() != null) {
				clock.getCircuit().getCircuitStates()
				     .forEach(state -> state.pushValue(clock.getPort(PORT), clockValue));
			}
		});
		clockChangeListeners.forEach((listener, o) -> listener.valueChanged(clockValue));
	}
	
	public static void startClock(int hertz) {
		lastTickTime = lastPrintTime = System.nanoTime();
		lastTickCount = tickCount = 0;
		
		long nanosPerTick = (long)(1e9 / (2 * hertz));
		System.out.println("Starting clock: " + hertz + " Hz = " + nanosPerTick + " nanos per tick");
		
		stopClock();
		Thread clockThread = new Thread(() -> {
			Thread thread = currentClock;
			
			while(thread != null && !thread.isInterrupted()) {
				long now = System.nanoTime();
				if(now - lastPrintTime >= 1e9) {
					lastTickCount = tickCount;
					tickCount = 0;
					lastPrintTime = now;
				}
				
				tick();
				tickCount++;
				
				lastTickTime += nanosPerTick;
				
				long diff = lastTickTime - System.nanoTime();
				if(diff >= 1e6) {
					try {
						Thread.sleep((long)(diff / 1e6));
					} catch(InterruptedException exc) {
						break;
					}
				}
			}
		});
		currentClock = clockThread;
		clockThread.start();
	}
	
	public static boolean isRunning() {
		return currentClock != null;
	}
	
	public static void stopClock() {
		if(currentClock != null) {
			System.out.println("Stopping clock");
			currentClock.interrupt();
			currentClock = null;
			lastTickCount = 0;
		}
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		circuitState.pushValue(getPort(PORT), WireValue.of(clock ? 1 : 0, 1));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
	
	public static void addChangeListener(ClockChangeListener listener) {
		clockChangeListeners.put(listener, listener);
	}
	
	public static void removeChangeListener(ClockChangeListener listener) {
		clockChangeListeners.remove(listener);
	}
	
	public interface ClockChangeListener {
		void valueChanged(WireValue value);
	}
}
