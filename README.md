# SimpleDMXControl
*Till Bovermann, 2022/2023 for [plonk](https://plonk.studio).*


SuperCollider Quark to interface with Enttec DMX Pro (mk2).

Implements

+ **SimpleDMX** — Base class for DMX interfaces 
+ **SimpleAbstractSerialDMX** —  Base class for serial port DMX interfaces
+ **SimpleEnttecDMXPro** —  Class for Enttec DMX Pro interface
+ **SimpleEnttecDMXProMk2** —  Class for Enttec DMX Pro mk2 interface
+ **SimpleDMXFixture** —  Class for defining DMX fixtures to be used in **SimpleDMXUniverse**
+ **SimpleDMXUniverse** —  Class implementing a DMX universe to be used in **SimpleDMX**


See helpfiles for details.

## Thanks

Inspired by [EnttecDMX](https://github.com/jreus/ENTTEC_USB_PRO) by Jonathan Reus, 2016 and [DMX](https://github.com/supercollider-quarks/DMX) by Marije Baalman. 
Implemented and tested with help from Bruno Gola and Constantin Engelmann.
