package com.ra4king.circuitsim.simulator.components.wiring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Clock extends Component {
	private static class ClockInfo {
		private Map<Clock, Object> clocks = new ConcurrentHashMap<>();
		private Map<ClockChangeListener, Object> clockChangeListeners = new ConcurrentHashMap<>();
		
		private Thread currentClock;
		private boolean clock;
		
		private long lastTickTime;
		private long lastPrintTime;
		private int tickCount;
		private volatile int lastTickCount;
		
		void reset() {
			stopClock();
			if(clock) {
				tick();
			}
		}
		
		void tick() {
			clock = !clock;
			WireValue clockValue = WireValue.of(clock ? 1 : 0, 1);
			clocks.forEach((clock, o) -> {
				if(clock.getCircuit() != null) {
					clock.getCircuit().forEachState(state -> state.pushValue(clock.getPort(PORT), clockValue));
				}
			});
			clockChangeListeners.forEach((listener, o) -> listener.valueChanged(clockValue));
		}
		
		void startClock(int hertz) {
			lastTickTime = lastPrintTime = System.nanoTime();
			lastTickCount = tickCount = 0;
			
			final long nanosPerTick = (long)(1e9 / (2 * hertz));
			
			stopClock();
			Thread clockThread = new Thread(() -> {
				Thread thread = currentClock;
				
				while(thread != null && !thread.isInterrupted()) {
					long now = System.nanoTime();
					if(now - lastPrintTime >= 1e9) {
						lastTickCount = tickCount;
						tickCount = 0;
						lastPrintTime = now;
						lastTickTime = now;
					}
					
					tick();
					tickCount++;
					
					lastTickTime += nanosPerTick;
					
					long diff = lastTickTime - System.nanoTime();
					if(diff >= 1e6 || (tickCount >> 1) >= hertz) {
						try {
							Thread.sleep(Math.max(1, (long)(diff / 1e6)));
						} catch(InterruptedException exc) {
							break;
						}
					}
				}
			});
			
			clockThread.setName("Clock thread");
			clockThread.setDaemon(true);
			
			currentClock = clockThread;
			clockThread.start();
		}
		
		void stopClock() {
			if(currentClock != null) {
				Thread clockThread = currentClock;
				
				currentClock.interrupt();
				currentClock = null;
				lastTickCount = 0;
				
				while(clockThread.isAlive()) {
					Thread.yield();
				}
			}
		}
	}
	
	private static final Map<Simulator, ClockInfo> simulatorClocks = new ConcurrentHashMap<>();
	
	public static final int PORT = 0;
	
	public Clock(String name) {
		super(name, Utils.getFilledArray(1, 1));
	}
	
	@Override
	public void setCircuit(Circuit circuit) {
		Circuit old = getCircuit();
		super.setCircuit(circuit);
		
		if(old != null) {
			ClockInfo clock = simulatorClocks.get(old.getSimulator());
			if(clock != null) {
				clock.clocks.remove(this);
			}
		}
		
		if(circuit != null) {
			ClockInfo clock = get(circuit.getSimulator());
			clock.clocks.put(this, this);
		}
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		ClockInfo clock = get(getCircuit().getSimulator());
		circuitState.pushValue(getPort(PORT), WireValue.of(clock.clock ? 1 : 0, 1));
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
	
	private static ClockInfo get(Simulator simulator) {
		return simulatorClocks.computeIfAbsent(simulator, s -> new ClockInfo());
	}
	
	public static void tick(Simulator simulator) {
		ClockInfo clock = get(simulator);
		clock.tick();
	}
	
	public static boolean getTickState(Simulator simulator) {
		ClockInfo clock = get(simulator);
		return clock.clock;
	}
	
	public static int getLastTickCount(Simulator simulator) {
		ClockInfo clock = get(simulator);
		return clock.lastTickCount;
	}
	
	public static void reset(Simulator simulator) {
		ClockInfo clock = get(simulator);
		clock.reset();
	}
	
	public static void startClock(Simulator simulator, int hertz) {
		ClockInfo clock = get(simulator);
		clock.startClock(hertz);
	}
	
	public static boolean isRunning(Simulator simulator) {
		ClockInfo clock = get(simulator);
		return clock.currentClock != null;
	}
	
	public static void stopClock(Simulator simulator) {
		ClockInfo clock = get(simulator);
		clock.stopClock();
	}
	
	public static void addChangeListener(Simulator simulator, ClockChangeListener listener) {
		ClockInfo clock = get(simulator);
		clock.clockChangeListeners.put(listener, listener);
	}
	
	public static void removeChangeListener(Simulator simulator, ClockChangeListener listener) {
		ClockInfo clock = get(simulator);
		clock.clockChangeListeners.remove(listener);
	}
	
	public interface ClockChangeListener {
		void valueChanged(WireValue value);
	}
}
