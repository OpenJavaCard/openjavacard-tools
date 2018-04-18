## Card compatibility

### Policy

This project is vendor-neutral, with a marked preference for cards that are available to the general public through retail channels. We will also prefer vendors with an open attitude (open sales, open information policy, open source).

We will make a general effort to support any cards that we can get our hands on if we consider them "usable" for one purpose or another. 

Being supported is no statement about the quality of the cards implementation, its security or suitability for any particular purpose. We also do not specifically endorse any manufacturers or resellers.

No structured testing is being performed at this point, but we are working on a test farm that we will use in the future.

### General

Most cards configured for SCP02 (the common default) will just work. Cards that require key diversification (Gemalto, Giesecke &amp; Devrient) are not supported yet.

Cards using SCP01 might work or not. We have not tested any in a while, and you might have to specify the protocol parameters manually.

SCP03 has been implemented but not tested yet. It is unlikely to work.

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

Not assessed. Seems to require key diversification.
