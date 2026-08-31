# SH-10 Authenticated Initialization and Move Fixtures

These paired `.gravity` and `.qst` fixtures provide bounded owned-mutable
lifecycle scenarios to C9. C9 binds each scenario to one exact value in the
authenticated SH-09 identity-bound effected core, derives type, effect,
capability, profile, target, span, and origin facts from that upstream result,
and then runs the existing ownership transition checker.

The accepted family covers initialization followed by read, explicit move, and
terminal consume. The rejected family covers uninitialized read, use after
move, double consume, and a function-return borrow that outlives its owner.

This family does not infer ownership from primitive types and does not claim a
general owned-mutable adapter, effectful or nonprimitive adaptation, aggregate
copy semantics, regions, arenas, general linear resources, transfer, runtime
borrow checks, unsafe audit execution, trusted digest resolution, MIR
preservation, SH-10 completion, self-hosting, seed retirement, or release.
