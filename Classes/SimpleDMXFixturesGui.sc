SimpleDMXFixturesGui {
	var <win, views, <fixtures;

	*new{|fixtures|
		^super.new.init(fixtures);
	}


	init {|argFixtures|
		var viewExtent = 100@100;
		var layoutMargin = 5@5;
		var layoutGap = layoutMargin;
		var maxWidth = Window.screenBounds.width;
		var width = argFixtures.size * (viewExtent.x + layoutGap.x) + layoutMargin.x;
		var height = (viewExtent.y + layoutGap.y);

		var numRows = (width / maxWidth).asInteger;
		var numViewsPerRow = (argFixtures.size / numRows).asInteger;

		width = min(width, maxWidth);
		height = (numRows + 1) * height  + layoutMargin.y; // care for last margin


		fixtures = argFixtures;
		win = Window.new(
			"SimpleDMXFixturesGui",
			Rect(
				0.0, 0.0,
				width,
				height
			)
		).front;
		win.addFlowLayout( layoutMargin,  layoutGap);

		views = fixtures.collect{|fixture|
			UserView(win, viewExtent)
			.drawFunc_{|me|
				var bounds = me.bounds.moveTo(0,0);
				fixture.desc[\drawFunc].value(fixture, bounds)
			}
		};
		this.frameRate_.animate_(true);
	}

	animate_ {|val = true|
		views.collect{|v| v.animate_(val)}
	}
	frameRate_ {|val = 10|
		views.collect{|v| v.frameRate_(val)}
	}

}
