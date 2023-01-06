package com.ra4king.circuitsim.simulator;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import com.ra4king.circuitsim.simulator.WireValue.State;

/**
 * @author Roi Atalla
 */
public class WireValueTest {
	@Test
	public void testBasic() {
		WireValue value = new WireValue(10);
		assertThat(value.getBitSize()).isEqualTo(10);
		assertThat(WireValueUtils.allBitsEqualTo(value, State.X)).isTrue();
		
		value = new WireValue(15, State.ONE);
		assertThat(value.getBitSize()).isEqualTo(15);
		assertThat(WireValueUtils.allBitsEqualTo(value, State.ONE)).isTrue();
	}
	
	@Test
	public void testOf() {
		WireValue value = WireValue.of(0b10101, 5);
		assertThat(WireValueUtils.allBitsEqualTo(value,
		                                         State.ONE,
		                                         State.ZERO,
		                                         State.ONE,
		                                         State.ZERO,
		                                         State.ONE)).isTrue();
	}
	
	@Test
	public void testMerge() {
		WireValue value1 = new WireValue(4);
		WireValue value2 = new WireValue(4);
		
		value1.setBit(0, State.ONE);
		value2.setBit(1, State.ZERO);
		value1.setBit(2, State.ZERO);
		value2.setBit(2, State.ZERO);
		
		WireValue merge = new WireValue(value1).merge(value2);
		assertThat(WireValueUtils.allBitsEqualTo(merge, State.ONE, State.ZERO, State.ZERO, State.X)).isTrue();
	}
	
	@Test
	public void testMergeException_differentLength() {
		WireValue value1 = new WireValue(3);
		WireValue value2 = new WireValue(4);
		
		assertThrows(IllegalArgumentException.class, () -> value1.merge(value2));
	}
	
	@Test
	public void testMergeException_shortCircuit() {
		WireValue value1 = new WireValue(1);
		WireValue value2 = new WireValue(1);
		
		value1.setBit(0, State.ONE);
		value2.setBit(0, State.ZERO);
		
		assertThrows(ShortCircuitException.class, () -> value1.merge(value2));
	}
	
	@Test
	public void testValue() {
		WireValue value = WireValue.of(15, 5);
		assertThat(value.getValue()).isEqualTo(15);
	}
	
	@Test
	public void testInvalidValue() {
		WireValue value = new WireValue(1);
		assertThat(value.isValidValue()).isFalse();
		
		assertThrows(IllegalStateException.class, value::getValue);
	}
}
