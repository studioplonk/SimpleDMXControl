SimpleDMXFixture {
	classvar <dmxSpec;
	classvar <models;
	var <dmxAddr;
	var <>universeId;
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
					\drawFunc: {|fixture, bounds|

						var extentHalf = bounds.extent * 0.5;

						var dim = fixture.get(\dim);

						var color = Color.black.blend(fixture.getColor, dim);
						var colorBounds = Rect(
							0, 0,
							*extentHalf.asArray
						);
						var dimString = "\tdim: %"
						.format(fixture.getRaw(\dim));
						var dimStringBounds = Rect(
							0, 0,
							extentHalf.x * 2, extentHalf.y
						);

						var white = Color.gray(fixture.get(\white) * dim);
						var whiteBounds = Rect(
							extentHalf.x, 0,
							*extentHalf.asArray
						);

						var strobeDurString = " s:\t%\n d:\t%"
						.format(*fixture.getRaw(\strobe, 2).collect(_.asStringToBase(10, 3)));
						var strobeDurBounds = Rect(
							0, extentHalf.y,
							*extentHalf.asArray
						);

						var fixtureString = "%".format(fixture.dmxAddr);
						var fixtureBounds = Rect(
							extentHalf.x, extentHalf.y,
							*extentHalf.asArray
						);

						Pen.use{
							Pen.color = Color.black;
							Pen.fillRect(bounds);


							// color
							Pen.color = color;
							Pen.fillRect(colorBounds);


							// white
							Pen.color = white;
							Pen.fillRect(whiteBounds);

							// dim string
							Pen.color = Color.gray(0.5);
							Pen.stringLeftJustIn(dimString, dimStringBounds, Font.monospace(10));


							// strobeDur
							Pen.color = Color.white;
							Pen.stringLeftJustIn(strobeDurString, strobeDurBounds, Font.monospace(10));


							Pen.color = Color.grey(1, 0.5);
							Pen.stringCenteredIn(fixture.dmxAddr.asString, fixtureBounds, Font.monospace(30));

						};
					}
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

	getRaw {|key, numVals = 1|
		var idx;

		key.isKindOf(Integer).if({
			idx = key;
		}, {
			idx = idxKeys[key]
		});

		idx.notNil.if{
			(numVals > 1).if({
				^state.copyRange(idx, idx+numVals-1);
			}, {
				^state[idx];
			})
		};
		^nil;
	}


	// expects values between 0..1.0
	set {|key, val, send = false|
		this.setRaw(key, dmxSpec.map(val).asInteger, send)
	}

	get {|key, numVals = 1|
		^dmxSpec.unmap(this.getRaw(key, numVals));
	}


	// a color's alpha is ignored
	setColor {|color, send = false|
		var rgbw = color.asArray;
		// this.set(\dim, rgbw.last);
		this.set(\r, rgbw[0..2], send);
	}

	setColorDim {|color, dim = 1, send = false|
		var rgbw = color.asArray;
		// this.set(\dim, rgbw.last);
		this.set(\r, rgbw[0..2]);
		this.set(\dim, dim, send);
	}

	rgbwDim {|r = 1, g = 1, b = 1, w = 1, dim = 1, send = false|
		this.set(\r, [r, g, b]);
		this.set(\white, w);
		this.set(\dim, dim, send);
	}

	rgbwDimRaw {|r = 255, g = 255, b = 255, w = 255, dim = 255, send = false|
		this.setRaw(\r, [r, g, b]);
		this.setRaw(\white, w);
		this.setRaw(\dim, dim, send);
	}


	getColor {
		var arr = this.get(\r, 3);

		^Color.fromArray(arr);
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

	printOn { arg stream;
        if (stream.atLimit, { ^this });
        stream << this.class.asCompileString << "(" << dmxAddr.asCompileString << ", " << numChannels.asCompileString << ", " << universeId.asCompileString << ")";
    }

    storeOn { arg stream;
        if (stream.atLimit, { ^this });
        stream << this.class.asCompileString << "(" << dmxAddr.asCompileString << ", " << numChannels.asCompileString << ", " << universeId.asCompileString << ")";
    }
}


