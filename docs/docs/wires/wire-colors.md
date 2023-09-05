---
layout: subpage
title: 'Wire Colors'
---

This tutorial will teach you some common errors people make while creating circuits.

One note about wires: they assume the bit width of the two links that they are
connected to. For example, if you have a 2-bit input and 2-bit output, and connect
the two via wires, the wire will become a 2-bit wire.

Link refers to the little nub on a component that you can connect a wire to.

## Non-Error State Wires

The following wire colors indicate a non-error state, where nothing is seriously
wrong with your circuit.

### Part 1: Dark Green / Light Green

These colors indicate your circuit is connected properly.

dark green wire - 1-bit wire with value of 0

![pt-1-dark-green-wire]

light green wire - 1-bit wire with value of 1

![pt-1-light-green-wire]

### Part 2: Light Blue / Dark Blue

Light blue indicates a 1-bit wire that is "floating" or in a "high impedance
state" — in other words, the wire is not connected to anything.

The wire below is floating because no value is being driven onto it.

![pt-2-blue-wire]

Dark blue indicates a floating multi-bit wire.

![pt-3-dark-blue-wire-uninit]

Since you may have both floating wires and wires with 0/1 on them in a properly
functioning circuit, a floating wire is not considered an error.

### Part 3: Black

Black indicates a multi-bit wire carrying no floating values (only 0s and 1s).

Black wire - multi-bit wire carrying only 0s and 1s:

![pt-3-black-wire]

Also note that a multi-bit wire can carry both floating and 0/1 bits.
This usually pops up when you combine wires together with a splitter, and
some of the individual wires that make up the multi-bit wire are floating.

As stated above, since you may have both floating wires and wires with 0/1 on
them in a properly functioning circuit, a floating wire is not considered an
error.

## Error State Wires

The following wire colors indicate an error state, where something is seriously
wrong with your circuit. In this state CircuitSim will not simulate your circuit.
You must fix any error state wires before running your circuit.

### Part 4: Orange

Indicates that a wire is connected to two links with different bit widths.
This is a problem, as a wire must be connected to links of equal bit widths.

In the screenshot below, we have connected a 2-bit input to a 1-bit output. This
produces an error, because the two links are of different bit widths.

![pt-4-orange-wire]

### Part 5: Red

Indicates a short circuit. Generally this occurs when you try to drive both a 
`1` and a `0` onto the same wire.

In the screenshot below, a short occurs because we've connecte two inputs to
the same wire. One input is `0`, while the other is `1`. This will produce a short.

![pt-5-red-wire]

### Extra: Wires in real world computers

Multi-bit wires don't actually exist in the real world. In your computer, each wire
carries only a single bit of information, a `1` or a `0`. 

They're included in CircuitSim for convenience, so instead of having to create 32 1-bit wires by hand 
(as you can imagine, this gets incredibly tedious), you can create a single 32-bit wire.

These multi-bit wires are known as "wire bundles" in industry.

[pt-1-light-green-wire]: img/wire-colors-pt-1-ligh-green-wire.PNG "light green CircuitSim wire"
[pt-1-dark-green-wire]: img/wire-colors-pt-1-dark-green-wire.PNG "dark green CircuitSim wire"
[pt-2-blue-wire]: img/wire-colors-pt-2-blue-wire.PNG "blue CircuitSim wire"
[pt-3-black-wire]: img/wire-colors-pt-3-black-wire.PNG "black CircuitSim wire"
[pt-3-dark-blue-wire-uninit]: img/wire-colors-pt-3-dark-blue-wire-uninit.PNG "dark blue CircuitSim wire"
[pt-4-orange-wire]: img/wire-colors-pt-4-orange-wire.PNG "orange CircuitSim wire"
[pt-5-red-wire]: img/wire-colors-pt-5-red-wire.PNG "red CircuitSim wire"
