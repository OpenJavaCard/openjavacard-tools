## Building openjavacard-tools

The toolkit can be built using ant because it is much simpler than maven.

A Maven build will be provided in the future.

Dependencies are currently pulled in as submodules so that one can easily build this in a closed environment.

Therefore you first need to pull in the dependencies:

```
$ git submodule update --init
```

Then you can build the package:

```
$ ant
```

You can then run the tool using the provided shell script:

```
$ ./run.sh
  Commands:
    cap-info	CAP: Show information about a cap file
    cap-size	CAP: Show size of a cap file, including load size
    apdu	Generic: Send an APDU to the card 
    scan-name	Generic: Scan a card using SELECT BY NAME
    gp-info	GlobalPlatform: show information about card
    gp-list	GlobalPlatform: list objects on card
    gp-load	GlobalPlatform: load objects onto the card
    gp-install	GlobalPlatform: install an applet
    gp-delete	GlobalPlatform: delete applets or packages from the card
    gp-extradite	GlobalPlatform: extradite an application to an SD
    gp-state	GlobalPlatform: set state of the card or applets
    gp-identity	GlobalPlatform: set card identity
    gp-keys	GlobalPlatform: set card security keys
    help	Show help for available commands
    script	Run commands from a script
```

You can also run the tests (there are none yet, but soon there will be):

```
$ ant test
```

And also generate the reference JavaDoc:

```
$ ant javadoc
```
