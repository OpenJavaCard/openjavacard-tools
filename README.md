## OpenJavaCard Tools

This project is a toolkit for JavaCard development and provisioning.

It contains library components as well as command-line tools for
interacting with smartcards using the GlobalPlatform protocol,
which allows upload and installation of packages and applets as
well as various other management operations.

CAUTION: This is still in its beta stage, even for development use.

[![Build Status](https://travis-ci.org/OpenJavaCard/openjavacard-tools.svg?branch=master)](https://travis-ci.org/OpenJavaCard/openjavacard-tools)

### Project

For more information about this overall project, see our [website](https://openjavacard.org/).

You can follow us on [Twitter](https://twitter.com/openjavacardorg) and chat with us on [Gitter](https://gitter.com/openjavacard).

### Documentation

 * [Build instructions](doc/building.md)
 * [Card compatibiliy](doc/cards.md)

### Features

 * Command-line tools
   * great for developing applets and libraries
   * compatible with a wide array of cards
 * GlobalPlatform host library
   * abstracts most GP features
   * intended for reuse from the start
 * CAP processing library
   * currently used only for loading packages
   * intended for CAP conversion/verification
 * Development tools
   * GlobalPlatform commands
   * Generic smartcard commands
   * Basic scripting

### Status

 * Experimental
   * Not even a review
   * Security considered but unverified
   * May eat your homework and cyberize your cat
 * Completeness
   * Most commands relevant to GP 2.2 implemented
   * Some features from GP 2.3 implemented
   * DAP is not supported yet
   * Works quite well for development on compatible cards
 * Security protocols
   * SCP02 works (C-ENC works, R-MAC experimental)
   * SCP03 works (C-ENC works, R-MAC and R-ENC experimental)
   * SCP01 is mostly untested
   * EMV and VISA2 key diversification
 * Plans
   * Implement more extensive tests
   * Implement tests against real cards
   * Support for full use of security domains
   * Support for more practical key handling
   * Incrementally work on development usecase
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
Copyright (C) 2015-2019 Ingo Albrecht <copyright@promovicz.org>

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

We will replace these routines at some point, probably when implementing keystore support.
