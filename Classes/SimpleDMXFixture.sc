SimpleDMXFixture {
	classvar <dmxSpec;
	classvar <models;
	var <dmxAddr;
	var <universeId;
	var <state;
	var <numChannels;
	var <desc;
	var <idxKeys;
	var <>device;

	*initClass {
		models = (
			ax3: (
				'13': (
					name: "Astera AX3 Lightdrop",
					functionality: "DRGBAWS",
					type: "LEDPar",
					channelTypes: #[dim, r, g, b, amber, white, strobe]
				)
			),
			thunderwash: (
				'7CH_2': (
					name: "cameo THUNDERWASH 600 RGBW",
					functionality: "DSdRGBW",
					type: "LEDWash",
					channelTypes: #[dim, strobe, dur, r, g, b, white],
					manual: "https://www.cameolight.com/en/downloads/file/id/-1911810176",
					typeRanges: (
						strobe: (
							on: (0..5) ++ (251..255),
							off: (6..10),
							strobe: (11..250),
						),
						dur: (
							relOffOn: (0..255)
						)
					),
				)
			)
		);
		dmxSpec = ControlSpec(0, 255, \lin, 1);
	}

	*new {|dmxAddr = 0, numChannels = 1, universeId = 0, desc, device|
		^super.new.init(dmxAddr, numChannels, universeId, desc, device)
	}

	*newFor {|model, mode, dmxAddr, universeId = 0, device|
		var desc = models[model][mode];
		var numChannels = desc[\channelTypes].size;
		var obj;

		desc[\model] = model;
		obj = this.new(dmxAddr, numChannels, universeId, desc, device);

		^obj
	}

	*printKnownModels {
		models.keys.asArray.sort.do{|model|
			models[model].keys.asArray.sort.do{|mode|
				"SimpleDMXFixture.newFor(%, %, <addr>)".format(
					model.asCompileString,
					mode.asCompileString
				).postln
			}
		}
	}

	init {|argDmxAddr, argNumChannels, argUniverseId, argDesc, argDevice|
		dmxAddr = argDmxAddr;
		desc = argDesc ?? {()};
		universeId = argUniverseId;
		device = argDevice;

		idxKeys = desc[\channelTypes].collectAs({|key, i|
			key -> i
		}, Event);
		numChannels = argNumChannels;
		this.flush;
	}

	flush {
		state = Array.fill(numChannels, 0);
	}

	// expects int values between 0..255
	setRaw {|key, rawVal, send = false|
		var idx;

		key.isKindOf(Integer).if({
			idx = key;
		}, {
			idx = idxKeys[key]
		});

		idx.notNil.if{
			state.overWrite(rawVal.asArray, idx);
			send.if{
				this.send
			};
		};
	}

	// expects values between 0..1.0
	set {|key, val, send = false|
		this.setRaw(key, dmxSpec.map(val).asInteger, send)
	}

	// a color's alpha is considered dim (if available in fixture spec, otherwise ignored).
	setColor {|color, send = false|
		var rgbw = color.asArray;
		this.set(\dim, rgbw.last);
		this.set(\r, rgbw[0..2], send);
	}

	state_ {|val|
		// restrict to numChannels
		val = val.asArray;
		(val.size < numChannels).if{
			val = val ++ Array.fill(numChannels-val.size, 0);
		};
		state = val[0..numChannels-1];
	}

	send {|flushDevice = false|
		device.notNil.if{
			device.add(this);
			device.sendDMX(this.universeId, flushDevice);
		}

	}

}


