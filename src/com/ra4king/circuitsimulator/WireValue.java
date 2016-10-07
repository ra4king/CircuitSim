package com.ra4king.circuitsimulator;

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
	}
	
	private State[] bits;
	
	public WireValue(int bitSize) {
		this(bitSize, State.X);
	}
	
	public WireValue(int bitSize, State state) {
		bits = new State[bitSize];
		setAllBits(state);
	}
	
	public WireValue(WireValue value) {
		bits = new State[value.bits.length];
		System.arraycopy(value.bits, 0, bits, 0, bits.length);
	}
	
	public void merge(WireValue value) {
		if(getBitSize() != value.getBitSize()) {
			throw new IllegalArgumentException("Cannot merge different bit-sized wires");
		}
		
		if(!isCompatible(value)) {
			throw new IllegalArgumentException("Cannot merge incompatible wires");
		}
		
		for(int i = 0; i < getBitSize(); i++) {
			if(getBit(i) == State.X && value.getBit(i) == State.X) {
				setBit(i, State.X);
			} else if(getBit(i) == State.X) {
				setBit(i, value.getBit(i));
			} else {
				setBit(i, getBit(i));
			}
		}
	}
	
	public static WireValue of(int value, int bitSize) {
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
	
	public void set(WireValue other) {
		if(other.getBitSize() != getBitSize()) {
			throw new IllegalArgumentException("Cannot set wire of different size bits. Wanted: " + bits.length + ", Found: " + other.bits.length);
		}
		
		System.arraycopy(other.bits, 0, bits, 0, bits.length);
	}
	
	public boolean isValidValue() {
		if(bits.length == 0)
			return false;
		
		for(State bit : bits) {
			if(bit == State.X)
				return false;
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
	
	public boolean isCompatible(WireValue value) {
		if(value.getBitSize() != this.getBitSize())
			return false;
		
		for(int i = 0; i < this.getBitSize(); i++) {
			State thisBit = this.getBit(i);
			State thatBit = value.getBit(i);
			if(thisBit != State.X && thatBit != State.X && thisBit != thatBit) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof WireValue) {
			WireValue value = (WireValue)other;
			if(value.getBitSize() != this.getBitSize())
				return false;
			
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
		String string = "";
		for(int i = this.getBitSize() - 1; i >= 0; i--) {
			string += this.getBit(i).repr;
		}
		return string;
	}
}
