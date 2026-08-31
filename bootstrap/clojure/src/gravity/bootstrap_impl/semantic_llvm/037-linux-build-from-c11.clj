(defn- p15-s23-b3-llvm-linux-build-from-c11!
  [c11-artifact checked-core context options]
  (let [source-path (:source-path context)]
    (p15-s23-b3-llvm-linux-target-preflight!
     source-path c11-artifact context)
    (p15-s23-b3-llvm-preflight! c11-artifact)
    (let [bridge
          (p15-s23-stage2-c13-c14-b1-packet-from-c11!
           c11-artifact checked-core context)
          binding
          (p15-s23-b3-llvm-source-binding!
           p15-s23-b3-llvm-finalization-token source-path)
          lowering
          (p15-s23-b3-llvm-invoke-builder!
           p15-s23-b3-llvm-finalization-token binding
           (:optimized-mir bridge) source-path)]
      (p15-s23-b3-llvm-linux-artifact-record
       c11-artifact checked-core context bridge lowering binding options))))
