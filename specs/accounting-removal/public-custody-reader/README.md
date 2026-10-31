# Reusable local custody and exact DATA reader

This source increment adds no-C same-thread process custody and defers catalog
discovery until selection. Normal parallel/start and protected namespace/RV
guards are unchanged. Single-domain admission and existing lease arities are
unchanged. No joint-domain or selected private-role executor is included.

`with-process-custody [operation finalize-owned-resources! observe!]` preserves
the original value/error and retains unresolved local handles. Registry changes
are forbidden after the operation phase, including changes caught by a later
callback. The observer receives a read-only completion view, populated only
after the observer returns or throws. Read it after scope exit. Finalization
precedes observation. Late errors cannot turn unknown custody into completion.
This API acquires no broker domain and provides no durable post-parent custody.

`read-exact-child-DATA! [reference exact-byte-length]` checks an already selected
absolute SOURCE reference, regular NOFOLLOW file identity, exact byte length,
EOF, stable before/after identity/size and SHA256. Read failures stay primary;
distinct close failures are suppressed and causal interruption is restored.
Its integer length range reflects byte-array/readNBytes representation only.
The protocol module is pure DATA; the fence declaration registry remains empty.
Neither API authenticates a current caller, installs a record or grants custody.

Each namespace and protocol dependency must have one selected resource origin.
Use a fresh JVM when changing source profiles; an already interned private Var
does not disappear when its source file changes. The public parallel namespace
has no selected private-role export. Consumers requiring that export must
refuse; ordinary runners, alternate arities and mixed source roots are not
fallbacks. No caller-selected domain/environment/command option is added.

The focused controls load only reusable namespaces, use injected resource
ports and private inert files, and verify source origins, default legacy
behavior, local phase/error/interrupt handling, deferred catalog and reader
refusals. They start no process and acquire no broker or canonical lock.
Source acceptance does not establish effective selected configuration, semantic
execution, creation coverage, original-L authority or terminal containment.
