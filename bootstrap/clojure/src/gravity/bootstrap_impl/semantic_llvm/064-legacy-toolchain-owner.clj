(defn-
 p15-s23-b3-llvm-toolchain-transaction!
 [candidate source-path lowering publication-intent?]
 (p15-s23-b3-llvm-require-authority!
  candidate
  source-path
  :authenticated-toolchain-transaction)
 (p15-s23-b3-llvm-fail!
  "B3-TARGET"
  source-path
  {}
  {:missing-fact :legacy-host-toolchain-transaction-disabled})
 (let
  [workspace
   (java.nio.file.Files/createTempDirectory
    "gravity-b3-llvm-"
    (make-array java.nio.file.attribute.FileAttribute 0))
   ir-path
   (.resolve workspace "program.ll")
   object-path
   (.resolve workspace "program.o")
   executable-path
   (.resolve workspace "program")
   target
   (:target-triple p15-s23-b3-llvm-policy)
   primary-failure
   (atom nil)]
  (try
   (let
    [phase-01-state
     (semantic-llvm-legacy-toolchain-phase-01!
      candidate
      source-path
      lowering
      publication-intent?
      workspace
      ir-path
      object-path
      executable-path
      target
      primary-failure)
     phase-02-state
     (semantic-llvm-legacy-toolchain-phase-02!
      candidate
      source-path
      lowering
      publication-intent?
      workspace
      ir-path
      object-path
      executable-path
      target
      primary-failure
      phase-01-state)
     phase-03-state
     (semantic-llvm-legacy-toolchain-phase-03!
      candidate
      source-path
      lowering
      publication-intent?
      workspace
      ir-path
      object-path
      executable-path
      target
      primary-failure
      phase-02-state)
     run-state
     (semantic-llvm-legacy-toolchain-run!
      candidate
      source-path
      lowering
      publication-intent?
      workspace
      ir-path
      object-path
      executable-path
      target
      primary-failure
      phase-03-state)
     snapshot-state
     (semantic-llvm-legacy-toolchain-snapshots!
      candidate
      source-path
      lowering
      publication-intent?
      workspace
      ir-path
      object-path
      executable-path
      target
      primary-failure
      run-state)]
    (semantic-llvm-legacy-toolchain-record
     candidate
     source-path
     lowering
     publication-intent?
     workspace
     ir-path
     object-path
     executable-path
     target
     primary-failure
     snapshot-state))
   (catch Throwable error (reset! primary-failure error) (throw error))
   (finally
    (try
     (p15-s23-b3-llvm-delete-tree! candidate workspace source-path)
     (catch
      Throwable
      cleanup
      (if-let
       [error @primary-failure]
       (cond
        (instance? Error error)
        (.addSuppressed error cleanup)
        (instance? Error cleanup)
        (do (.addSuppressed cleanup error) (throw cleanup))
        :else
        (.addSuppressed error cleanup))
       (throw cleanup))))))))
