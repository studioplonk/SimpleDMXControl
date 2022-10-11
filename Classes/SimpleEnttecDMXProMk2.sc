SimpleEnttecDMXProMk2 {
	// EnttecDMX Pro Mk2 Interface
	// Till Bovermann, Bruno Gola, 2022
	// inspired by EnttecDMX by Jonathan Reus, 2016 and Marije Baalmans dmx quark.

	var <port, <>portid;
	var <universes;
	var <>trace = false;
	var <>latency = nil;

	*new {|portid=nil, cmdperiod=true|
		^super.new.init(portid, cmdperiod);
	}


	// ENTTEC serial protocol
	// 126 6 nb 0 0 b1 b2 b3 ... bn 231
	// 126 - start byte 0x7E 6
	// 6|202 - DMX op code for universe selection
	// nb  - number of bytes (channels) to send + 1 (first byte)
	// 0 or 1 - if number of bytes greater than 256
	// 0 - start code
	// b1 ... bn channel values (each 1 byte)
	// 231 - end byte 0xE7
	init {|argportid, numchannels, cmdperiod|

		universes = [
			SimpleDMXUniverse(
				[0x7E, 6, (512+1) & 0xFF, ((512+1) >> 8) & 0xFF, 0],
				[0xE7]
			),
			SimpleDMXUniverse(
				[0x7E, 202, (512+1) & 0xFF, ((512+1) >> 8) & 0xFF, 0],
				[0xE7]
			)
		];


		portid = argportid;


		if(cmdperiod == true) {
			CmdPeriod.add({
				this.close;
			});
		};
	}


	open {

		// Create serial port
		if(portid.isNil) {
			// Use default
			var tmp = SerialPort.devicePattern;
			SerialPort.devicePattern = "/dev/tty.usbserial-EN*";
			portid = SerialPort.devices[0];
			SerialPort.devicePattern = tmp;
		};
		("Opening serial port" + portid + " ...").postln;
		port = SerialPort.new(portid, 57600, crtscts: true);

		this.pr_initDevice;
	}

	pr_initDevice {
		var packet;
		// Activates the PRO MK2; Sets both Ports for DMX
		// from cpp example code
		// 13 means Set API key, API key in our case is {0xC9, 0xA4, 0x03, 0xE4}
		packet = Int8Array.with(0x7E, 13, 4, 0, 0xC9, 0xA4, 0x03, 0xE4, 0xE7);
		fork {
			port.putAll(packet);
			// Not sure if this wait is necessary, but better safe than sorry.
			0.3.wait;
			// 147 is to set the port assignment (from Python code + cpp code)
			packet = Int8Array.with(0x7E, 147, 2, 0, 1, 1, 0xE7);
			port.putAll(packet);
		};
	}


	addChannelVals {|channel = 1, vals = 0, universeId = 1|
		this.getUniverse(universeId).addChannelVals(channel, vals)
	}

	getUniverse {|id = 1|
		^universes[id - 1]

	}

	setState {|state, universeId = 1|
		var universe = this.universes[universeId - 1];

		universe.notNil.if{
			universe.state = state;
		};
	}


	sendDMX {|universeId = 1, flush = true|
		var array = this.getUniverse(universeId).asInt8Array;
		trace.if({
			array.postcs;
		});
		port.notNil.if{
			latency.notNil.if({
				r {
					latency.wait;
					port.putAll(array);
				}.play;
				}, {
					port.putAll(array);
			});
		};
		flush.if{
			this.flush(universeId);
		};
	}

	flush {|universeId = 1|
		this.getUniverse(universeId).flush;
	}

	flushAll {
		this.universes.do(_.flush);
	}

	close {
		"Closing serial ports for SimpleEnttecDMXProMk2.".postln;
		// SerialPort.closeAll;
		port.close;
		this.release;
	}
}
