---
layout: subpage
title: 'Splitters'
---

This section will cover splitters, the component
used to access the individual bits of a multi-bit wire

### splitters intro

Splitters can be used to combine multiple wires into a
single wire, or split a multi-bit wire into multiple
smaller wires.

You've already seen this example

![splitter-example]

Here's that same example, with the splitters circled

![splitter-example-annotated]

What's happening here? 

The leftmost splitter is combining the 3 1-bit wires into a single 3-bit wire.
The rightmost splitter is splitting the 3-bit wire into 3 1-bit wires.

### splitter terminology

Remember that you can access the Splitter's attributes by clicking
on it and chaning values in the attribute table that pops up
in the bottom left corner of your screen

input - the angled tip of the splitter that either produces the "combined"
wire if you're using the splitter to combine multiple smaller wires, or the
input, where you connect a multi-bit wire you wish to split into smaller ones.

![splitter-input-circled]

fanout - the part of the splitter that does the "splitting" -
they're the little fingers with a bit width less than that 
of the input

![splitter-fanout-circled]

editable attributes

- label - text label for splitter
- label location - side of splitter that label is placed on
- direction - the direction the fan out points in
- input location - the direction the input points in
- bit size - the bit width of the input end of the splitter
- fanouts - the number of wires that you're splitting the input into
- bit x - for each bit of the input, you assign that bit to one of the fanouts

### splitters in action

Let's revisit the splitter example from above

![splitter-example-full]

- Direction = WEST - the fanout is pointing to the west (left) side of the screen
- Input location = Right/Down - the input is pointing to the Right/Down
- Bitsize - since we have 3 1-bit wires, and I want to carry the value of each of
these wires on a single wire, I need 3 x 1-bit = 3 bits total
- Fanouts - I have 3 wires that I want to combine into 1, so I'll need 3 fanouts. 
Note that the number of fanouts doesn't have to match bit width of the input.

Note that I will have to choose the fanout for each of the bits in the input wire 
(up to bitsize bits)

- Bit 0 - assign bit 0 to fanout 0
- Bit 1 - assign bit 1 to fanout 1
- Bit 2 - assign bit 2 to fanout 2

How do the bit and fanout numbering work?

You should already know how bits are numbered. 

- The 0th bit is the rightmost bit (LSB - Least Significant Bit).
- The 2nd bit is the leftmost bit  (MSB - Most Significant Bit).

Fanouts are numbered from furthest from input to closest, starting from 0.

- 0th fanout - furthest/top
- 1st fanout - middle
- 2nd fanout - closest/bottom

Final note: you can assign multiple bits to the same fanout. For example, if I assigned
both bits 0 and 1 to the 0th fanout, then that fanout would be 2-bits wide.






[splitter-example]: img/splitter-example.PNG "3 1-bit input pins connected to 3 1-bit output pins via a splitter and 3-bit wire"
[splitter-example-annotated]: img/splitter-example-annotated.PNG "splitter example with splitters circled"
[splitter-fanout-circled]: img/splitter-fanout-circled.PNG "splitter with fanout circled"
[splitter-input-circled]: img/splitter-input-circled.PNG "splitter with input circled"
[splitter-example-full]: img/splitter-example-full.PNG "splitter example with attributes visible"
