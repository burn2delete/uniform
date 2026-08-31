; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l18!
 [operator node]
 (case
  operator
  altmem/provider
  (typed-diagnostic!
   "L18-PROVIDER"
   "no memory provider satisfies the active profile requirement"
   node
   "Select a deterministic memory provider declared for the active profile, target, allocation families, and capability grants."
   {:provider-id nil,
    :active-profile (:profile node),
    :memory-family :alloc/arena,
    :capability-scope :memory})
  altmem/hidden-alloc
  (typed-diagnostic!
   "L18-HIDDEN-ALLOC"
   "allocation is hidden where the profile forbids it"
   node
   "Declare allocation effects and reject hidden provider allocation in constrained profiles unless the profile admits it."
   {:provider-id 'custom.memory/arena,
    :active-profile (:profile node),
    :memory-family :alloc/managed,
    :hidden? true})
  altmem/lifetime
  (typed-diagnostic!
   "L18-LIFETIME"
   "reference or borrow can outlive its storage"
   node
   "Emit lifetime facts, insert a runtime check, reject the program, or require an unsafe boundary."
   {:provider-id 'custom.memory/arena,
    :memory-family :alloc/region,
    :region :region/frame,
    :required-proof :lifetime-valid})
  altmem/escape
  (typed-diagnostic!
   "L18-ESCAPE"
   "value escapes its region, arena, stack, or device scope"
   node
   "Reject the escape or transfer ownership through an explicit checked boundary."
   {:provider-id 'custom.memory/arena,
    :memory-family :alloc/arena,
    :region :arena/frame,
    :required-proof :no-escape})
  altmem/alias
  (typed-diagnostic!
   "L18-ALIAS"
   "aliasing violates ownership or mutation rules"
   node
   "Preserve ownership, borrow, alias, and mutation facts in the typed artifact."
   {:provider-id 'custom.memory/arena,
    :memory-family :ownership,
    :required-proof :unique-mutable-borrow})
  altmem/uninit
  (typed-diagnostic!
   "L18-UNINIT"
   "value may be read before initialization"
   node
   "Emit initialization facts or retain a runtime initialization check."
   {:provider-id 'custom.memory/arena,
    :memory-family :initialization,
    :required-proof :initialized-range})
  altmem/double-release
  (typed-diagnostic!
   "L18-DOUBLE-RELEASE"
   "linear resource may be released twice"
   node
   "Track release state and reject duplicate release paths."
   {:provider-id 'custom.memory/arena,
    :memory-family :linear-resource,
    :required-proof :exactly-once-release})
  altmem/leak
  (typed-diagnostic!
   "L18-LEAK"
   "resource requiring release is not released"
   node
   "Emit release evidence for every linear resource or reject the leaking path."
   {:provider-id 'custom.memory/arena,
    :memory-family :linear-resource,
    :required-proof :no-leak})
  altmem/bounds
  (typed-diagnostic!
   "L18-BOUNDS"
   "memory access may exceed its range"
   node
   "Prove bounds, retain a runtime bounds check, or reject the access."
   {:provider-id 'custom.memory/arena,
    :memory-family :borrowed-view,
    :required-proof :in-bounds})
  altmem/device-sync
  (typed-diagnostic!
   "L18-DEVICE-SYNC"
   "host/device synchronization is missing"
   node
   "Record device ownership, transfer, synchronization, and visibility facts before crossing the boundary."
   {:provider-id 'custom.memory/device,
    :memory-family :alloc/device,
    :required-proof :host-device-sync})
  altmem/mmio
  (typed-diagnostic!
   "L18-MMIO"
   "MMIO address, width, volatility, or ordering is invalid"
   node
   "Validate the device map, access width, volatile semantics, ordering, and capability scope."
   {:provider-id 'custom.memory/mmio,
    :memory-family :memory-mapped-io,
    :capability-scope :hardware/mmio,
    :required-proof :mmio-safe})
  altmem/ffi-allocator-error
  (typed-diagnostic!
   "L18-FFI-ALLOCATOR"
   "foreign allocation and release providers mismatch"
   node
   "Record allocator identity and require compatible release operations for foreign memory."
   {:provider-id 'custom.memory/foreign,
    :memory-family :alloc/foreign,
    :allocator :lib-a/malloc,
    :release :lib-b/free})
  altmem/unsafe-audit
  (typed-diagnostic!
   "L18-UNSAFE-AUDIT"
   "safe wrapper lacks unsafe invariant evidence"
   node
   "Keep unsafe internals visible and attach invariant, capability, layout, lifetime, and audit evidence."
   {:provider-id 'custom.memory/mmio,
    :memory-family :memory-mapped-io,
    :safe-wrapper :mmio.safe/read-u32,
    :required-proof :wrapper-invariant})
  semantic-early-call-specific-diagnostic-unhandled))
