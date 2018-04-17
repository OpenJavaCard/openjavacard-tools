## Card compatibility

### Testing

Testing is currently only performed manually and sporadically. We plan to introduce automatic testing on a pool of actual cards before making releases.

### Working

#### NXP

The following NXP cards have been used as development targets:

 * JCOP 2.4.2 R3 (JavaCard 3.0.1, GlobalPlatform 2.2.1, SCP02-55 by default)
   * J2E145 (Contact-only, 144k)
 * JCOP 2.4.2 R2 (JavaCard 3.0.1, GlobalPlatform 2.2, SCP02-55 by default)
   * J3D081 (Dual-interface, 80k)
 * JCOP 2.4.1 R3 (JavaCard 2.2.2, GlobalPlatform 2.1.1, SCP02-15 by default)
   * J3A080 (Dual-interface, 80k)
   * J2A081 (Contact-only, 80k)
   * J2A080 (Contact-only, 80k)

Recent cards are available from various resellers in small to large quantities.

Other card variants should also work as long as the card is configured for SCP02.

### Others

#### ACS ACOSJ

Seen basic commands working on a users terminal. Will be in test pool.

#### Gemalto

Not yet supported. Cards require key diversification, which is not yet supported.

#### Giesecke &amp; Devrient

Not assessed. Seems to require key diversification.

#### Feitian

Not assessed.

### Protocols

#### SCP01 cards

SCP01 is implemented and used to work but is currently unsupported.

#### SCP02 cards

SCP02 seems to work well.

#### SCP03 cards

SCP03 is implemented to some degree but currently very much untested.
