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
