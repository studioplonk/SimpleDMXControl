SimpleDMXFixture {
	classvar <dmxSpec;
	classvar <models;
	var <dmxAddr;
	var <universeId;
	var <>state;
	var <desc;

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
					channelTypes: #[dim, strobe, dur, r, g, b, white]
				)
			)


		)
	}

	*new {|dmxAddr = 0, numChannels = 1, universeId = 0, desc|
		^super.new.init(dmxAddr, numChannels, universeId, desc)
	}

	*newFor {|model, mode, dmxAddr, universeId = 0|
		var desc = models[model][mode];
		var numChannels = desc[\channelTypes].size;
		var obj;

		desc[\model] = model;
		obj = this.new(dmxAddr, numChannels, universeId, desc);

		obj.desc[\rgbIdx] = obj.desc[\channelTypes].detectIndex{|v| v == \r};

		^obj
	}



	setColor {|color|
		state.overWrite((color.asArray[0..2] * 255).asInteger, desc[\rgbIdx])
	}

	init {|argDmxAddr, argNumChannels, argUniverseId, argDesc|
		dmxAddr = argDmxAddr;
		desc = argDesc;
		universeId = argUniverseId;

		state = 0!argNumChannels;
	}

	numChannels {
		^state.size;
	}
}


/*
(
q = q ? ();

SerialPort.devicePattern = "/dev/tty.usbserial-EN*";
if(thisProcess.platform.name == \linux) { SerialPort.devicePattern = "/dev/ttyU*" };
q.dmxPort = SerialPort.devices[0];
q.enttec = SimpleEnttecDMXPro(q.dmxPort);
// q.enttec.latency = q.nodeLatency;

q.enttec.open;
)


b = SimpleDMXFixture.newFor(\ax3, \13, 0, 0)
b.state = {255.rand}!7
b.state = [255, 255, 255, 255, 255, 0]

b.setColor(Color.rand)
b.state
q.enttec.add(b); q.enttec.sendDMX(flush: true);



c = (0..7).collect{|fixtureId| SimpleDMXFixture.newFor(\ax3, \13, fixtureId * 7)}

(
c.do{|f|
//	f.state = {255.rand}!6 ++ 0;
	f.state = {255}!6 ++ 0;
	q.enttec.add(f)
};

q.enttec.sendDMX(flush: true);
)
*/