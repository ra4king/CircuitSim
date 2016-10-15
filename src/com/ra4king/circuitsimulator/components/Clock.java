package com.ra4king.circuitsimulator.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ra4king.circuitsimulator.CircuitState;
import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.WireValue;
import com.ra4king.circuitsimulator.utils.Utils;

/**
 * @author Roi Atalla
 */
public class Clock extends Component {
	private static final Timer timer = new Timer("clock", true);
	private static TimerTask currentClock;
	private static boolean clock;
	private static List<Clock> clocks = new ArrayList<>();
	
	public Clock(String name) {
		super("Clock " + name, Utils.getFilledArray(1, 1));
		
		clocks.add(this);
	}
	
	public static void startClock(int hertz) {
		timer.scheduleAtFixedRate(currentClock = new TimerTask() {
			@Override
			public void run() {
				clock = !clock;
				WireValue clockValue = WireValue.of(clock ? 1 : 0, 1);
				clocks.forEach(clock1 -> clock1.getCircuit().getCircuitStates().stream()
						                         .forEach(state -> state.pushValue(clock1.getPort(0), clockValue)));
			}
		}, 10, hertz <= 500 ? 500 / hertz : 1);
	}
	
	public static void stopClock() {
		if(currentClock != null) {
			currentClock.cancel();
			currentClock = null;
		}
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {}
}
