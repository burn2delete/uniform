# Reader constructor current-source composition

This local candidate composes accepted SOURCE/MODELED inputs only. It does not
integrate main, publish the constructor, run the command or grant runtime credit.
The constructor `740fadf162483fb6c7180a911b87f1f1d4b76dd0`, tree
`0cd0bd564f4c3b2de976b282bb73cc3f28b16d14`, has qualified independent source/model
acceptance, receipt SHA256
`50b2f8505f9ed021dccbbdb503671eff4f63ed37f60b5ed6326af1a4c841f282`.
That receipt was consumed once; its 177 checks were not repeated as a review.

## Exact source composition

The common base is `a1aa3651f226359190db282e82375b89de661736`.
Normal merge `b225409bd43fceb55901fd10984e8e0b9850b4c7` has ordered parents
`740fadf162483fb6c7180a911b87f1f1d4b76dd0` and
`6878660c0be560d9515ba3bc8d8dc7d26c8586eb`, the published pure Reader/CI head.
Normal merge `381268a33b11612e251d6bc37cd5fd53106afec4` adds current main
`ecd3c5d4920e0d45cfdc8d7b6203600682422ea6`. Its tree is
`265575aef1b3ca416ff4d6ed75bd4087a4a2ca62`.
The direct merge preview of constructor `740fadf1` and CI merge `d442fb35`
has that same tree, independently checking the sequential composition equation.
Neither merge needed a conflict resolution or implementation rewrite.

The complete Git blob/mode map preserves 38 constructor-only paths and
79 incoming-only paths, plus all 9639 common entries. Exactly two paths combine:

- `bootstrap_impl/load_order.edn` retains the constructor's native-view helper
  before the native validator and main's removal of three retired Resolution
  fragments; every resource remains unique.
- `p15_s23/emitter.gravity` retains the constructor's build-read compiler helper
  and compile-call body byte-for-byte. Removing precisely that insertion and
  restoring the incoming call body reconstructs the incoming emitter bytes
  exactly, including main's independent parameter-source validation changes.

No issuer, bridge, constructor, raw-result, adapter, envelope or private consumer
implementation is changed by the composition. The current main Resolution
verification extraction and published complete identity equations are retained.
Pinned migration/release authority is not inferred from these source results.

## Concrete inventory correction

The focused existing SH-01 ownership check initially reported two failures:
`gravity/reader.gravity` existed but had no module-owner entry, and the expected
Gravity inventory still described 69 modules. Registering the new module under
`:sh-reader` exposed the corresponding old total-owner count as well. The local
metadata correction adds exactly that Reader entry and updates both inventory
assertions for the one additional module. It changes no source implementation,
profile, grant, lifecycle state, runtime support or milestone disposition.
The original failed observations remain historical evidence.

Affected source/injected controls passed 42 tests / 659 assertions. The language
boundary passed 6 / 11. After correction the focused existing ownership check
passed 1 test / 567 assertions. No Bootstrap, actual Reader/Gravity compiler or
runtime, CLI, provider IO, shared 6343 operation, lease or probe was executed.
The ownership metadata and combined-source composition need their own affected
acceptance; prior source acceptance does not automatically cover them.

## Conditional command-link input

Issuer successor `bd014009b2d2c281895919ff15e1fbc2da89fbc3`, tree
`fdf3dc2386b56dfc9e32a5f41c120ceccdf58df1`, has parent `740fadf1` and remains
under its separate affected review. Its only changed paths are invocation,
CLI dispatch and invocation tests. They do not overlap either combined path or
the ownership correction. A read-only merge preview against source merge
`381268a3` succeeds with tree `ef3da071b52997e9073fca4a8dd13aa44374abc0`.
That preview neither imports nor accepts the pending command/JSON implementation.
The actual checkout retains the original unavailable command link until its
owner routes an accepted successor. No issuer file is rewritten here.

## Existing hosted CI

Automatic run `41174900438` completed successfully at published head
`6878660c0be560d9515ba3bc8d8dc7d26c8586eb`. Its actual merge is `d442fb35`, tree
`a20d46f602840ffe0502bbec945e5e96d8ddf92e`, with ordered parents current main
`ecd3c5d4` and published head `6878660c`. Static, contracts, Bootstrap, Macro,
Resolution, portable/Darwin leaves, unsupported-host handling and snapshot jobs
passed; source-Core readiness and prerelease jobs remain configured skips.
This run excludes the effectful constructor and command-link successors.
Historical run `41172854770` retains its distinct portable HTTP 429 failure before
tests and its executed Resolution fixed-golden failure. No run was rerun or
manually dispatched. Full FL-P01-T01 effectful/genuine acceptance remains open.
