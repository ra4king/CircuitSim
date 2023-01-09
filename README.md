CircuitSim v1.9.0
=================

Site and download links: https://ra4king.github.io/CircuitSim

Basic circuit simulator with many built-in components. Check out `src/com/ra4king/circuitsim/simulator/components`
for examples of how Components are written. Their GUI counterparts are in `src/com/ra4king/circuitsim/gui/peers`.

Screenshot
![Screenshot](http://i.imgur.com/Oon39Np.png)

Building
========

The provided gradle scripts can be used to easily build a JAR file. Just run `/.gradlew jar` on Linux
or `/.gradlew.bat jar` on Windows!

The release directory contains a `build.sh` script that automatically builds the JAR, and generates an executable image
for Windows, Mac, and Linux.

Changelog
=========

1.9.0
=====

- The grid background can now be hidden: View -> uncheck Show grid
- Circuits can be exported as image files: File -> Export as images
- New component: 7-segment display
- Updated components: probes and constants can now display in multiple formats.
- Integer inputs now accept multiple formats: decimal, binary, and hex
- RAM and ROM editors have been improveds, and ROM edits now support undo and redo.
- Remove the ability to negate the NOT gate's input, this looks to have been an oversight.
- Improve placement and display of gates with negated inputs and more than 5 inputs: no more "floating" inputs. Inputs
  are now either directly attached to the gate's body or have a line linking them to the gate. This is backwards
  compatible by maintaining the old behavior for older save files.

1.8.5
-----

- New component: Breakpoint component that automatically stops the clock at a particular input value
- New component: Power and Ground components that are pretty much just the 1-bit constants 1 and 0 respectively
- Fixed a deadlock issue in Clock
- Fixed possible race conditions where the circuit was modified while it's being accessed outside the simulator lock

1.8.4
-----

- Hotfix for an issue that leads to erroneous Short Circuit messages when there are none

1.8.3
-----

- RAM now has the option to have separate load/store ports
- The transistor has been updated with a more accurate version. This is backwards compatible by deprecating the old
  transistor and replacing it with a new component

1.8.2
-----

- Hotfix for issue with buffer component where setting a label causes a NullPointerException
- Hotfix for issue with memory editor for both RAM and ROM components where pasting 16 or more values causes it to skip
  a column every 16th value
- Improve error message window to include "Save and Exit"

1.8.1
-----

- Significant performance and memory usage improvements
- Holding CTRL will keep a component selected after placing it
- Circuit file generation is now deterministic
- Fix subcircuit pin count mismatch errors
- Fix display of Output pin, where it used to be printed as Input pin
- Fix saving ROM contents after being broken in 1.8.0
- (Breaking change) Fix subcircuit pin ordering on the east and west side by sorting by Y instead of X

1.8.0
-----

- Revamp ROM/RAM component look: now showing the current address and value
- Revamp ROM/RAM memory editor: no need to double click to start typing, no need to hit enter to commit, full multi-cell
  copy/paste support
- Huge improvement and bug-fixes to wire behavior when moving components and with undo/redo
- Add RandomGenerator component
- Add copy/cut/paste to context menu in circuit editor
- Display missing label names in certain components
- Add All Files option in Load File dialog
- Fixed bug with gates where downsizing the number of inputs kept the extra Negate options

1.7.4
-----

- Fix several bugs with wire creation when dragging components
- Give an option to send an error report upon unexpected errors
- Minor bug fixes

1.7.3
-----

- Fix blue wire issues with Tunnels and subcircuits
- Highlight wires when clicking on them
- Ctrl+scroll now zooms in/out
- Right clicking when placing components or dragging wires cancels the action
- Fix bug with intersecting wires being auto-joined on drag
- Improved error handling

1.7.2
-----

- Add Probe component, which lets you observe values on wires without resorting to output pins
- Improve behavior multi-component selection when holding down control by disabling drag-less wire creation when control
  is held down
- Copy doesn't do anything when nothing is selected instead of emptying the clipboard
- Other minor bugs fixed

1.7.1
-----

- Squashed tunnel bugs: now short-circuits should propagate properly across them and tunnels should work properly
- Added comparison type to Comparator: now you can do both 2's complement and unsigned comparison
- Draw magnifying glass to subcircuit components when hovering over them to indicate ability to view internal state
- Officially making a file format change supporting library paths
- Other minor bugs fixed

1.7.0
-----

- Big thanks to Cem Gokmen for helping with the Mac issues and Austin Adams for gradle-ifying the repo!
- Now supporting both Java 8 and 9 - thanks to Austin Adams for figuring out how to create a multi-release Jar
- Huge rendering performance improvements, especially for Mac
- Fixed keyboard shortcut issue on Mac
- Use system menu bar on Mac instead of the in-app bar
- Added an update checker that notifies you when there is a new version
- Fixed a bug with subcircuits where it pushed values through output pins, causing seemingly un-explainable short
  circuits
- Improved behavior of tab selection when moving them or deleting them, especially with undo/redo
- Many minor bug fixes and improvements

1.6.2
-----

- Fixed exceptions being thrown when using Tunnels
- Now you don't have hit enter to update a text or value property, someone complained that it was annoying. The
  component is automatically updated when you unfocus from the text input
- Multi-bit wires with a floating bit (X) now show up as dark blue instead of black
- Button now has directions

1.6.1
-----

- Improved behavior when dragging components to also delete wires whenever they overlap existing ones
- Huge performance increases with subcircuits and tunnels. Bad designs will still be very slow to run the autograder in
- Now showing Help dialog on first run

1.6.0
-----

- Added creation of new wires when dragging components. This allows maintaining existing connections when dragging
  components to new locations
- Hold CTRL before beginning the drag to disable this
- Fixed file load issue where an error was thrown if the folder doesn't exist anymore
- Fixed issues with dragging + keyboard shortcuts
- Fixed an undo bug when updating components
