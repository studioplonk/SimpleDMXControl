SimpleEnttecDMXPro : SimpleAbstractSerialDMX {
	// EnttecDMX Pro Interface
	// initial implementation by Till Bovermann, Bruno Gola, 2022
	// reworked by Till Bovermann, 2023
	// inspired by EnttecDMX by Jonathan Reus, 2016 and Marije Baalmans dmx quark.

	*new {|portid, baudrate = 57600, universeSize = 512, cmdperiod = true|
		^super.basicNew(cmdperiod)
			.initSerialInterface(portid, baudrate)
			.initSimpleEnttecDMXPro(universeSize);
	}

	// ENTTEC serial protocol
	// 126 6 nb 0 0 b1 b2 b3 ... bn 231
	// 126 - start byte 0x7E 6
	// 6   - send DMX op code
	// nb  - number of bytes (channels) to send + 1 (first byte)
	// 0 or 1 - if number of bytes greater than 256
	// 0 - start code
	// b1 ... bn channel values (each 1 byte)
	// 231 - end byte 0xE7
	initSimpleEnttecDMXPro {|universeSize|
		universes = [
			SimpleDMXUniverse(
				size: universeSize,
				header: [
					0x7E, 6, 
					(universeSize+1) & 0xFF, 
					((universeSize+1) >> 8) & 0xFF, 0
				],
				footer: [0xE7], 
			)
		];
	}
}



