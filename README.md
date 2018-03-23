## OpenJavaCard Development Tools

This project is a toolkit for JavaCard development and provisioning.

It contains library components as well as command-line tools for
interacting with smartcards using the GlobalPlatform protocol,
which allows upload and installation of packages and applets as
well as various other management operations.

CAUTION: This is still in its beta stage, even for development use.

[![Build Status](https://travis-ci.org/OpenJavaCard/openjavacard-tools.svg?branch=master)](https://travis-ci.org/OpenJavaCard/openjavacard-tools)

### Documentation

 * [Build instructions](doc/building.md)
 * [Card compatibiliy](doc/cards.md)

### Features

 * Clean-room GlobalPlatform host library
 * Partial CAP processing library
 * Verbose command-line tools
 * Provisioning tools
   * GlobalPlatform commands
 * Development tools
   * Comfortable APDU command
   * AID scanner
   * CAP file info and sizing tool
   * Klunky scripting

### Status

 * Experimental
   * Not even a review
   * Security considered but unverified
   * May eat your homework and cyberize your cat
 * Completeness
   * Most GlobalPlatform commands implemented
   * Key handling commands are incomplete but not missing much
   * Card state and identity commands did not get much testing yet
   * Works quite well for development on compatible cards
 * Security protocols
   * SCP02 seems to work okay
   * SCP01 is mostly untested
   * SCP03 is completely untested
   * Key diversification is not implemented (some cards only)
 * Plans
   * Provide more documentation
   * Establish testing and finish what is there
   * Incrementally work on development usecase
   * Offer a Maven build for more general applicability
   * Planned: keystore support
   * Planned: use for local provisioning and updates
   * Not sure: offline command-stream generation
   * Distant future: CAP conversion and verification

### Acknowledgements

Contains some crypto routines from [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro).

Built with the help of some of code from [IsoApplet](https://github.com/philipWendland/IsoApplet/).

### Legal

Vast majority of the code has been developed for this project:

```
openjavacard-tools: OpenJavaCard Development Tools
Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
```

This project contains some crypto routines from [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro):

```
gpj - Global Platform for Java SmartCardIO

Copyright (C) 2009 Wojciech Mostowski, woj@cs.ru.nl
Copyright (C) 2009 Francois Kooman, F.Kooman@student.science.ru.nl
Copyright (C) 2015-2017 Martin Paljak, martin@martinpaljak.net

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
```

At some point these license dependencies will be eliminated in the course of introducing keystore support and cleaning up TLV parsing, respectively.
