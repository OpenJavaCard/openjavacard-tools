## Card compatibility

### Security Protocols

Most cards configured for SCP02 or SCP03 should just work.

Cards using SCP01 are currently not supported.

Key diversification of the EMV and VISA2 types is supported.

### Known Working

#### ACS

The following ACS cards have been tried at some point:

 * ACS ACOSJ (GlobalPlatform 2.2.1, SCP02-55, Dual-Interface, 40k)

#### Feitian

The following Feitian cards have been tried at some point:

 * Feitian JavaCOS
   * A22 (GlobalPlatform 2.1.1, SCP02-55, Dual-Interface, 150k)
   * A40 (GlobalPlatform 2.1.1, SCP02-55, Dual-Interface, 64k)

These cards come without an SSD package, so they have only one security domain.

#### Giesecke &amp; Devrient

The following G&amp;D cards seem to work:

 * SmartCafe Expert 3.2
 * SmartCafe Expert 7.0 (SCP03 seems to have broken RENC)

#### NXP

The following kinds of NXP cards have been used as development targets:

 * JCOP 2.4.? R? (JavaCard 3.0.4)
   * J3H145 (Dual-interface, 144k)
 * JCOP 2.4.2 R3 (JavaCard 3.0.1, GlobalPlatform 2.2.1, SCP02-55)
   * J2E145 (Contact-only, 144k)
 * JCOP 2.4.2 R2 (JavaCard 3.0.1, GlobalPlatform 2.2, SCP02-55)
   * J3D081 (Dual-interface, 80k)
 * JCOP 2.4.1 R3 (JavaCard 2.2.2, GlobalPlatform 2.1.1, SCP02-15)
   * J3A080 (Dual-interface, 80k)
   * J2A081 (Contact-only, 80k)
   * J2A080 (Contact-only, 80k)

Recent cards are available from various resellers in small to large quantities.

There are many different configurations on the market, so it can be difficult
to find out what you are going to get.

Other card variants should also work as long as the card is configured for SCP02.

#### sysmocom

The following USIM card from the open mobile communications company sysmocom works:

 * sysmoUSIM-SJS1

### Others

#### Gemalto

Not yet supported. Cards require key diversification, which is not yet supported.

