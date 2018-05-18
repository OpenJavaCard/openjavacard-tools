## Building openjavacard-tools

### Gradle build

For convenient use the toolkit can be built using gradle:

```
$ ./gradlew fatJar
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

If you encounter problems or want verbose output you can use the "run-trace.sh" wrapper to run the tool with trace-level logging.

### Standalone ant build

STOP: Currently broken because of splitting the package. Will be fixed.

There also is an ant build which is more convenient when building or developing offline.

Dependencies are taken from /usr/share/java by default.

You need to install the required Java libraries (Example for Debian):

```
$ aptitude install ant libbcprov-java libjcommander-java libslf4j-java liblogback-java junit4
```

You should then be able to build the package using ant:

```
$ ant
```

Tests can be executed and API documentation can be built:

```
$ ant test
$ ant javadoc
```
