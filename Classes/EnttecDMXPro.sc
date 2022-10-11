EnttecDMXPro {
	// EnttecDMX Pro Interface
	// Till Bovermann, Bruno Gola, 2022
	// inspired by EnttecDMX by Jonathan Reus, 2016 and Marije Baalmans dmx quark.

	var <port, <>portid;
	var <>universe;
	var <>trace = false;
	var <>latency = nil;

	*new {|portid, cmdperiod = true|
		^super.new.init(portid, cmdperiod);
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
	init {|argportid, cmdperiod |

		universe = DMXUniverse(
			[0x7E, 6, (512+1) & 0xFF, ((512+1) >> 8) & 0xFF, 0],
			[0xE7]
		);

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
	}


	addChannelVals {|channel = 1, vals = 0, universeId = 1|
		// universeId ignored in this class, added for compatibility with EnttecDMXProMk2
		(universeId == 1).if{
			universe.addChannelVals(channel, vals)
		}
	}

	getUniverse {|id = 1|
		(id == 1).if({
			^universe
		}, {
			^nil
		})
	}


	sendDMX {|universeId = 1, flush = true|
		// universeId ignored in this class, added for compatibility with EnttecDMXProMk2
		var array = this.getUniverse(universeId);

		array.notNil.if{
			array = array.asInt8Array;
			port.notNil.if{
				latency.notNil.if({
					r {
						latency.wait;
						port.putAll(array);
					}.play;
				}, {
					port.putAll(array);
				})
			};
			trace.if({
				array.postcs;
			});
			flush.if{
				this.flush;
			};
		};
	}

	flush {
		this.universe.flush;
	}

	close {
		"Closing serial ports for EnttecDMX.".postln;
		// SerialPort.closeAll;
		port.close;
		this.release;
	}
}



