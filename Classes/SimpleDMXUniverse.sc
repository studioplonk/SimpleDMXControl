SimpleDMXUniverse {
	var <header, <footer;
	var <state, size;

	*new{|header ([]), footer ([]), size = 512|
		^super.new.init(header, footer, size)
	}


	init {|argHeader, argFooter, argSize|
		header = argHeader;
		footer = argFooter;
		size = argSize;
		this.clear;
	}

	flush {
		state = 0!size;
	}

	clear {
		this.flush;
	}

	asInt8Array {
		^Int8Array.newFrom(header ++ state ++ footer)
	}

	add {|fixture|
		this.addChannelVals(fixture.dmxAddr, fixture.state)
	}

	addChannelVals {|channel = 1, vals = 0|
		// insert values
		// channel is in range [0..511]
		vals = if (vals.isArray) { vals } { [ vals ] };

		state = state.overWrite(vals, channel);
	}

	state_{|vals|
		state = vals[0..511] ++ (0!(max(512 - vals.size, 0)))
	}

	getChannelVals {|channel = 1, range = 1|
		^state[(channel - 1) .. (channel - 2 + range)]
	}

}




