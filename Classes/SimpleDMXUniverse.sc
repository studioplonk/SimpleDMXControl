SimpleDMXUniverse {
	var <>header, <>footer;
	var <state, size;

	*new{|size = 512, header ([]), footer ([])|
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

	blackout {
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
		state = state.overWrite(vals.asArray, channel);
	}

	size_ {|newSize|
		size = newSize;
		this.flush;
	}

	state_{|vals|
		// make sure we have enough values
		state = vals[0..(size-1)] ++ (0!(max(size - vals.size, 0)))
	}

	getChannelVals {|channel = 0, range = 1|
		^state[(channel) .. (channel - 1 + range)]
	}

}




