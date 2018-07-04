## Card compatibility

### General

Most cards configured for SCP02 should just work.

Cards using SCP01 are currently not supported. 

SCP03 support is in the works.

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

#### NXP

The following NXP cards have been used as development targets:

 * JCOP 2.4.2 R3 (JavaCard 3.0.1, GlobalPlatform 2.2.1, SCP02-55)
   * J2E145 (Contact-only, 144k)
 * JCOP 2.4.2 R2 (JavaCard 3.0.1, GlobalPlatform 2.2, SCP02-55)
   * J3D081 (Dual-interface, 80k)
 * JCOP 2.4.1 R3 (JavaCard 2.2.2, GlobalPlatform 2.1.1, SCP02-15)
   * J3A080 (Dual-interface, 80k)
   * J2A081 (Contact-only, 80k)
   * J2A080 (Contact-only, 80k)

Recent cards are available from various resellers in small to large quantities.

Other card variants should also work as long as the card is configured for SCP02.

### Others

#### Gemalto

Not yet supported. Cards require key diversification, which is not yet supported.

#### Giesecke &amp; Devrient

The following G&amp;D cards have been tried at some point but are not fully working:

 * SmartCafe Expert 3.2 (peculiar about its SCP02)
 * SmartCafe Expert 7.0 (uses SCP03)
