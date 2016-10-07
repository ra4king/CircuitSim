package com.ra4king.circuitsimulator.components;

import java.util.Timer;
import java.util.TimerTask;

import com.ra4king.circuitsimulator.Component;
import com.ra4king.circuitsimulator.Simulator;
import com.ra4king.circuitsimulator.Utils;
import com.ra4king.circuitsimulator.WireValue;

/**
 * @author Roi Atalla
 */
public class Clock extends Component {
	private Timer timer;
	private TimerTask currentClock;
	private boolean clock;
	
	public Clock(Simulator simulator, String name) {
		super(simulator, "Clock " + name, Utils.getFilledArray(1, 1));
		
		timer = new Timer("clock", true);
	}
	
	public void startClock(int hertz) {
		timer.scheduleAtFixedRate(currentClock = new TimerTask() {
			@Override
			public void run() {
				clock = !clock;
				ports[0].pushValue(WireValue.of(clock ? 1 : 0, 1));
			}
		}, 10, hertz <= 1000 ? 1000 / hertz : 1);
	}
	
	public void stopClock() {
		if(currentClock != null) {
			currentClock.cancel();
		}
	}
	
	@Override
	public void valueChanged(WireValue value, int portIndex) {}
}
