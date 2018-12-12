package org.openjavacard.iso;

public enum AIDUsage {
    PREFIX("Prefix"),
    APPLET("Applet"),
    DOMAIN("Domain"),
    MODULE("Module"),
    PACKAGE("Package");

    public final String name;

    AIDUsage(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
