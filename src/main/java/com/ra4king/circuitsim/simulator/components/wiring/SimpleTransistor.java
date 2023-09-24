package com.ra4king.circuitsim.simulator.components.wiring;

import com.ra4king.circuitsim.simulator.CircuitState;
import com.ra4king.circuitsim.simulator.Component;
import com.ra4king.circuitsim.simulator.SimulationException;
import com.ra4king.circuitsim.simulator.WireValue;
import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla, Austin Adams, and Tom Conte
 * <p>
 * Abridged related email thread explaining the behavior of Transistors (read
 * top-to-bottom):
 *
 * <blockquote>
 * From: Adams, Austin J
 * Sent: Tuesday, December 11, 2018 2:14:58 PM
 * To: Conte, Thomas
 * Subject: Connecting a P-type to ground
 * <p>
 * How should a GUI digital circuit simulator like Brandonsim respond to a
 * P-type transistor being connected to ground,  or a N-type being connected to
 * power? In class you said "physics" but I'm still curious.
 * <hr>
 * From: Conte, Thomas
 * Sent: Tuesday, December 11, 2018 2:59:29 PM
 * To: Adams, Austin J
 * Subject: RE: Connecting a P-type to ground
 * <p>
 * In what context do you want it to behave?
 * Brandonsim is a logic level simulator, correct?
 * <hr>
 * From: Adams, Austin J
 * Sent: Tuesday, December 11, 2018 3:01:21 PM
 * To: Conte, Thomas
 * Subject: Re: Connecting a P-type to ground
 * <p>
 * Yeah, but we have them create NAND/NOR/NOT gates in it with transistors
 * (using constant 0 as ground and constant 1 as power).
 * <hr>
 * From: Conte, Thomas
 * Sent: Tuesday, December 11, 2018 3:44 PM
 * To: Adams, Austin J
 * Subject: Re: Connecting a P-type to ground
 * <p>
 * Buckle up! This will be a ride...
 * <p>
 * Let's take an n-type first.
 * In class I present it as the voltage on the gate is what turns the transistor
 * on. It's actually the difference between the gate voltage and the source
 * voltage. If you're an EE, you know that current runs from positive voltage
 * source to ground. So the "source" side of an n-type is the side that is attached
 * to a positive voltage.
 * <p>
 * An n-type will turn on (conduct) if V_gate - V_source &gt; 4 Volts (for most
 * CMOS technologies this value is usually close to VDD but not quite VDD).
 * If V_source is tied to VDD (= 5V), then there's no way that the n-type will ever
 * turn on because V_gate - V_source will always be either negative or 0 Volts.
 * <p>
 * Thus the n-type appears as an open circuit regardless of what the gate voltage
 * is.
 * <p>
 * On the flip side, a P-type turns on when V_gate - V_source &lt; -4V
 * [Editor's note: I've flipped the inequality here from &gt; to &lt; due to
 * Tom having a possible dyslexic moment]
 * <p>
 * Consider what happens when a P-type is hooked up correctly. When V_gate = 5V,
 * then 5V - V_source = 5V - 5V = 0V, which is greater than -4V, so the gate is off
 * (the switch is open). When V_gate = 0V, then 0V - 5V = -5V, which is &lt; -4V,
 * so the gate is on (the switch is closed).
 * [Editor's note: Another inequality flip: changed -5V &gt; -4V to -5V &lt; -4V]
 * <p>
 * This makes interpreting what happens when a P-type is hooked to ground a little
 * tricky because the issue is really what the P-type's source is connected to.
 * <p>
 * If they do the common mistake of putting the N-type in the top and the P-type on
 * the bottom, let's call this an "inverted inverter" configuration, that means
 * that the top part (the n-type) of the "inverted inverter" would be always
 * disconnected from VDD. The voltage for the source of the P-type would depend on
 * the voltage at the "inverted inverter's" output (since the current would in
 * essence "run backwards" from output to input in this "inverted inverter.")
 * <p>
 * That's harder to figure out without knowing the whole circuit.
 * <p>
 * Here's one truth though: if the P-type is hooked from VDD to GND with nothing
 * in between, it will provide a short circuit and go "poof". <strong>What you
 * can do is enforce the rule for N-types hooked to VDD are always off and just
 * throw an error for P-types that are not hooked to VDD.</strong> [emphasis added]
 * </blockquote>
 */
public class SimpleTransistor extends Component {
	public static final int PORT_SOURCE = 0;
	public static final int PORT_GATE = 1;
	public static final int PORT_DRAIN = 2;
	
	private static final WireValue Z_VALUE = new WireValue(1);
	
	private boolean isIllegallyWired;
	private boolean isPType;
	
	public SimpleTransistor(String name, boolean isPType) {
		super(name, new int[] { 1, 1, 1 });
		
		this.isIllegallyWired = false;
		this.isPType = isPType;
	}
	
	public boolean getIllegallyWired() {
		return this.isIllegallyWired;
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		if (portIndex == PORT_DRAIN) {
			return;
		}
		
		State enableBit = isPType ? State.ZERO : State.ONE;
		State sourceBit = state.getLastReceived(getPort(PORT_SOURCE)).getBit(0);
		boolean nTypeOk = isPType || sourceBit != State.ONE;
		boolean pTypeOk = !isPType || sourceBit != State.ZERO;
		this.isIllegallyWired = !nTypeOk || !pTypeOk;
		
		if (pTypeOk && nTypeOk && state.getLastReceived(getPort(PORT_GATE)).getBit(0) == enableBit) {
			state.pushValue(getPort(PORT_DRAIN), new WireValue(sourceBit));
		} else {
			state.pushValue(getPort(PORT_DRAIN), Z_VALUE);
			
			if (!nTypeOk) {
				throw new SimulationException("N-type transistor must not be connected to logic-level high!");
			}
			if (!pTypeOk) {
				throw new SimulationException("P-type transistor must not be connected to ground!");
			}
		}
	}
}
