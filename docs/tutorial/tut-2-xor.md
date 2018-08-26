---
layout: subpage
title: 'Tutorial: 2: Creating an XOR Circuit'
---

This tutorial will cover creating an XOR circuit, and using
some of the more advanced features of CircuitSim

### Part 0: XOR Review

If you already know what an XOR does, you can skip this section.

XOR (Exclusive OR) is a logical operation on two inputs that returns
true if both the inputs are different.

Here's the truth table for XOR

| a | b | output |
| - | - | ------ |
| 0 | 0 | 0      |
| 0 | 1 | 1      |
| 1 | 0 | 1      |
| 1 | 1 | 0      |

There's an XOR component in CircuitSim, but for the sake of this
tutorial, let's use AND, OR, and NOT gates to create our own XOR circuit.

Below is a circuit diagram of the XOR gate we will be creating.
The symbols for the various logic gates used are labeled.

![pt-0-circuit-diagram]

### Part 1: Adding Pins

This will be a 1-bit XOR, so we'll need two 1-bit inputs
(for `a` and `b`), and a single 1-bit output.

Go ahead and place the 2 inputs and 1 output now. Make sure
that you leave enough room for all the gates you'll need
for your XOR.

![pt-1-pins]

### Part 2: Changing Component Attributes

Lets add some labels to our inputs and outputs to make
this subcircuit a bit more clear.

Click on a pin. The attribute table for the pin should
pop up in the bottom left corner of your screen.

Here you can change the bit width of the pin, change its
orientation, or add a label. I encourage you to try changing
the pin's orientation and bit width. See what happens. You
may find these settings useful when working on larger circuits.

For now, just change the label for the pin. After typing in a
label, hit "enter" and the changed label will be saved.

Label the two inputs `a` and `b`, and the output `c`.

This is what your circuit should look like with labels.

![pt-2-pins-labeled]

### Part 3: Adding Gates

Now place the AND, OR, and NOT gates you'll need to
complete the XOR circuit. Make sure to follow the circuit
diagram from Part 0.

![pt-3-gates-placed]

### Part 4: Adding Wires

Connect each of the gates with wires according to the
circuit diagram.

![pt-4-wires-added]

### Part 5: Adding Text

Click on the Text tool (T in the toolbar), and then click
anywhere on the canvas to place a text box.

With text box selected, enter some text. I chose "XOR circuit
created with CircuitSim" but you can write anything you want.
As long as the text box is selected, you can edit/add/delete text.

Deselect the text box once you are done. You can deselect by
clicking anywhere else on the canvas.

![pt-5-text-added]

### Part 6: Labeling Subcircuits

Now that are subcircuit is done, let's label it. Right click on
the subcircuit's tab (you can find it at the top left corner
of the canvas). Click on the "Rename" option. The following dialog
box should pop up.

![pt-6-subcircuit-labeled-1]

Enter an appropriate name for the subcircuit and hit ok. Your subcircuit
should now be renamed. Notice how the tab's name has changed from "New circuit"
to "xor" in the screenshot below.

![pt-6-subcircuit-labeled-2]

### Part 7: Saving Circuits

Now let's save our circuit.

Remeber to always save frequently. While working on larger projects,
it would be a good idea to use version control software (CircuitSim
saves circuits as JSON, so any standard VCS should work fine).

Also make sure to test previous saves to ensure that they work. You
can never be too careful.

The save process is self explanatory. Use whatever filename you'd like.
In the example below, I saved this XOR subcircuit as `xor.sim`.

NOTE: `.sim` is the file extension used by CircuitSim to store circuit files.

![pt-7-circuit-saved]

### Part 8: Testing your circuit

Now that we've completed our circuit, let's test it.

After going into "Click Mode', you can change the inputs. After
checking every possible combination of the `a` and `b` inputs against
their correct values in the truth table, I'm confident this XOR works.

![pt-8-circuit-tested]



### Extra: Why bother creating an XOR with ANDs and ORs?

Any gate can be created with ANDs, ORs, and NOTs. The XOR gate we just
built is a neat application of this idea.

Additionaly, in digital logic design, minimizing the number of transistors
to perform a certain function is always top priority.

If you look at the circuit diagram for any logic gate, you will notice
that these gates share a lot of the same "building blocks". By using
parts of one gate to build another, you can reduce your overall transistor
count and power consumption.

This kind of optimization isn't possible if you view a logic gate
as a monolithic entity that can't be broken down.

Previous tutorial: [Tutorial: 1: My First Circuit]({{ site.baseurl }}/tutorial/tut-1-beginner)

Next tutorial: [Tutorial: 3: Creating a circuit with Tunnels and Splitters]({{ site.baseurl }}/tutorial/tut-3-tunnels-splitters)



[pt-0-circuit-diagram]: img/tut-2-pt-0-circuit-diagram.PNG "XOR circuit diagram"
[pt-1-pins]: img/tut-2-pt-1-pins.PNG "XOR circuit pins only"
[pt-2-pins-labeled]: img/tut-2-pt-2-pins-labeled.PNG "XOR circuit pins with labels"
[pt-3-gates-placed]: img/tut-2-pt-3-gates-placed.PNG "XOR circuit gates placed"
[pt-4-wires-added]: img/tut-2-pt-4-wires-added.PNG "XOR wires added"
[pt-5-text-added]: img/tut-2-pt-5-text-added.PNG "XOR text added"
[pt-6-subcircuit-labeled-1]: img/tut-2-pt-6-subcircuit-labeled-1.PNG "XOR circuit subcircuit labeled part 1"
[pt-6-subcircuit-labeled-2]: img/tut-2-pt-6-subcircuit-labeled-2.PNG "XOR circuit subcircuit labeled part 2"
[pt-7-circuit-saved]: img/tut-2-pt-7-saved.PNG "XOR circuit saved"
[pt-8-circuit-tested]: img/tut-2-pt-8-tested.PNG "XOR circuit tested "
