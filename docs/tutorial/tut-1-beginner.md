---
layout: subpage
title: 'Tutorial: 1: My First Circuit'
---

This tutorial will serve as an introduction to CircuitSim.
You will learn how to use pins, connect wires, and test your new circuit.

### Part 1: CircuitSim Overview

After opening CircuitSim, you should see a window that looks similar to the following.

![pt-1-empty-circuitsim]

The CircuitSim window has 3 main components

- canvas - this is where you will be creating circuits. Once a component is on the canvas,
you can move it by clicking and dragging, connect it to other components via wires, or delete
it by hitting the delete key. The canvas is also tabbed, so you can create multiple sub-circuits
and use them within your main circuit. You might find this feature useful later on.
- explorer pane - where you'll find the various electronic components that you might
want to use in your circuit. Note that this pane is tabbed. Click on the tab to change
which subset of components are displayed. For this tutorial, we'll be focusing on
the `wiring` tab.
- attribute table - when a component is selected in either the explorer plane or on the canvas,
the attribute table will populate with various settings that you can change for each component.
For now, we'll ignore this menu

There are two other components to the CircuitSim window that might be useful

- toolbar - has some commonly used components, and menus to change the global bit size and scale.
We won't be changing the global bit size for this tutorial, but you may want to change the scale
if components on the canvas are too small.
- menu - pretty self explanatory. 

That's a lot of information, let's make it more concrete by creating a basic circuit.

![pt-1-empty-circuitsim-annotated]

### Part 2: Adding Pins

All circuits should have an `input pin` and `output pin` 

- `input pin` allows you to interact 
with the circuit you create. When creating circuits with multiple sub-circuits, the
`input pin` also allows other circuits to interact with it. Think of the input as the "parameters"
to a function, where the function is your circuit.
- `output pin` is where you feed the result of your circuit. When creating sub-circuits, 
the output `output pin` allows other circuits to get the result of your computation. Think of the 
output as the "return value" of your circuit.

Now lets add a single `input pin` and a single `output pin` to our circuit.

Click on the `input pin` button in the `wiring` tab of the `explorer pane`.
Click anywhere on the canvas to drop the `input pin` there. Note that you can 
also move the `input pin` by clicking and dragging it, delete the pin by 
hitting the delete key, or change some of the pin's settings by changing
a value in the attribute table.

Repeat the process and add a single `output` pin to your circuit.

Your final result should look something like the screenshot below.

Note how the `output pin` currently displays an `X` as its value.
This is expected, because it currently has no value (it's unitialized).

![pt-1-add-pins-1]

### Part 3: Adding Wires

Lets add a wire connecting the `input pin` to the `output pin`. This way,
whatever value is given to the input will the passed to the output. 
If the input is `0`, then the output will also be `0`.

To add a wire, click on the link (the little nub) attached to the `input pin` and drag your
wire until it connects with the link on the `output pin`

![pt-1-add-wires-1]

Your final result should look something like this. Note that the
`output pin` is no longer an `X` because you're feeding it
the value of the `input pin`.

![pt-1-add-wires-2]

### Part 4: Testing your circuit

Your circuit is now complete! Now let's test it.

You can change the `input pin` from a `0` to a `1` and back by clicking
on the pin in `Click Mode`.

Click on the `Click Mode` button in the toolbar. Next, click on your
`input pin`. Notice how the values of both the `input pin` and `output pin`
change to `1`. 

Also note that in `Click Mode`, you can examine the value on any wire by
clicking on it.

![pt-1-test-circuit-1]

After changing the `input pin` to `1`, CircuitSim should look like this:

![pt-1-test-circuit-2]

You should now have a basic understanding of how to create circuits in CircuitSim


[pt-1-empty-circuitsim]: img/tut-1-pt-1-empty-circuitsim.PNG "empty CircuitSim window"
[pt-1-empty-circuitsim-annotated]: img/tut-1-pt-1-empty-circuitsim-annotated.PNG "empty CircuitSim window annotated"
[pt-1-add-pins-1]: img/tut-1-pt-2-add-pins-1.PNG "circuit with just pins"
[pt-1-add-wires-1]: img/tut-1-pt-3-add-wires-1.PNG "adding wires to circuit annotated"
[pt-1-add-wires-2]: img/tut-1-pt-3-add-wires-2.PNG "finished adding wires to circuit"
[pt-1-test-circuit-1]: img/tut-1-pt-4-test-circuit-1.PNG "testing circuit annotated"
[pt-1-test-circuit-2]: img/tut-1-pt-4-test-circuit-2.PNG "input is now 1 - shows changes in output pin"
