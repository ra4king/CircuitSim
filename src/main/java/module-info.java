module circuitsim {
	requires javafx.controls;
	requires com.google.gson;

	exports com.ra4king.circuitsim.gui;
	exports com.ra4king.circuitsim.gui.file;
	exports com.ra4king.circuitsim.gui.peers;
	exports com.ra4king.circuitsim.gui.peers.arithmetic;
	exports com.ra4king.circuitsim.gui.peers.gates;
	exports com.ra4king.circuitsim.gui.peers.io;
	exports com.ra4king.circuitsim.gui.peers.memory;
	exports com.ra4king.circuitsim.gui.peers.misc;
	exports com.ra4king.circuitsim.gui.peers.plexers;
	exports com.ra4king.circuitsim.gui.peers.wiring;
	exports com.ra4king.circuitsim.simulator;
	exports com.ra4king.circuitsim.simulator.components;
	exports com.ra4king.circuitsim.simulator.components.arithmetic;
	exports com.ra4king.circuitsim.simulator.components.gates;
	exports com.ra4king.circuitsim.simulator.components.memory;
	exports com.ra4king.circuitsim.simulator.components.plexers;
	exports com.ra4king.circuitsim.simulator.components.wiring;
}
