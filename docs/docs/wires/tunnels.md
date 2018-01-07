---
layout: subpage
title: 'Tunnels'
---

This section will cover the use and use case for tunnels.

# Tunnels Overview

Tunnels are basically a way of having "invisible wires"

See the example below

![tunnels-example]

Here, there's effectively an invisible wire connecting the
leftmost tunnel "aa" and the rightmost tunnel "bb"

# Tunnels Terminology

- Label - name for the tunnel. For tunnels to connect to each other,
they must have the same label
- Direction - the side the input of the tunnel will be on
- Bitsize - the bit width of the wire the tunnel connects to. All tunnels
with the same label must also have the same bitsize, or any error will occur

# Tunnels in Action

Let's take a look at the tunnel example from above

![tunnels-example-full]

- Label - both my tunnels have the label "aa" so that they can connect to each other
- Direction - my leftmost tunnel points west, and my rightmost tunnel points east. This 
setting isn't strictly necessary, but it makes my circuit look cleaner.
- Bitsize - since both my input and output pins are 1-bit wide, my tunnels must
also be 1-bit wide.

# Why Use Tunnels?

Tunnels aren't strictly necessary in any circuit, after all, wires do the same job.

However, with larger circuits, you will have wires running everywhere. Tunnels can
make your job easier by reducing the number of wires that you have to keep track of, 
prevent an inordinate number of wires from cluttering your screen, and act as a form
of self documentation. If you label a tunnel a descriptive name - say you have a tunnel
connected to an adder and you label the tunnel "adder-1" - it becomes immediately obvsious
what it's connected to.

All these factors will make your TAs much happier if you're using this program to complete
a programming assignment for a class.

[tunnels-example]: img/tunnels-example.PNG "example of using a tunnel"
[tunnels-example-full]: img/tunnels-example-full.PNG "example of using a tunnel with full circuitsim window"
