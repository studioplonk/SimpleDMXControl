SimpleEnttecDMXProMk2 : SimpleAbstractSerialDMX {
	// EnttecDMX Pro Mk2 Interface

	*new {|portid, baudrate = 57600, universeSizes([512, 512]), cmdperiod = true|
		// make sure there are at least two universe sizes
		universeSizes = [#[1, 1], universeSizes].flop.flop.last;
		^super.basicNew(cmdperiod)
			.initSerialInterface(portid, baudrate)
			.initSimpleEnttecDMXProMk2(universeSizes);
	}

	initSimpleEnttecDMXProMk2 {|universeSizes|
		universes = [
			SimpleDMXUniverse(
				universeSizes[0],
				header: [0x7E, 6, (universeSizes[0]+1) & 0xFF, ((universeSizes[0]+1) >> 8) & 0xFF, 0],
				footer: [0xE7]
			),
			SimpleDMXUniverse(
				universeSizes[1],
				header: [0x7E, 202, (universeSizes[1]+1) & 0xFF, ((universeSizes[1]+1) >> 8) & 0xFF, 0],
				footer: [0xE7]
			)
		];
	}

	pr_initDevice {
		var packet;
		// Activates the PRO MK2; Sets both Ports for DMX (adapted from cpp example code)
		// 13 means Set API key, API key in our case is {0xC9, 0xA4, 0x03, 0xE4}
		packet = Int8Array.with(0x7E, 13, 4, 0, 0xC9, 0xA4, 0x03, 0xE4, 0xE7);
		fork {
			port.putAll(packet);
			0.3.wait; // Not sure if this wait is necessary

			// 147 is to set the port assignment (from Python code + cpp code)
			packet = Int8Array.with(0x7E, 147, 2, 0, 1, 1, 0xE7);
			port.putAll(packet);
		};
	}
}
