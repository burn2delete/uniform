(doseq [helper '[semantic-llvm-final-artifact
                 semantic-llvm-final-b13-context
                 semantic-llvm-final-build-context
                 semantic-llvm-final-contract-context
                 semantic-llvm-final-verification-context
                 semantic-llvm-frozen-contract-base-context
                 semantic-llvm-frozen-contract-build-context
                 semantic-llvm-frozen-contract-path-context
                 semantic-llvm-frozen-contract-section-01
                 semantic-llvm-frozen-contract-section-02
                 semantic-llvm-frozen-contract-section-03
                 semantic-llvm-frozen-contract-section-04
                 semantic-llvm-frozen-contract-section-05
                 semantic-llvm-frozen-contract-section-06
                 semantic-llvm-frozen-contract-section-07
                 semantic-llvm-frozen-contract-section-08
                 semantic-llvm-frozen-contract-section-09
                 semantic-llvm-legacy-toolchain-phase-01!
                 semantic-llvm-legacy-toolchain-phase-02!
                 semantic-llvm-legacy-toolchain-phase-03!
                 semantic-llvm-legacy-toolchain-record
                 semantic-llvm-legacy-toolchain-run!
                 semantic-llvm-legacy-toolchain-snapshots!
                 semantic-llvm-linux-toolchain-execution!
                 semantic-llvm-linux-toolchain-observations!
                 semantic-llvm-linux-toolchain-record
                 p15-s23-b3-llvm-final-record
                 p15-s23-b3-llvm-finalization-token
                 p15-s23-b3-llvm-tool-observation-state]]
  (ns-unmap *ns* helper))

(ns-unmap *ns* 'semantic-llvm-authority-holder)
