var json = JSON.parse(file);

if (json.version != version) {
    throw new Error("Incompatible version!");
}

var ArrayList = java.util.ArrayList;
var HashSet = java.util.HashSet;
var Properties = com.ra4king.circuitsimulator.gui.Properties;
var CircuitInfo = com.ra4king.circuitsimulator.gui.file.FileFormat.CircuitInfo;
var ComponentInfo = com.ra4king.circuitsimulator.gui.file.FileFormat.ComponentInfo;
var WireInfo = com.ra4king.circuitsimulator.gui.file.FileFormat.WireInfo;

var circuits = new ArrayList();

for (var name in json.circuits) {
    var circuit = json.circuits[name];
    
    var components = new HashSet();

    circuit.components.forEach(function (component) {
        var className = component.name;
        var x = component.x;
        var y = component.y;
        var properties = new Properties();

        for (var property in component.properties) {
            properties.ensureProperty(new Properties.Property(property, null, component.properties[property]));
        }

        components.add(new ComponentInfo(className, x, y, properties));
    });

    var wires = new HashSet();

    circuit.wires.forEach(function (wire) {
        var x = wire.x;
        var y = wire.y;
        var length = wire.length;
        var isHorizontal = wire.isHorizontal;
        wires.add(new WireInfo(x, y, length, isHorizontal));
    });

    circuits.add(new CircuitInfo(name, components, wires));
}

circuits;
