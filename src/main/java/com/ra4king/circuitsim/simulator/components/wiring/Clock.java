package com.ra4king.circuitsim.simulator.components.wiring;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ra4king.circuitsim.simulator.Circuit;
import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.Simulator;
import com.ra4king.circuitsim.simulator.Utils;
import com.ra4king.circuitsim.simulator.WireValue;

import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Roi Atalla
 */
public class Clock extends Component {
	public static class EnabledInfo {
		private final boolean enabled;
		private final int hertz;
		
		public EnabledInfo(boolean enabled, int hertz) {
			this.enabled = enabled;
			this.hertz = hertz;
		}
		
		public boolean getEnabled() {
			return enabled;
		}
		
		public int getHertz() {
			return hertz;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			
			if (!(o instanceof EnabledInfo)) {
				return false;
			}
			
			EnabledInfo e = (EnabledInfo)o;
			
			return enabled == e.enabled && hertz == e.hertz;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(enabled, hertz);
		}
	}
	
	private static class ClockInfo {
		private final Map<Clock, Object> clocks = new ConcurrentHashMap<>();
		private final Map<ClockChangeListener, Object> clockChangeListeners = new ConcurrentHashMap<>();
		
		private static class InternalClockInfo {
			private final Thread thread;
			private final AtomicBoolean enabled = new AtomicBoolean(true);
			
			InternalClockInfo(Thread thread) {
				this.thread = thread;
			}
		}
		
		private final Simulator simulator;
		
		private InternalClockInfo currentClock;
		private final SimpleObjectProperty<Clock.EnabledInfo>
			clockEnabled =
			new SimpleObjectProperty<>(new Clock.EnabledInfo(false, 0));
		private boolean clock;
		
		private long lastTickTime;
		private long lastPrintTime;
		private int tickCount;
		private volatile int lastTickCount;
		
		private ClockInfo(Simulator simulator) {
			this.simulator = simulator;
			
			clockEnabled.addListener((obs, oldValue, newValue) -> {
				if (newValue.enabled) {
					startClock(newValue.getHertz());
				} else {
					stopClock(/* waitForClockToStop= */ false);
				}
			});
		}
		
		void reset() {
			stopClock(/* waitForClockToStop= */ true);
			synchronized (this) {
				if (clock) {
					tick();
				}
			}
		}
		
		synchronized void tick() {
			clock = !clock;
			WireValue clockValue = WireValue.of(clock ? 1 : 0, 1);
			
			simulator.runSync(() -> clocks.forEach((clock, o) -> {
				Circuit circuit = clock.getCircuit();
				if (circuit != null) {
					circuit.forEachState(state -> state.pushValue(clock.getPort(PORT), clockValue));
				}
			}));
			clockChangeListeners.forEach((listener, o) -> listener.valueChanged(clockValue));
		}
		
		synchronized void startClock(int hertz) {
			if (currentClock != null) {
				stopClock(/* waitForClockToStop= */ false);
			}
			
			lastTickTime = lastPrintTime = System.nanoTime();
			lastTickCount = tickCount = 0;
			
			final long nanosPerTick = (long)(1e9 / (2L * hertz));
			
			Thread clockThread = new Thread(() -> {
				InternalClockInfo currentClock = this.currentClock;
				if (currentClock == null || !Thread.currentThread().equals(currentClock.thread)) {
					return;
				}
				
				while (currentClock.enabled.get()) {
					long now = System.nanoTime();
					if (now - lastPrintTime >= 1e9) {
						lastTickCount = tickCount;
						tickCount = 0;
						lastPrintTime = now;
						lastTickTime = now;
					}
					
					tick();
					tickCount++;
					
					lastTickTime += nanosPerTick;
					
					long diff = lastTickTime - System.nanoTime();
					if (diff >= 1e6 || (tickCount >> 1) >= hertz) {
						try {
							Thread.sleep(Math.max(1, (long)(diff / 1e6)));
						} catch (InterruptedException exc) {
							break;
						}
					}
				}
			});
			
			clockThread.setName("Clock thread");
			clockThread.setDaemon(true);
			
			currentClock = new InternalClockInfo(clockThread);
			clockThread.start();
		}
		
		void stopClock(boolean waitForClockToStop) {
			if (currentClock != null) {
				InternalClockInfo clock = null;
				synchronized (this) {
					if (currentClock != null) {
						clock = currentClock;
						currentClock = null;
						clock.enabled.set(false);
					}
				}
				
				if (clock != null && waitForClockToStop) {
					boolean isClockThread = Thread.currentThread().equals(clock.thread);
					// Wait for the clock thread to die if we're not already in the clock thread.
					while (clock.thread.isAlive() && !isClockThread) {
						Thread.yield();
					}
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
		
		if (old != null) {
			ClockInfo clock = simulatorClocks.get(old.getSimulator());
			if (clock != null) {
				clock.clocks.remove(this);
			}
		}
		
		if (circuit != null) {
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
		return simulatorClocks.computeIfAbsent(simulator, ClockInfo::new);
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
		clock.clockEnabled.set(new Clock.EnabledInfo(true, hertz));
	}
	
	public static boolean isRunning(Simulator simulator) {
		ClockInfo clock = get(simulator);
		return clock.clockEnabled.get().enabled;
	}
	
	public static SimpleObjectProperty<Clock.EnabledInfo> clockEnabledProperty(Simulator simulator) {
		ClockInfo clock = get(simulator);
		return clock.clockEnabled;
	}
	
	public static void stopClock(Simulator simulator) {
		ClockInfo clock = get(simulator);
		clock.clockEnabled.set(new Clock.EnabledInfo(false, 0));
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
