SimpleDMX {
	var <>universes;
	var <>trace = false;
	var <>latency = nil;
	var gui = nil;


	*basicNew {|cmdperiod = true|
        // SimpleDMX.basicNew.states; // does not have any states
		^super.new.initSimpleDMX(cmdperiod);
	}

    *new {|universeSizes([512]), cmdperiod = true|
        ^this.basicNew(cmdperiod).initUniverses(universeSizes);
    }

	initSimpleDMX {|cmdperiod|
		if(cmdperiod) {
			CmdPeriod.add({
				this.close;
			});
		};
	}
    initUniverses {|universeSizes|
        universes = universeSizes.collect({|size|
            SimpleDMXUniverse.new(size);
        });
    }

	open {
		// this.subclassResponsibility(thisMethod);
	}

	close {
		// this.subclassResponsibility(thisMethod);
	}

	////////////////////// state handling //////////////////////
	setState {|state, universeId|
        var universe;
        // if no universeId is given, set all universes
        universeId.isNil.if{
            // make sure there are as many states as universes
            var states = [state.asArray, universes].flop.flop.first;
            universes.do({|universe, i|
                this.setState(states[i], universe.id);
            });
            ^this;
        };

		// fail silently to be compatible with multi-universe DMX devices
		universe = this.getUniverse(universeId);
		universe.notNil.if{
			universe.state = state;
		};
	}

    states {
        ^this.getState;
    }

    getState {|universeId|
        var universe;

        universeId.isNil.if{
            ^universes.collect(_.state);
        };

        universe = this.getUniverse(universeId);
        ^universe.notNil.if({universe.state}, {nil});
    }


	add {|fixture|
		// fixture knows its universe, so we can just add it there
		var universe;

		// if universeId is set, we use it
		fixture.universeId.notNil.if({
			universe = this.getUniverse(fixture.universeId);
			universe.notNil.if({
				universe.add(fixture)
			})
		}, {
			// if universe is not set, we send to all universes
			universes.do{|universe|
				universe.add(fixture)
			}
		});
	}

	addChannelVals {|channel = 0, vals = 0, universeId = 0|
		// if universe is not 0, we ignore it
		var universe = this.getUniverse(universeId);
		universe.notNil.if({
			universe.addChannelVals(channel, vals)
		}, {
			universes.do{|universe|
				universe.addChannelVals(channel, vals)
			}
		})
	}

    blackout {|universeId = 0|
        this.flush(universeId);
    }

    blackoutAll {
        this.flushAll;
    }

	flush {|universeId = 0|
		var universe = this.getUniverse(universeId);
		universe.notNil.if{
			universe.flush;
		};
	}

	flushAll {
		this.universes.do(_.flush);
	}

	getUniverse {|id = 0|
		id.notNil.if({
			^universes[id]
		}, {
			^nil
		})
	}



	////////////////////// sending //////////////////////
	sendDMX {|universeId, flush = false|
        var universe;

        // if no universeId is given, send all universes
        universeId.isNil.if{
            universes.do({|universe, id|
                this.sendDMX(id, flush);
            });
            ^this;
        };

		universe = this.getUniverse(universeId);

        // fail silently to be compatible with multi-universe DMX devices
		universe.notNil.if{
			var array = universe.asInt8Array;

			this.pr_sendDMX(array);
			trace.if({
				array.postcs;
			});
			flush.if{
				this.flush;
			};
		};

	}


    // the acutal sending is done by subclasses
	pr_sendDMX {|rawArray|
		// subclass responsibility
	}
}

SimpleAbstractSerialDMX : SimpleDMX {
	var <port, <>portid, <baudrate;

	*new {|portid, baudrate = 57600, universeSizes([512]), cmdperiod = true|
		^super.new(universeSizes, cmdperiod).initSerialInterface(portid, baudrate);
	}

	initSerialInterface {|argPortid, argBaudrate|
		portid = argPortid;
		baudrate = argBaudrate;
	}

	////////////////////// sending //////////////////////
	pr_sendDMX {|rawArray|
		(port.notNil && {port.isOpen}).if({
			latency.notNil.if({
				r {
					latency.wait;
					port.putAll(rawArray);
				}.play;
			}, {
				port.putAll(rawArray);
			})
		}, {
			"%: port not open".format(this.class).warn;
		});
	}

	////////////////////// port handling //////////////////////
	open {
		// Create serial port
		if(portid.isNil) {
			// Use default
			var tmp = SerialPort.devicePattern;
			SerialPort.devicePattern = "/dev/tty.usbserial-EN*";
			portid = SerialPort.devices[0];
			SerialPort.devicePattern = tmp;
		};
		("Opening serial port" + portid + " ...").inform;
		port = SerialPort.new(portid, baudrate, crtscts: true);

        this.pr_initDevice;
    }
	close {
		"Closing serial ports for EnttecDMX.".inform;
		// SerialPort.closeAll;
		port.close;
	}

	pr_initDevice {
		// called after opening the device
		// subclasses might want to configure device here
	}
}

