var result = {
    version: version,
    circuits: {}
};

circuits.forEach(function (circuit) {
    var circuitInfo = {
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
            json.properties[property.name] = property.validator.toString(property.value);
        });

        circuitInfo.components.push(json);
    });

    circuit.wires.forEach(function (wire) {
        var properties = {
            x: wire.x,
            y: wire.y,
            length: wire.length,
            isHorizontal: wire.isHorizontal
        };
        circuitInfo.wires.push(properties);
    });

    result.circuits[circuit.name] = circuitInfo;
});

JSON.stringify(result, null, 4);
