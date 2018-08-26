package com.ra4king.circuitsim.simulator;

/**
 * @author Roi Atalla
 */
public class WireValue {
	public enum State {
		ONE('1'), ZERO('0'), X('x');
		
		public final char repr;
		
		State(char c) {
			repr = c;
		}
		
		public State negate() {
			return this == X ? X : this == ONE ? ZERO : ONE;
		}
	}
	
	private State[] bits;
	
	public WireValue(int bitSize) {
		this(bitSize, State.X);
	}
	
	public WireValue(int bitSize, State state) {
		bits = new State[bitSize];
		setAllBits(state);
	}
	
	public WireValue(State... states) {
		bits = states;
	}
	
	public WireValue(WireValue value) {
		bits = new State[value.bits.length];
		System.arraycopy(value.bits, 0, bits, 0, bits.length);
	}
	
	public WireValue(WireValue value, int newSize) {
		bits = new State[newSize];
		setAllBits(State.ZERO);
		System.arraycopy(value.bits, 0, bits, 0, newSize < value.bits.length ? newSize : value.bits.length);
	}
	
	public WireValue merge(WireValue value) {
		if(value.getBitSize() != this.getBitSize()) {
			throw new IllegalStateException("Different size wires detected: wanted "
					                                + this.getBitSize() + ", found " + value.getBitSize());
		}
		
		for(int i = 0; i < getBitSize(); i++) {
			if(getBit(i) == State.X) {
				setBit(i, value.getBit(i));
			} else if(value.getBit(i) == State.X) {
				setBit(i, getBit(i));
			} else if(value.getBit(i) != getBit(i)) {
				throw new ShortCircuitException(this, value);
			}
		}
		
		return this;
	}
	
	public static WireValue of(long value, int bitSize) {
		WireValue wireValue = new WireValue(bitSize);
		for(int i = bitSize - 1; i >= 0; i--) {
			if((value & (1 << i)) == 0) {
				wireValue.setBit(i, State.ZERO);
			} else {
				wireValue.setBit(i, State.ONE);
			}
		}
		return wireValue;
	}
	
	public void setAllBits(State state) {
		for(int i = 0; i < bits.length; i++) {
			bits[i] = state;
		}
	}
	
	public int getBitSize() {
		return bits.length;
	}
	
	public void setBitSize(int bitSize) {
		State[] oldBits = bits;
		
		bits = new State[bitSize];
		setAllBits(State.X);
		System.arraycopy(oldBits, 0, bits, 0, bitSize < oldBits.length ? bitSize : oldBits.length);
	}
	
	public State getBit(int index) {
		return bits[index];
	}
	
	public void setBit(int index, State state) {
		bits[index] = state;
	}
	
	public WireValue set(WireValue other) {
		if(other.getBitSize() != getBitSize()) {
			throw new IllegalArgumentException("Cannot set wire of different size bits. Wanted: " + bits.length +
					                                   ", Found: " + other.bits.length);
		}
		
		System.arraycopy(other.bits, 0, bits, 0, bits.length);
		return this;
	}
	
	public WireValue slice(int offset, int length) {
		if(offset <= 0 || offset + length > bits.length) {
			throw new IllegalArgumentException("Incorrect offset and length: " + offset + ", " + length);
		}
		
		WireValue value = new WireValue(length);
		for(int i = offset; i < offset + length; i++) {
			value.setBit(i - offset, bits[i]);
		}
		
		return value;
	}
	
	public boolean isValidValue() {
		if(bits.length == 0) {
			return false;
		}
		
		for(State bit : bits) {
			if(bit == State.X) {
				return false;
			}
		}
		
		return true;
	}
	
	public int getValue() {
		int value = 0;
		for(int i = 0; i < bits.length; i++) {
			if(bits[i] == State.X) throw new IllegalStateException("Invalid value");
			
			value |= (1 << i) * (bits[i] == State.ONE ? 1 : 0);
		}
		return value;
	}

	/**
	 * Converts the value held on this wire to a hex string.
	 *
	 * @return a lowercase hex string if all bits are defined, otherwise
	 *         getBitSize() 'x's
	 */
	public String toHexString() {
		String value;
		int hexDigits = 1 + (getBitSize() - 1) / 4;
		if(isValidValue()) {
			value = String.format("%0" + hexDigits + "x", getValue());
		} else {
			value = "";
			for(int i = 0; i < hexDigits; i++) {
				value += "x";
			}
		}
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof WireValue) {
			WireValue value = (WireValue)other;
			if(value.getBitSize() != this.getBitSize()) {
				return false;
			}
			
			for(int i = 0; i < this.getBitSize(); i++) {
				if(this.getBit(i) != value.getBit(i)) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i = this.getBitSize() - 1; i >= 0; i--) {
			builder.append(this.getBit(i).repr);
		}
		return builder.toString();
	}
}
