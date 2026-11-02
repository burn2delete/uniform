# Preserved cold-metadata negative controls

PR618's frozen source is
`3a1a553dbb1b3eb0ae472f5bf806171c02a71109`, tree
`c8600e2c52f4ba47f1366fae4035248d23365623`. Its production accounting removal
was incorporated by PR628 and later main changes. This port keeps current
production unchanged and restores four semantic negative cases through the
existing shared fixture: wrong mode, forged excluded-node IDs, forged source
argument call IDs and a missing source-argument-call-ID field.

The existing retired-field and unknown-field refusals remain. Current C7,
Effects and lowering consumers refuse the same substitutions against their
three-key cold schema. The C7 producer projects semantic fields; its control
checks that substitutions remain visible rather than silently normalized away.
This does not restore historical counters or accept extra keys at consumers.
The original cold-quote census and report are retained byte-for-byte as records
of the frozen source, not as claims about the current source schema.

The composition base is main
`068321a889f4b4278c234a53d3f7a92d1300d22b`, tree
`744a2318c4627c4fc7fd29754a2e1e7244f3b896`. Unintegrated parent source from
PR593/598/613/614/615 is not replayed. This change establishes no source/Core
authentication, runtime/native-provider, whole accounting or PR502 closure,
release, self-hosting or seed-retirement support.

The three current C7/Effects/lowering kernels passed 3 tests / 28 assertions.
The first C7 run had four assertion failures because its preexisting loop
expected unchanged output for every variant. The corrected producer control
checks exact field projection; consumer refusals retain all eight variants.
Independent exact-composition review, CI and integration preflight remain
required.
