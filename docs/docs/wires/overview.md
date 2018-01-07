---
layout: subpage
title: 'Wires Overview'
---

This section will cover wire basics

### Links on Components

First, let's define a couple of terms:

component - an electrical component that you use as a
building block to a circuit. Components include
things like input pins, gates, and plexers.
link - the little "nub" on a component that you connect
wires to. Components may have multiple links. For example,
a standard AND gate has 2 links for input, and 1 link for output.

Here are the links on an input pin (notice that it only has
one link)

![input-pin-link]

And here are the links on a standard AND gate (notice the 2
input links and 1 output link)

![and-gate-links]

Wires are used to connect 2 or more links together

### 1-bit vs multi-bit wires (wire bundles)

1-bit wires are what you'd think of as "normal wires" - they
can either be on or off. 

Here's an example of a 1-bit wire carrying a `1` (on), and a 1-bit
wire carrying a `0` (off).

![one-bit-wire]

Many kinds of circuits require that more than 1-bit of information
be transmitted - for those we use multi-bit wires (wire bundles).

Multi-bit wires can be thought of as multiple 1-bit wires next to each
other.

We will also use the terms *bitsize* or *bit width* to refer to 
how many bits a multi-bit wire can hold.

Here's an example of a multi-bit wire vs multiple 1-bit wires, to
show that they are analogous to one another.

![multi-vs-one]

NOTE: this picture above uses splitter, which we'll go over in the next
section.

### bit width of a wire bundle

How does a wire bundle know what bit width to assume? There's no menu
you can access to change the bit width of a wire bundle.

Wire bundles will assume the bit width of the links that they are connected
to.

Let's look at a couple of examples.

In the example below, the input pin and output pin are both 1-bit wide, so
their links are also 1-bit wide. If we connect them together, then the
wire will assume a bit width of 1-bit.

![one-bit-wire-1]

If we make our input and output pins both 2-bits wide, then the links
on our pins will also be 2-bits wide. When we try to connect them together,
the wire will assume a bit width of 2.

![two-bit-wire]

What happens when we connect a 1-bit link and a 2-bit link together? The wire
won't know how many bits it should be, so CircuitSim will display an error.
Orange wires always indicate that you are connecting links with incompatible
bit widths.

![circuitsim-error-bit-width-links]


[and-gate-links]: img/and-gate-links.PNG "links on AND gate"
[input-pin-link]: img/input-pin-link.PNG "links on input pin"
[one-bit-wire]: img/one-bit-wire.PNG "1-bit wires both on and off"
[multi-vs-one]: img/multi-bit-wire-vs-multiple-single-bit-wires.PNG "one multi-bit wire vs. multiple single-bit wires"
[one-bit-wire-1]: img/one-bit-wire-1.PNG "a single 1-bit wire"
[two-bit-wire]: img/two-bit-wire.PNG "a single 2-bit wire"
[circuitsim-error-bit-width-links]: img/circuitsim-error-bit-width-links.PNG "1-bit input pin connected to 2-bit output pin produces error"
