var result = {
    version: 1.0,
    circuits: {}
};

circuits.forEach(function (circuit) {
    result.circuits[circuit.name] = {
        components: [],
        wires: []
    };

    circuit.components.forEach(function (component) {
        var json = {
            name: component.className,
            x: component.x,
            y: component.y,
            properties: {}
        };

        component.properties.forEach(function (property) {
            json.properties[property.name] = property.value;
        });

        result.circuits[circuit.name].components.push(json);
    });

    circuit.wires.forEach(function (wire) {
        var properties = {
            x: wire.x,
            y: wire.y,
            length: wire.length,
            isHorizontal: wire.isHorizontal
        };
        result.circuits[circuit.name].wires.push(properties);
    });
});

JSON.stringify(result, null, 4);
