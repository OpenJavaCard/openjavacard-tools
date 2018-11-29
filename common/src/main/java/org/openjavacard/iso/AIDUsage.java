package org.openjavacard.iso;

public enum AIDUsage {
    APPLET("Applet"),
    DOMAIN("Domain"),
    MODULE("Module"),
    PACKAGE("Package"),
    PREFIX("Prefix");

    public final String name;

    AIDUsage(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
