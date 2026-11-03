# Current selected Syntax ownership and consumer controls

This increment binds explicit development-current Syntax selection to the
actual owner-produced operation context and implementation projection. C3
checks the selected owner for registered literals as well as ordinary source
products. A forged or missing physical projection is refused even when C3's
path-neutral artifact ID is unchanged. Missing selected projections do not
fall back to frozen source. Default pinned-source refusal is preserved.

The owning contracts are C3, C5, D8, D9, and the selected Syntax source and
diagnostic consumer contract in `docs/end-to-end-contracts.md`. This is a
development-source increment; selected source remains unadmitted for migration
and release purposes.

## Original path accounting

The original PR544 authored delta is
`68a127520c2465a90a4a9b06742cb58ebaffca77` through
`e73520fd810184689af72469931400fabff40b6f`, tree
`86eb534f45a91525354896c56178a5d1fd9f1e33`. All 19 paths are enumerated in
the paired EDN receipt.

- Current main already contains the harness code/test, C3 model field removal,
  closed invocation and explicit selection checks, and the source quota removal.
  Its Reader revision additionally has no 1 MiB project ceiling.
- The old standalone binding helper and its load-order insertion are replaced
  by the consolidated `00851` owner. Its source snapshot, public DATA, eleven
  exports, byte/hash/rule bookends, operation context, implementation projection,
  and fresh verification responsibilities are retained.
- Old rich-object, resolved-result and compiler construction implementations are
  now owned by `gravity.syntax`. Current `00842`, `00851`, `00852` and resolution
  construction-input changes preserve their protocol responsibilities.
- All 22 original adapter test names remain. Quota-only assertions are retired;
  type, graph, serialization, Unicode, provenance, metadata, identity, mutation
  and refusal controls remain. The seven selection controls use an actual
  context/projection separation case in place of the old source-shape oracle.
- The original README and evidence/reconciliation receipts describe a prepared
  harness before this implementation. They remain historical records.

## Current source and checks

The immutable tested source checkpoint is
`83c458c1866889b0ac428e8436c3cc9f728753e4`, tree
`611e48ff155a1fc40fa2c62a00e57c0a10b23dd4`, over main
`0e2b37cd2eefbab2f02e5898b960f9bbc4100fef`. The receipt records byte
counts and SHA-256 values for all eight changed source/test inputs. It is
written after that source checkpoint and does not self-hash its own metadata.
The three report files were uncommitted during development checks; their
presence does not change the eight recorded source/test inputs. These runs
make no whole-worktree cleanliness claim. Final integration requires a clean
committed candidate and its own exact preflight.

The fresh C3 product/template/owner/tamper and source metadata/hygiene/Unicode
run passed 6 tests and 86 assertions at that source checkpoint. Exact selectors
and logs are in the EDN receipt. These execute the source and fresh verifiers;
the same-ID projection forgery is not accepted as owner authority.

Composition checkpoint `7ee80ec222cfd5d73744c2f1b533bcffa37a1344`, tree
`f388e758b24f2ac55b0d937160df32c0e1fda61f`, normally incorporates main
`438978627bce8ceb9bdc82bb2bc1385a645a2fec` and retains original PR544
history without replaying its tree. All eight source/test inputs remain
byte-identical to the tested checkpoint. Selection and language-boundary
checks at this later composition passed 13 tests and 96 assertions.

Pinned direct lint reports zero findings for the cleaned Syntax source. Two
unused destructured locals and a redundant nested let were removed without
changing ordered binding evaluation. The earlier three changed-source
warnings were blocking findings, not baseline warnings. The earlier full
context comparison timed out after 120 seconds while checking its unchanged
baseline; that attempt remains a nonpass at its recorded base.
The later full context comparison against main `4389786` passed with zero
blocking findings at composition `7ee80ec`; its exact log is in the receipt.

Independent review and exact integration preflight remain separate. This does
not establish source admission, migration, public C7/B47 support, complete
PR512 acceptance, native-target execution, release or self-hosting.
