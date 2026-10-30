# Preserved List-join consumer controls

Owner: ACC-01. Parent: PR595, exact
`99893151b3d0b7c3c435d5c2e6e90c03255b5951`. This test-only child restores
two original kernels from historical PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`. It does not accept the parent,
remove Effects/lowering accounting or change production.

## Restored source

The complete `body-list-if-join-development-kernel` and
`current-gravity-list-if-join-lowering-development-kernel` bodies and comments
are byte-identical to history. No source substitutions are needed. Every
surrounding parent test byte and all production remain unchanged.

Both sides of the branch join now have width-1025 literal List cases. Their
checked-member extents must be 1025; a coherently changed extent of 1024 must
reject. Each original late-invalid member at index 1024, extra type field and
nonvector member carrier remains covered through actual C7, Effects and
lowering dispatch definitions. Missing-local diagnostics, source/evidence
tampering, truthiness, lowered instruction, runtime value and identity checks
remain in the original lowering kernel. These are test definitions, not
observed execution results.

The Effects kernel has 16 syntactic assertion forms versus the parent's 11.
The lowering kernel has 27 versus 22: six original semantic assertion forms
are restored and one parent-only payment-counter assertion is retired. Its
paired missing-local diagnostic remains. The removed assertion equated a
failure's work count to prepaid work plus a visit index; it enforced no type,
source, diagnostic or runtime semantics. Other historical outer-accounting
assertions remain visible and unresolved; this restoration does not endorse
them or claim complete accounting cleanup.

## Dependencies and evidence

C7, C8, C14, L2 and D9 govern the retained type/effect/lowering, source and
evidence obligations. `list-join-consumer-controls-census.edn` pins the two
complete raw spans, both before/after files, nine directly named owner
definitions and six fixture/dispatch/projection definitions. The runtime
wrapper pin and uninvoked projection helper are limited source evidence;
they do not prove the runtime's transitive call graph or native behavior.
Historical helper differences are recorded without equivalence inference.

## Acceptance and remaining work

Both complete-file equations, nine owner and six host-helper pins, boundary
6/11, documentation 240/19, roadmap and whitespace checks pass. Twelve inert
source mutations refuse reduced width, moved late-invalid member, disabled
extent forgery, weakened applicability, lost assertion and substituted C7
owner cases in each kernel. Lint has zero errors and exactly the parent's
152-warning/one-info finding multiset. Independent source review and CI remain
pending. Genuine C7/Effects/lowering/runtime
execution is UNRUN. No Bootstrap or Gravity definition is evaluated by the
source checker, and no held operation or selector is retried.

Parent acceptance, current-main composition and final integration are separate.
No authenticated admission, whole ACC01/PR502 closure, release or self-hosting
authority follows from restored test definitions or green routine checks.
