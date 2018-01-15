---
layout: subpage
title: 'Tutorial: 3: Creating a circuit with Tunnels and Splitters'
---

This tutorial will cover creating a circuit and using tunnels and splitters.

### Part 0: The Prompt

We will create a circuit that will tell you whether you should bring an umbrella 
with you when you go out. To determine this we'll follow the following formula: 
you should bring an umbrella with you if it's both cloudy and windy or if the 
weatherman tells you to bring an umbrella.

### Part 1: Adding the Input

This circuit will have a single 3-bit input that will handle all of the conditions. 
Add an input to the circuit and change it to be three bits:

![tut-3-pt-1-adding-input-pin]

### Part 2: Adding the Splitter

Now that we have the input placed, we need to access each of the individual bits. The
3-bit input we have placed combines three basic 1-bit inputs into one circuit element.
However, it is often useful to access each of the bits from the input individually. To do
this, we must use a splitter/joiner which can be found under the wiring folder. Go ahead
and add one to the circuit and connect it to the input:

![tut-3-pt-2-adding-splitter]

### Part 3: Editing the Splitter's Attributes

You will notice off the bat that the splitter is not set up properly. Our splitter should take
in a single 3 bit wire and split it into three 1-bit wires. However, by default, it takes in a
single 1-bit wire and splits it into a signle 1-bit wire. We can fix this by changing the Bit
Width In and Fan Out properties:

![tut-3-pt-3-editing-splitter]

### Part 4: Testing the splitter

Make sure that the splitter connects to the correct fanouts.

If you hover over the link on each of the fanouts, you can see which fanout it is. In this splitter,
fanout 0 is at the bottom.

If you attach wires to the splitter, then you can observe its value in "Click Mode" by clicking on the 
wire like so.

Notice that the color of the link on the splitter will also change as you change the value of the input.

![tut-3-pt-4-testing-splitter]

### Part 5: Building the Circuit

Now let’s actually wire up the circuit. If we look back to the formula we want to use, we
can partition it and rewrite it to make the circuit obvious: IF ((Cloudy AND Windy) OR
Weatherman Advice) THEN Take Umbrella. Can you see what gates you need to use?
How would you wire it? It may be useful to label the wires so you don't get them
confused. Create your circuit so that Bit 0 from the input is the Cloudy wire, Bit 1 is
the Windy wire, and Bit 2 is the Weatherman's Advice wire. Your circuit should look
something like this when you are finished:

![tut-3-pt-5-build-circuit]

### Part 6: Adding an Output and Tunnel

We are close to being done with our circuit! The last thing we must do is connect the OR
gate to the output of the circuit. Instead of directly connecting the gate to an output, we're
going to use tunnels. 

Tunnels are a very useful organizational tool to use when making circuits. They can be thought of 
as invisible wires; two wires with matching labels will act as if they are connected by a wire. 
They are not something that exist in real life, but can be used to tidy up your circuits. 

Start by adding an output and two tunnels (found under the wiring folder or by pressing the tunnel
button on the very right of the item bar) to the circuit. In the properties of the tunnels, set
the label property of both tunnels to the same label (such as "output" or "umbrella").
Connect one of the labels to the OR gate and the other to the output. You should have
something like this when you are done. Notice how the value on the wire is carried
through the tunnel:

![tut-3-pt-5-add-output-and-tunnel]

### Part 7: Testing your Circuit

We're done with the circuit! Create a label in your circuit with your name and gtID.
Lastly, you should test the circuit. Try different inputs and make sure that the output is
correct!

Credit: I did not write this tutorial, and I have no idea who did (probably some CS-2110 TA long past).

[tut-3-pt-1-adding-input-pin]: img/tut-3-pt-1-adding-input-pin.PNG "circuit with 3-bit input pin"
[tut-3-pt-2-adding-splitter]: img/tut-3-pt-2-adding-splitter.PNG "added default splitter"
[tut-3-pt-3-editing-splitter]: img/tut-3-pt-3-editing-splitter.PNG "edited splitter"
[tut-3-pt-4-testing-splitter]: img/tut-3-pt-4-testing-splitter.PNG "testing the splitter"
[tut-3-pt-5-build-circuit]: img/tut-3-pt-5-build-circuit.PNG "building the circuit"
[tut-3-pt-5-add-output-and-tunnel]: img/tut-3-pt-5-add-output-and-tunnel.PNG "finished circuit with tunnel and output"

