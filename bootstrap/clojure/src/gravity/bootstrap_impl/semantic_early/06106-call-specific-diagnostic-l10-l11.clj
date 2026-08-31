; Semantic decomposition of HEAD reader line 6106.
; Loaded into gravity.bootstrap; intentionally no ns form.

(defn
 semantic-early-call-specific-diagnostic-l10-l11!
 [operator node]
 (case
  operator
  memory/uninit-read
  (typed-diagnostic!
   "L10-UNINIT-READ"
   "code reads memory before initialization is proven"
   node
   "Prove initialization, retain a runtime check, or reject the access.")
  hidden/alloc
  (typed-diagnostic!
   "L10-HIDDEN-ALLOC"
   "allocation is hidden from profile and memory artifacts"
   node
   "Declare allocation effects and memory provider policy or reject the profile.")
  move/use-after
  (typed-diagnostic!
   "L10-USE-AFTER-MOVE"
   "owned value is used after transfer"
   node
   "Do not use a moved value unless ownership is returned explicitly.")
  borrow/escape
  (typed-diagnostic!
   "L10-BORROW-ESCAPE"
   "borrow may outlive its owner or region"
   node
   "Constrain the borrow lifetime or copy/move into a valid owner.")
  bounds/get
  (typed-diagnostic!
   "L10-BOUNDS"
   "memory access cannot be proven in bounds and lacks an allowed check"
   node
   "Add proof, bounds check, or reject the access.")
  mmio/read
  (typed-diagnostic!
   "L10-MMIO-CAP"
   "MMIO operation lacks capability or profile support"
   node
   "Use an MMIO provider grant and audited safe wrapper.")
  shared/mutate
  (typed-diagnostic!
   "L11-DATA-RACE"
   "shared mutable state is accessed without synchronization"
   node
   "Use an atomic, lock, actor, channel, synchronized cell, or unsafe island with audit evidence.")
  task/borrow
  (typed-diagnostic!
   "L11-BORROW-TASK"
   "borrow crosses task lifetime boundary illegally"
   node
   "Move owned data, copy immutable data, or keep the borrow inside the parent scope.")
  atomic/load
  (typed-diagnostic!
   "L11-ATOMIC-ORDER"
   "atomic operation lacks legal memory ordering"
   node
   "Declare an explicit ordering accepted by the active profile.")
  workflow/race
  (typed-diagnostic!
   "L11-REPLAY-RACE"
   "durable workflow concurrency lacks replay record"
   node
   "Record stable event ids and replay ordering for concurrent workflow branches.")
  gpu/shared-read
  (typed-diagnostic!
   "L11-GPU-BARRIER"
   "GPU shared memory access lacks barrier or proof"
   node
   "Add a barrier, proof, or reject the shared-memory access.")
  semantic-early-call-specific-diagnostic-unhandled))
