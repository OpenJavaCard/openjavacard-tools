## OpenJavaCard Development Tools

This project is a toolkit for JavaCard development.

CAUTION: This is still in its beta stage.

It currently contains:

 * Implementation of GlobalPlatform as a clean-room library
 * Command-line tool for interacting with GlobalPlatform cards
 * Beginnings of a CAP processing library (load-file generation)

Various bits are unfinished:

 * Key handling commands have not been finished
 * Keystore support has not been finished
 * SCP03 is completely untested
 * SCP01 is mostly untested
 * Key diversification is not implemented

The commandline tools have several advanced features:

 * They can be used during development to install and
   continuously reinstall a whole chain of dependent packages
 * They are verbose and have lots of logging so that
   GlobalPlatform interaction can be debugged in detail

The libraries has been developed with the following in mind:

 * Access to card information in an abstract manner so that
   an automated provisioning system can use that information

Features that the design does not support yet:

 * Offline generation of update command streams
 * Using DAP verification

Goals that might take a while to achieve:

 * Implement a CAP converter and verifier so that one can go 100% opensource
